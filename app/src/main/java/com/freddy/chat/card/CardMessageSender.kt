package com.freddy.chat.card

import android.app.Activity
import com.freddy.chat.im.MessageBuilder
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.MessageType

/**
 * 发送个人名片（自研 [MessageProcessor.sendMsg]）。
 */
object CardMessageSender {

    fun send(
        activity: Activity,
        isGroupChat: Boolean,
        toId: String,
        sessionId: String?,
        cardUserId: String,
        displayName: String,
        portrait: String?,
        idno: String?,
    ) {
        val conversationType = if (isGroupChat) {
            MessageType.GROUP_CHAT.msgType
        } else {
            MessageType.SINGLE_CHAT.msgType
        }
        val message = MessageBuilder.buildCardMessage(
            sessionId,
            conversationType,
            toId,
            cardUserId,
            displayName,
            portrait,
            idno,
        )
        MessageProcessor.getInstance().sendMsg(message, {
            if (!activity.isFinishing) {
                activity.runOnUiThread { activity.finish() }
            }
        }, true)
    }
}
