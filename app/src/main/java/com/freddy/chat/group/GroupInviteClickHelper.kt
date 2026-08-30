package com.freddy.chat.group

import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.im.GroupJoinPayActivity
import com.cboda.intelligent.bean.CheckGroupJoiBean
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.showToastMain
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
                ToastBroadUtils.show(pair.second)
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
                    ToastBroadUtils.show(context.getString(R.string.scan_network_request_failed))
                    return@onSuccess
                }
                val groupInfo = buildGroupQrInfo(groupId, groupName, groupIcon, data)
                when {
                    data.isBlacklisted() -> {
                        inFlight.set(false)
                        showToastMain(R.string.scan_group_blacklisted)
                    }
                    data.isNeedPay() -> {
                        inFlight.set(false)
                        GroupJoinPayActivity.start(context, groupInfo)
                    }
                    data.isAllowJoin() || data.success -> {
                        applyJoinGroup(context, groupId, groupInfo)
                    }
                    else -> {
                        inFlight.set(false)
                        ToastBroadUtils.show(
                            data.msg?.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.scan_network_request_failed),
                        )
                    }
                }
            }.onFailure { _, message ->
                inFlight.set(false)
                ToastBroadUtils.show(
                    message.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.scan_network_request_failed),
                )
            }.onError {
                inFlight.set(false)
                ToastBroadUtils.show(context.getString(R.string.scan_network_request_failed))
            }
        }
    }

    private fun applyJoinGroup(context: Context, groupId: String, groupInfo: GroupQrInfoRes) {
        ImGroupHttpExample.requestApplyInGroup(
            groupId,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    inFlight.set(false)
                    if (!result) {
                        ToastBroadUtils.show(context.getString(R.string.scan_network_request_failed))
                        return
                    }
                    if (isJoinNeedVerify(groupInfo)) {
                        ToastBroadUtils.show(
                            context.getString(R.string.scan_applied_waiting_group_owner),
                        )
                    } else {
                        openGroupChat(context, groupId, groupInfo.name, groupInfo.icon)
                    }
                }
            },
            {
                inFlight.set(false)
                ToastBroadUtils.show(context.getString(R.string.scan_network_request_failed))
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

    private fun isJoinNeedVerify(groupInfo: GroupVo?): Boolean {
        if (groupInfo == null) return false
        if (groupInfo.joinNeedVerify == 1) return true
        return "1" == groupInfo.busiExt?.openQyyz
    }
}
