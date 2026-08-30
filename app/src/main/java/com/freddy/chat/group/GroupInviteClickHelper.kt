package com.freddy.chat.group

import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.im.GroupJoinPayActivity
import com.cboda.intelligent.bean.CheckGroupJoiBean
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.showToastMain
import com.freddy.chat.utils.AppGson
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupQrInfoRes
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.groupToSession
import com.freddy.webtrc.utils.BroadE.checkPeopleInGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 群邀请卡片点击：对齐 [com.cboda.intelligent.activity.ScanActivity] 入群校验。
 * 1. [checkPeopleInGroup] 已在群 → 进群聊
 * 2. 否则 [api/checkGroupJoin]：黑名单 Toast；收费进收费页；通过则申请入群
 * 3. 收费/申请前先 [im/group/detail] 拉取群信息，再创建 [GroupQrInfoRes]
 */
object GroupInviteClickHelper {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val inFlight = AtomicBoolean(false)

    fun handleClick(
        context: Context,
        groupId: String,
        groupName: String? = null,
        groupIcon: String? = null,
    ) {
        if (groupId.isBlank() || !inFlight.compareAndSet(false, true)) {
            return
        }
        val userId = MMKVBroadUtils.getRealUserId()
        checkPeopleInGroup(groupId, userId) { pair ->
            if (pair.first) {
                inFlight.set(false)
                openGroupChat(context, groupId, groupName, groupIcon)
            } else if (!TextUtils.isEmpty(pair.second)) {
                inFlight.set(false)
                showToastMain(pair.second ?: "")
            } else {
                checkGroupJoinEligibility(context, groupId, groupName, groupIcon)
            }
        }
    }

