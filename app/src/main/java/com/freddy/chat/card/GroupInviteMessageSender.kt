package com.freddy.chat.card

import android.app.Activity
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.freddy.chat.group.GroupJoinCheckHelper
import com.freddy.chat.im.MessageBuilder
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.MessageType
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.ImUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

/**
 * 邀请好友进群：
 * - 非收费群且好友不在黑名单 → [ImGroupHttpExample.requestAddMembers] 直接拉人
 * - 收费群 → 单聊发送邀请卡片
 * （选人页已拦截黑名单好友选中；此处再兜底一次）
 */
object GroupInviteMessageSender {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * @param invitees userId → 展示名（备注优先）
     */
    fun sendToFriends(
        activity: Activity,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        groupPeopleCount: String?,
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
        scope.launch {
            val detail = awaitGroupDetail(groupId)
            val idno = detail?.busiExt?.idno?.takeIf { it.isNotBlank() }
            val name = groupName?.takeIf { it.isNotBlank() } ?: detail?.name
            val icon = groupIcon?.takeIf { it.isNotBlank() } ?: detail?.icon
//            val isPaid = isJoinPayOpen(detail)
//
//            if (isPaid) {
            dispatchInvites(activity, groupId, name, icon, idno, groupPeopleCount, targets, onAllDone)
//                return@launch
//            }
//
//            val blackIds = GroupJoinCheckHelper.fetchBlacklistedUserIds(
//                groupId,
//                targets.map { it.first },
//            )
//            // 非收费：黑名单好友跳过（选人页已提示）；其余直接拉人
//            val directTargets = targets.filter { it.first !in blackIds }
//            if (directTargets.isEmpty()) {
//                finishOnUi(activity, onAllDone)
//                return@launch
//            }
//            addMembersDirect(groupId, directTargets.map { it.first }) {
//                finishOnUi(activity, onAllDone)
//            }
        }
    }

    private fun isJoinPayOpen(vo: GroupVo?): Boolean {
        if (vo == null) return false
        val flag = vo.busiExt?.openJoinPay?.takeIf { it.isNotBlank() } ?: vo.openJoinPay
        return flag == "1"
    }

    private suspend fun awaitGroupDetail(groupId: String): GroupVo? =
        suspendCancellableCoroutine { cont ->
            ImGroupHttpExample.requestGetGroupDetail(
                groupId,
                object : OnHttpImListener<GroupVo> {
                    override fun onHttpImSuccess(result: GroupVo) {
                        if (cont.isActive) cont.resume(result)
                    }
                },
                {
                    if (cont.isActive) cont.resume(null)
                },
            )
        }

    private fun addMembersDirect(
        groupId: String,
        memberIds: List<String>,
        onDone: () -> Unit,
    ) {
        if (memberIds.isEmpty()) {
            onDone()
            return
        }
        ImGroupHttpExample.requestAddMembers(
            groupId,
            memberIds,
            object : OnHttpImListener<GroupVo> {
                override fun onHttpImSuccess(result: GroupVo) {
                    onDone()
                }
            },
            { onDone() },
        )
    }

    private fun dispatchInvites(
        activity: Activity,
        groupId: String,
        groupName: String?,
        groupIcon: String?,
        groupIdno: String?,
        groupPeopleCount: String?,
        targets: List<Pair<String, String>>,
        onAllDone: (() -> Unit)?,
    ) {
        if (targets.isEmpty()) {
            onAllDone?.invoke()
            return
        }
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
                groupPeopleCount
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
