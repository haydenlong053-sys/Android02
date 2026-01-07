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
import com.google.gson.Gson
import com.cboda.intelligent.utils.AppBroadUtils

class SendCustomBroadMsg {
    private val gson = Gson()


    fun sendYeHuoMingPian(
        isGroup: String,
        uid: String,
        target: String,
        displayName: String,
        portrait: String,
        activity: Activity
    ) {
        val userInfo = ChatManager.Instance().getUserInfo(uid, false)
        val cardMessageContent =
            CardMessageContent(0, uid, displayName, portrait, ChatManager.Instance().userId)
        cardMessageContent.name = userInfo.name
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
                AppBroadUtils.debug("野火", "名片消息发送 onSuccess-->")
                activity.finish()
            }

            override fun onFail(errorCode: Int) {
                AppBroadUtils.debug("野火", "名片消息发送 onFail-->$errorCode")
            }

            override fun onPrepare(messageId: Long, savedTime: Long) {
            }

        })
    }

    fun sendYeHuoZhuanZhang(
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


    fun sendYeHuoLingQu(
        target: String,
        tipType: String,
        sendUserId: String,
        sendUserName: String,
        lingquUserId: String,
        lingquUserName: String,
        msgSender: String = ""
    ) {
        val lqMessageContent =
            LingQuMessageContent(tipType, sendUserId, sendUserName, lingquUserId, lingquUserName)
        val conversation = Conversation()
        conversation.target = target
        conversation.type =
            Conversation.ConversationType.Group
        val msg = Message()
        msg.conversation = conversation
        msg.content = lqMessageContent
        msg.sender = ChatManager.Instance().userId
        if (!TextUtils.isEmpty(msgSender)) {
            msg.toUsers = arrayOf(msgSender)
        }
        ChatManager.Instance().sendMessage(msg, object : SendMessageCallback {
            override fun onSuccess(messageUid: Long, timestamp: Long) {
                AppBroadUtils.debug("野火", "转账消息发送 onSuccess-->")
            }

            override fun onFail(errorCode: Int) {
                AppBroadUtils.debug("野火", "转账消息发送 onFail-->$errorCode")
            }

            override fun onPrepare(messageId: Long, savedTime: Long) {
            }

        })
    }


    fun sendYeHuoZhuanZhangJieShou(
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

    fun sendYeHuoHongBao(
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
        activity: Activity
    ) {
        val cardMessageContent = HongBaoMessageContent(
            hongbaoId,
            price,
            hongbaoDesc,
            lingquStatus,
            hongbaoLeixing,
            zhuanshuUserId,
            zhuanshuUserName,
            zhuanshuUserAvatar,
            arrayListOf<String>()
        )
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
                AppBroadUtils.debug("野火", "红包消息发送 onSuccess-->")
                activity.finish()
            }

            override fun onFail(errorCode: Int) {
                AppBroadUtils.debug("野火", "红包消息发送 onFail-->$errorCode")
            }

            override fun onPrepare(messageId: Long, savedTime: Long) {
            }

        })
    }


    fun sendYeHuoWeiZhi(
        isGroup: String,
        target: String,
        title: String,
        latitude: Double,
        longitude: Double,
        thumbnail: Bitmap,
        activity: Activity
    ) {
        val locCont = LocationMessageContent()
        locCont.title = title
        locCont.location.latitude = latitude
        locCont.location.longitude = longitude
        locCont.thumbnail = thumbnail


        val conversation = Conversation()
        conversation.target = target
        conversation.type =
            if (isGroup == "1") Conversation.ConversationType.Group else Conversation.ConversationType.Single
        val msg = Message()
        msg.conversation = conversation
        msg.content = locCont
        msg.sender = ChatManager.Instance().userId
        ChatManager.Instance().sendMessage(msg, object : SendMessageCallback {
            override fun onSuccess(messageUid: Long, timestamp: Long) {
                AppBroadUtils.debug("野火", "位置消息发送 onSuccess-->")
                activity.finish()
            }

            override fun onFail(errorCode: Int) {
                AppBroadUtils.debug("野火", "位置消息发送 onFail-->$errorCode")
            }

            override fun onPrepare(messageId: Long, savedTime: Long) {
            }

        })
    }

}