package com.freddy.chat.card

import android.app.Activity
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.freddy.chat.im.MessageBuilder
import com.freddy.chat.im.MessageProcessor
import java.util.concurrent.atomic.AtomicInteger

/**
 * 向好友单聊发送群邀请卡片（不调用 addMembers）。
 */
object GroupInviteMessageSender {

    fun sendToFriends(
        activity: Activity,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        friendUserIds: List<String>,
        onAllDone: (() -> Unit)? = null,
    ) {
        val targets = friendUserIds.map { it.trim() }.filter { it.isNotEmpty() }
        if (groupId.isEmpty() || targets.isEmpty()) {
            onAllDone?.invoke()
            return
        }
        val fromName = MMKVBroadUtils.getUserName()?.takeIf { it.isNotBlank() }
            ?: MMKVBroadUtils.getRealUserId().orEmpty()
        val remaining = AtomicInteger(targets.size)
        targets.forEach { uid ->
            val message = MessageBuilder.buildGroupInviteMessage(
                null,
                uid,
                groupId,
                groupName,
                groupIcon,
                fromName,
            )
            MessageProcessor.getInstance().sendMsg(message, {
                if (remaining.decrementAndGet() <= 0) {
                    finishOnUi(activity, onAllDone)
                }
            }, true)
        }
    }

    private fun finishOnUi(activity: Activity, onAllDone: (() -> Unit)?) {
        if (activity.isFinishing) return
        activity.runOnUiThread {
            onAllDone?.invoke()
        }
    }
}