    private fun checkGroupJoinEligibility(
        context: Context,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
    ) {
        scope.launch {
            val params = hashMapOf<String, Any?>("id" to groupId)
            val response = withContext(Dispatchers.IO) {
                NetworkBroadRepository.postEncryptedFlatRaw2(
                    UrlBroadBroadConstants.CHECK_GROUP_JOIN,
                    CheckGroupJoiBean::class.java,
                    params,
                )
            }
            response.onSuccess { data ->
                if (data == null) {
                    inFlight.set(false)
                    showToastMain(R.string.scan_network_request_failed)
                    return@onSuccess
                }
                val groupInfo = buildGroupQrInfo(groupId, groupName, groupIcon, data)
                when {
                    data.isBlacklisted() -> {
                        inFlight.set(false)
                        showToastMain(R.string.scan_group_blacklisted)
                    }

                    data.isNeedPay() -> {
                        GroupJoinPayActivity.start(context, groupInfo)
//                        loadGroupDetailThenContinue(context, groupId, groupName, groupIcon, data, needPay = true)
                    }

                    data.isAllowJoin() || data.success -> {
                        applyJoinGroup(context, groupId, groupInfo)
                    }

                    else -> {
                        inFlight.set(false)
                        showToastMain(
                            data.msg?.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.scan_network_request_failed),
                        )
                    }
                }
            }.onFailure { _, message ->
                inFlight.set(false)
                showToastMain(
                    message.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.scan_network_request_failed),
                )
            }.onError {
                inFlight.set(false)
                showToastMain(R.string.scan_network_request_failed)
            }
        }
    }


    private fun buildGroupQrInfo(
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        check: CheckGroupJoiBean,
    ): GroupQrInfoRes {
        val info = GroupQrInfoRes()
        info.id = check.groupId?.takeIf { it.isNotBlank() } ?: groupId
        info.name = check.groupName?.takeIf { it.isNotBlank() } ?: groupName
        info.icon = check.groupIcon?.takeIf { it.isNotBlank() } ?: groupIcon
        if (check.memberCount > 0) {
            info.memCount = check.memberCount
        }
        if (!check.priceDualx.isNullOrBlank()) {
            info.joinPayPrice = check.priceDualx
        }
        val payFlag = when {
            check.openJoinPay == "1" || check.needPay == "1" -> "1"
            else -> check.openJoinPay
        }
        if (!payFlag.isNullOrBlank()) {
            info.openJoinPay = payFlag
            info.ensureBusiExt().apply {
                openJoinPay = payFlag
                if (!check.priceUsdt.isNullOrBlank()) {
                    joinPayPriceUsdt = check.priceUsdt
                }
                if (!check.feeRate.isNullOrBlank()) {
                    joinPayFeeRate = check.feeRate
                }
            }
        }
        return info
    }

    /**
     * 调用 [im/group/detail/{groupId}] 获取群信息后，再创建 [GroupQrInfoRes] 并继续收费页或申请入群。
     */
    private fun loadGroupDetailThenContinue(
        context: Context,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        check: CheckGroupJoiBean,
        needPay: Boolean,
    ) {
        ImGroupHttpExample.requestGetGroupDetail(
            groupId,
            object : OnHttpImListener<GroupVo?> {


                override fun onHttpImSuccess(result: GroupVo) {
                    if (result.id.isNullOrBlank()) {
                        inFlight.set(false)
                        showToastMain(R.string.scan_network_request_failed)
                        return
                    }
                    val groupInfo = toGroupQrInfo(result, groupName, groupIcon)
                    mergeCheckGroupJoinIntoQrInfo(groupInfo, check)
                    if (needPay) {
                        inFlight.set(false)
                        GroupJoinPayActivity.start(context, groupInfo)
                    } else {
                        applyJoinGroup(context, groupId, groupInfo)
                    }
                }
            },
            {
                inFlight.set(false)
                showToastMain(R.string.scan_network_request_failed)
            },
        )
    }

    private fun applyJoinGroup(context: Context, groupId: String, groupInfo: GroupQrInfoRes) {
        ImGroupHttpExample.requestApplyInGroup(
            groupId,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    inFlight.set(false)
                    if (!result) {
                        showToastMain(R.string.scan_network_request_failed)
                        return
                    }
                    if (isJoinNeedVerify(groupInfo)) {
                        showToastMain(R.string.scan_applied_waiting_group_owner)
                    } else {
                        openGroupChat(context, groupId, groupInfo.name, groupInfo.icon)
                    }
                }
            },
            {
                inFlight.set(false)
                showToastMain(R.string.scan_network_request_failed)
            },
        )
    }

    private fun openGroupChat(
        context: Context,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
    ) {
        val vo = GroupVo().apply {
            id = groupId
            if (!groupName.isNullOrBlank()) name = groupName
            if (!groupIcon.isNullOrBlank()) icon = groupIcon
        }
        JumpBroadUtils.startChatMsgActivity(groupToSession(vo), context)
    }

    /** 将 [GroupVo]（群详情）转为收费/入群所需的 [GroupQrInfoRes]。 */
    private fun toGroupQrInfo(
        vo: GroupVo,
        fallbackName: String?,
        fallbackIcon: String?,
    ): GroupQrInfoRes {
        val gson = AppGson.get()
        val info = gson.fromJson(gson.toJson(vo), GroupQrInfoRes::class.java) ?: GroupQrInfoRes()
        if (info.id.isNullOrBlank()) {
            info.id = vo.id
        }
        if (info.name.isNullOrBlank() && !fallbackName.isNullOrBlank()) {
            info.name = fallbackName
        }
        if (info.icon.isNullOrBlank() && !fallbackIcon.isNullOrBlank()) {
            info.icon = fallbackIcon
        }
        return info
    }

    /** 用 checkGroupJoin 返回的价格等字段补全群详情，供收费进群页展示。 */
    private fun mergeCheckGroupJoinIntoQrInfo(groupInfo: GroupQrInfoRes, check: CheckGroupJoiBean) {
        if (!check.groupName.isNullOrBlank()) {
            groupInfo.name = check.groupName
        }
        if (!check.groupIcon.isNullOrBlank()) {
            groupInfo.icon = check.groupIcon
        }
        if (check.memberCount > 0) {
            groupInfo.memCount = check.memberCount
        }
        if (!check.priceDualx.isNullOrBlank()) {
            groupInfo.joinPayPrice = check.priceDualx
        }
        val payFlag = when {
            check.openJoinPay == "1" || check.needPay == "1" -> "1"
            else -> check.openJoinPay ?: groupInfo.openJoinPay
        }
        if (!payFlag.isNullOrBlank()) {
            groupInfo.openJoinPay = payFlag
            groupInfo.ensureBusiExt().apply {
                openJoinPay = payFlag
                if (!check.priceUsdt.isNullOrBlank()) {
                    joinPayPriceUsdt = check.priceUsdt
                }
                if (!check.feeRate.isNullOrBlank()) {
                    joinPayFeeRate = check.feeRate
                }
            }
        }
    }

    private fun isJoinNeedVerify(groupInfo: GroupVo?): Boolean {
        if (groupInfo == null) return false
        if (groupInfo.joinNeedVerify == 1) return true
        return "1" == groupInfo.busiExt?.openQyyz
    }
}
