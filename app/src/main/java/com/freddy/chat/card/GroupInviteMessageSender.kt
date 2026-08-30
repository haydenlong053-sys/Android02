package com.freddy.chat.card

import android.app.Activity
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.freddy.chat.im.MessageBuilder
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.MessageType
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.ImUtils
import java.util.concurrent.atomic.AtomicInteger

/**
 * 向好友单聊发送群邀请卡片（不调用 addMembers）。
 */
object GroupInviteMessageSender {

    /**
     * @param invitees userId → 展示名（备注优先）
     */
    fun sendToFriends(
        activity: Activity,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        invitees: Map<String, String>,
        onAllDone: (() -> Unit)? = null,
    ) {
        val targets = invitees.entries
            .map { it.key.trim() to it.value.trim() }
            .filter { it.first.isNotEmpty() }
        if (groupId.isEmpty() || targets.isEmpty()) {
            onAllDone?.invoke()
            return
        }
        // 先拉群详情，携带显示用群号 idno
        ImGroupHttpExample.requestGetGroupDetail(
            groupId,
            object : OnHttpImListener<GroupVo> {
                override fun onHttpImSuccess(result: GroupVo) {
                    val idno = result.busiExt?.idno?.takeIf { it.isNotBlank() }
                    val name = groupName?.takeIf { it.isNotBlank() } ?: result.name
                    val icon = groupIcon?.takeIf { it.isNotBlank() } ?: result.icon
                    dispatchInvites(activity, groupId, name, icon, idno, targets, onAllDone)
                }
            },
            {
                // 详情失败仍发送，但无显示群号
                dispatchInvites(activity, groupId, groupName, groupIcon, null, targets, onAllDone)
            },
        )
    }

    private fun dispatchInvites(
        activity: Activity,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        groupIdno: String?,
        targets: List<Pair<String, String>>,
        onAllDone: (() -> Unit)?,
    ) {
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        val fromName = MMKVBroadUtils.getUserName()?.takeIf { it.isNotBlank() }
            ?: selfId
        val remaining = AtomicInteger(targets.size)
        val sessionSnapshots = ArrayList<SessionItem>(targets.size)
        targets.forEach { (uid, displayName) ->
            val peerName = displayName.ifBlank { uid }
            val sessionId = ImUtils.getP2pSessionIdId(selfId, uid)
            if (sessionId.isNotEmpty()) {
                sessionSnapshots.add(
                    SessionItem().apply {
                        this.sessionId = sessionId
                        conversation = MessageType.SINGLE_CHAT.msgType
                        oppositeUserId = uid
                        sessionName = peerName
                    },
                )
            }
        }
        if (sessionSnapshots.isNotEmpty()) {
            ImLocalStore.saveSessions(sessionSnapshots)
        }
        targets.forEach { (uid, displayName) ->
            val peerName = displayName.ifBlank { uid }
            val sessionId = ImUtils.getP2pSessionIdId(selfId, uid)
            val message = MessageBuilder.buildGroupInviteMessage(
                sessionId.takeIf { it.isNotEmpty() },
                uid,
                groupId,
                groupName,
                groupIcon,
                groupIdno,
                fromName,
                peerName,
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
