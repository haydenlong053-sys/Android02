package com.cboda.intelligent.holder

import android.app.Activity
import android.graphics.Bitmap
import android.text.TextUtils
import cn.wildfirechat.message.CardMessageContent
import cn.wildfirechat.message.HongBaoMessageContent
import cn.wildfirechat.message.LingQuMessageContent
import cn.wildfirechat.message.LocationMessageContent
import cn.wildfirechat.message.Message
import cn.wildfirechat.message.ZhuanZhangJieShouMessageContent
import cn.wildfirechat.message.ZhuanZhangMessageContent
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.SendMessageCallback
import com.blankj.utilcode.util.GsonUtils
import com.cboda.intelligent.utils.AppBroadUtils
import com.freddy.chat.hongbao.HongBaoMessageSender

class SendCustomBroadMsg {
    private val gson = GsonUtils.getGson()

    /** 自研会话传入 sessionId 时走 MessageProcessor */
    private fun useSelfIm(sessionId: String?): Boolean {
        return !TextUtils.isEmpty(sessionId)
    }


    @Deprecated("废弃")
    fun sendMyImZhuanZhang(
        isGroup: String,
        target: String,
        hongbaoDesc: String,
        hongbaoId: String,
        price: String,
        lingquStatus: String,
        activity: Activity
    ) {
        val cardMessageContent =
            ZhuanZhangMessageContent(hongbaoId, price, hongbaoDesc, lingquStatus)
        val conversation = Conversation()
        conversation.target = target
        conversation.type =
            if (isGroup == "1") Conversation.ConversationType.Group else Conversation.ConversationType.Single
        val msg = Message()
        msg.conversation = conversation
        msg.content = cardMessageContent
        msg.sender = ChatManager.Instance().userId
        ChatManager.Instance().sendMessage(msg, object : SendMessageCallback {
            override fun onSuccess(messageUid: Long, timestamp: Long) {
                AppBroadUtils.debug("野火", "转账消息发送 onSuccess-->")
                activity.finish()
            }

            override fun onFail(errorCode: Int) {
                AppBroadUtils.debug("野火", "转账消息发送 onFail-->$errorCode")
            }

            override fun onPrepare(messageId: Long, savedTime: Long) {
            }

        })
    }


//    fun sendMyImLingQu(
//        target: String,
//        tipType: String,
//        sendUserId: String,
//        sendUserName: String,
//        lingquUserId: String,
//        lingquUserName: String,
//        msgSender: String = "",
//        sessionId: String = ""
//    ) {
//        if (useSelfIm(sessionId)) {
//            HongBaoMessageSender.sendClaim(
//                isGroupChat = true,
//                toId = target,
//                sessionId = sessionId.ifEmpty { target },
//                tipType = tipType,
//                sendUserId = sendUserId,
//                sendUserName = sendUserName,
//                lingquUserId = lingquUserId,
//                lingquUserName = lingquUserName,
//            )
//        }
//    }


    fun sendMyImZhuanZhangJieShou(
        isGroup: String,
        target: String,
        hongbaoDesc: String,
        hongbaoId: String,
        price: String,
        lingquStatus: String,
        activity: Activity
    ) {
        val cardMessageContent =
            ZhuanZhangJieShouMessageContent(hongbaoId, price, hongbaoDesc, lingquStatus)
        val conversation = Conversation()
        conversation.target = target
        conversation.type =
            if (isGroup == "1") Conversation.ConversationType.Group else Conversation.ConversationType.Single
        val msg = Message()
        msg.conversation = conversation
        msg.content = cardMessageContent
        msg.sender = ChatManager.Instance().userId
        ChatManager.Instance().sendMessage(msg, object : SendMessageCallback {
            override fun onSuccess(messageUid: Long, timestamp: Long) {
                AppBroadUtils.debug("野火", "转账接受消息发送 onSuccess-->")
//                activity.finish()
            }

            override fun onFail(errorCode: Int) {
                AppBroadUtils.debug("野火", "转账接受消息发送 onFail-->$errorCode")
            }

            override fun onPrepare(messageId: Long, savedTime: Long) {
            }

        })
    }

    //暂不使用，由后端接口来发
    fun sendMyImHongBao(
        isGroup: String,
        target: String,
        hongbaoDesc: String,
        hongbaoId: String,
        price: String,
        lingquStatus: String,
        hongbaoLeixing: String,
        zhuanshuUserId: String,
        zhuanshuUserAvatar: String,
        zhuanshuUserName: String,
        activity: Activity,
        sessionId: String = ""
    ) {
        if (useSelfIm(sessionId)) {
            HongBaoMessageSender.sendRedPacket(
                activity = activity,
                isGroupChat = isGroup == "1",
                toId = target,
                sessionId = sessionId,
                hongbaoDesc = hongbaoDesc,
                hongbaoId = hongbaoId,
                price = price,
                lingquStatus = lingquStatus,
                hongbaoLeixing = hongbaoLeixing,
                zhuanshuUserId = zhuanshuUserId,
                zhuanshuUserAvatar = zhuanshuUserAvatar,
                zhuanshuUserName = zhuanshuUserName,
            )
        }
    }


}