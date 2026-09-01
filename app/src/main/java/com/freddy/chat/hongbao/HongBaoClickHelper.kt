package com.freddy.chat.hongbao

import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.OKGoBroadHelper
import com.freddy.chat.utils.HongBaoMessageContentUtil
import com.freddy.chat.viewholder.MessageViewHolderContext
import com.freddy.im.bean.AppMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * 自研红包气泡点击：查 hbInfo / dhbInfo 后跳转 GetWebx（对齐 MyImManagerBroadHolder.hbInfo）。
 */
object HongBaoClickHelper {

    @JvmStatic
    fun open(context: Context?, message: AppMessage, vhContext: MessageViewHolderContext?) {
        if (context == null) {
            return
        }
        val content = message.content
        val hongbaoId = HongBaoMessageContentUtil.resolveHongbaoId(content)
        if (TextUtils.isEmpty(hongbaoId)) {
            return
        }
        var lingStatus = HongBaoMessageContentUtil.resolveLingquStatus(content)
        val lqUsers = HongBaoMessageContentUtil.resolveLqUsers(content)
        val currentUserId = vhContext?.currentUserId ?: MMKVBroadUtils.getRealUserId()
        if (lingStatus != "1"
            && !TextUtils.isEmpty(currentUserId)
            && lqUsers.contains(currentUserId)
        ) {
            lingStatus = "1"
        }
        if (lingStatus == "3") {
            return
        }
        val isGroup = vhContext?.isGroupChat == true
        val payload = buildPayloadJson(content)
        val localKey = resolveLocalKey(message)
        val sessionId = vhContext?.sessionId ?: message.conversationId
        val msgTarget = resolveMsgTarget(message, vhContext, isGroup, currentUserId, sessionId)

        if (lingStatus == HongBaoMessageContentUtil.LINGQU_STATUS_ALL_CLAIMED) {
            openDetailPage(
                context, hongbaoId, isGroup, true, payload, localKey,
                message, msgTarget, sessionId,
            )
            return
        }

        if (lingStatus == "2") {
            openDetailPage(
                context, hongbaoId, isGroup, true, payload, localKey,
                message, msgTarget, sessionId,
            )
            return
        }

        queryHbInfo(
            context = context,
            hongbaoId = hongbaoId,
            isGroup = isGroup,
            showLoading = true,
            onResult = { data ->
                handleHbInfoResult(
                    context = context,
                    data = data,
                    message = message,
                    hongbaoId = hongbaoId,
                    isGroup = isGroup,
                    currentUserId = currentUserId,
                    lqUsers = lqUsers,
                    payload = payload,
                    localKey = localKey,
                    msgTarget = msgTarget,
                    sessionId = sessionId,
                )
            },
        )
    }

    private fun handleHbInfoResult(
        context: Context,
        data: CommonResultBroadBean.CommonResult,
        message: AppMessage,
        hongbaoId: String,
        isGroup: Boolean,
        currentUserId: String?,
        lqUsers: List<String>,
        payload: String,
        localKey: String,
        msgTarget: String?,
        sessionId: String?,
    ) {
        when {
            data.isLing == "1" -> {
                HongBaoLocalUpdater.applyLingquStatus(localKey, "1")
                openDetailPage(
                    context, hongbaoId, isGroup, true, payload, localKey,
                    message, msgTarget, sessionId,
                )
            }
            data.tuiStatus == 1 -> {
                HongBaoLocalUpdater.applyLingquStatus(localKey, "3")
                ToastBroadUtils.show(context.getString(R.string.string_hongbao_expired))
            }
            data.tuiStatus == 2 -> {
                HongBaoLocalUpdater.applyLingquStatus(localKey, "2")
            }
            HongBaoMessageContentUtil.shouldMarkAllClaimedByOthers(
                isGroup,
                data.isLing,
                currentUserId,
                lqUsers,
                data.ylq,
                data.count,
            ) -> {
                HongBaoLocalUpdater.applyLingquStatus(
                    localKey,
                    HongBaoMessageContentUtil.LINGQU_STATUS_ALL_CLAIMED,
                )
                openDetailPage(
                    context, hongbaoId, isGroup, true, payload, localKey,
                    message, msgTarget, sessionId,
                )
            }
            else -> {
                val isReceived = !isGroup && TextUtils.equals(message.fromId, currentUserId)
                openDetailPage(
                    context, hongbaoId, isGroup, isReceived, payload, localKey,
                    message, msgTarget, sessionId,
                )
            }
        }
    }

