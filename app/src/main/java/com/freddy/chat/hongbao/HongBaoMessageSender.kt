package com.freddy.chat.hongbao

import android.app.Activity
import android.text.TextUtils
import com.freddy.chat.im.MessageBuilder
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.MessageType

/**
 * 发送红包气泡 / 群领取通知（自研 [MessageProcessor.sendMsg]）。
 */
object HongBaoMessageSender {

    //暂不使用，由后端接口来发
    fun sendRedPacket(
        activity: Activity?,
        isGroupChat: Boolean,
        toId: String,
        sessionId: String?,
        hongbaoDesc: String,
        hongbaoId: String,
        price: String,
        lingquStatus: String,
        hongbaoLeixing: String,
        zhuanshuUserId: String,
        zhuanshuUserAvatar: String,
        zhuanshuUserName: String,
        finishActivity: Boolean = true,
    ) {
        val conversationType = if (isGroupChat) {
            MessageType.GROUP_CHAT.msgType
        } else {
            MessageType.SINGLE_CHAT.msgType
        }
        val message = MessageBuilder.buildRedPacketMessage(
            sessionId,
            conversationType,
            toId,
            hongbaoId,
            price,
            hongbaoDesc,
            lingquStatus,
            hongbaoLeixing,
            zhuanshuUserId,
            zhuanshuUserName,
            zhuanshuUserAvatar,
            arrayListOf(),
        )
        MessageProcessor.getInstance().sendMsg(message, {
            if (finishActivity && activity != null && !activity.isFinishing) {
                activity.runOnUiThread { activity.finish() }
            }
        }, true)
    }

//    fun sendClaim(
//        isGroupChat: Boolean,
//        toId: String,
//        sessionId: String?,
//        tipType: String,
//        sendUserId: String,
//        sendUserName: String,
//        lingquUserId: String,
//        lingquUserName: String,
//    ) {
//        val conversationType = if (isGroupChat) {
//            MessageType.GROUP_CHAT.msgType
//        } else {
//            MessageType.SINGLE_CHAT.msgType
//        }
//        val resolvedSession = if (!TextUtils.isEmpty(sessionId)) sessionId else toId
//        val message = MessageBuilder.buildHongbaoClaimMessage(
//            resolvedSession,
//            conversationType,
//            toId,
//            tipType,
//            sendUserId,
//            sendUserName,
//            lingquUserId,
//            lingquUserName,
//        )
//        MessageProcessor.getInstance().sendMsg(message, null, true)
//    }
}