    private fun queryHbInfo(
        context: Context,
        hongbaoId: String,
        isGroup: Boolean,
        showLoading: Boolean,
        onResult: (CommonResultBroadBean.CommonResult) -> Unit,
    ) {
        val map = JSONObject()
        try {
            map.put("id", hongbaoId)
        } catch (_: Exception) {
        }
        OKGoBroadHelper().post(
            context,
            if (isGroup) "/hbInfo" else "/dhbInfo",
            map.toString(),
            CommonResultBroadBean(),
            showToast = false,
            showLoading = showLoading,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    val data = (bean as? CommonResultBroadBean)?.data ?: return
                    onResult(data)
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    private fun openDetailPage(
        context: Context,
        hongbaoId: String,
        isGroup: Boolean,
        isReceived: Boolean,
        payload: String,
        localKey: String,
        message: AppMessage,
        msgTarget: String?,
        sessionId: String?,
    ) {
        JumpBroadUtils.toGetWebxRedEnvelopesActivity(
            context,
            hongbaoId,
            isGroup,
            isReceived,
            payload,
            localKey,
            message.timestamp,
            msgTarget,
            message.fromId,
            0L,
            localKey,
            sessionId,
        )
    }

    private fun resolveLocalKey(message: AppMessage): String {
        return if (!TextUtils.isEmpty(message.msgId)) {
            message.msgId
        } else {
            message.clientMsgId ?: ""
        }
    }

    private fun resolveMsgTarget(
        message: AppMessage,
        vhContext: MessageViewHolderContext?,
        isGroup: Boolean,
        currentUserId: String?,
        sessionId: String?,
    ): String? {
        return when {
            isGroup -> if (!TextUtils.isEmpty(sessionId)) sessionId else message.toId
            vhContext != null && !TextUtils.isEmpty(vhContext.peerUserId) -> vhContext.peerUserId
            else -> if (message.isSender(currentUserId)) message.toId else message.fromId
        }
    }

    private fun buildPayloadJson(content: String?): String {
        return try {
            val obj = JSONObject()
            obj.put("type", "1000")
            obj.put("hongbaoId", HongBaoMessageContentUtil.resolveHongbaoId(content))
            obj.put("price", HongBaoMessageContentUtil.resolvePrice(content))
            obj.put("hongbaoDesc", HongBaoMessageContentUtil.resolveHongbaoDesc(content))
            obj.put("lingquStatus", HongBaoMessageContentUtil.resolveLingquStatus(content))
            obj.put("hongbaoLeixing", HongBaoMessageContentUtil.resolveHongbaoLeixing(content))
            obj.put("zhuanshuUserId", HongBaoMessageContentUtil.resolveZhuanshuUserId(content))
            obj.put("zhuanshuUserName", HongBaoMessageContentUtil.resolveZhuanshuUserName(content))
            obj.put("zhuanshuUserAvatar", HongBaoMessageContentUtil.resolveZhuanshuUserAvatar(content))
            val arr = JSONArray()
            val users = HongBaoMessageContentUtil.resolveLqUsers(content)
            for (i in users.indices) {
                arr.put(i, users[i])
            }
            obj.put("lqUsers", arr)
            obj.toString()
        } catch (_: Exception) {
            content ?: ""
        }
    }
}
