package com.freddy.webtrc.floatcall

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.freddy.webtrc.activity.CallActivity
import com.freddy.webtrc.utils.CallNetworkQualityHelper
import com.freddy.webtrc.utils.CallUtil
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 自研通话缩小到悬浮窗后，Activity 会 finish，LiveKit 房间与通话状态由此保活。
 */
object CallSessionHolder {

    private const val TAG = "CallSessionHolder"
    /** 断网重连补偿：超过此时长仍未恢复则自动挂断。 */
    const val RECONNECT_GRACE_MS = 30_000L

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    var active: Boolean = false
        private set

    /** 正在为悬浮窗 finish Activity，onDestroy 时不要 leaveRoom。 */
    @Volatile
    var keepRoomOnDestroy: Boolean = false

    /**
     * LiveKit 正在重连。full reconnect 会短暂抛出 ParticipantDisconnected，
     * Activity / 悬浮窗据此挂断会造成断网立刻结束；重连结束前应忽略。
     */
    @Volatile
    var roomReconnecting: Boolean = false

    var targetId: String = ""
    var participantIds: ArrayList<String> = arrayListOf()
    var groupId: String = ""
    var callId: String = ""
    var roomName: String = ""
    var callUrl: String = ""
    var callToken: String = ""
    var video: Boolean = false
    var outgoing: Boolean = true
    var isMultiCall: Boolean = false

    var muted: Boolean = false
    var speakerOn: Boolean = true
    var callConnected: Boolean = false
    var connectedAtMs: Long = 0L
    var openedAtMs: Long = 0L
    var leaveAsTimeout: Boolean = false

    val connectedParticipantIds: MutableSet<String> = CopyOnWriteArraySet()

    /** 当前前台（通话页 / 悬浮窗）提供的挂断实现。 */
    @Volatile
    private var hangupAction: (() -> Unit)? = null

    /** Disconnected 落在补偿期内时，延后到补偿结束再挂断。 */
    @Volatile
    private var deferredHangupAction: (() -> Unit)? = null

    private var reconnectGraceRunnable: Runnable? = null

    /** 对端暂时离房（对方断网全量重连）补偿：超时仍未回来再挂断。 */
    @Volatile
    var peerTemporarilyGone: Boolean = false
        private set

    private var peerGoneGraceRunnable: Runnable? = null
    private var peerGoneHangupAction: (() -> Unit)? = null

    fun bindHangupAction(action: (() -> Unit)?) {
        hangupAction = action
    }

    /**
     * 进入重连：启动 30s 补偿（仅首次）。超时仍未 [onRoomReconnected] 则自动挂断。
     */
    fun onRoomReconnecting() {
        roomReconnecting = true
        scheduleReconnectGraceHangup()
    }

    /**
     * 本端系统断网。LiveKit 往往要等网络恢复才抛 Reconnecting，补偿定时器不会启动；
     * 因此在系统 onLost 时主动启动同一套 30s 超时挂断。
     */
    fun onLocalNetworkLost() {
        eLogD(TAG, "本端系统断网，启动断网重连补偿 ${RECONNECT_GRACE_MS}ms")
        scheduleReconnectGraceHangup()
    }

    /**
     * 本端网络恢复后的探测：若 LiveKit 仍显示已连接且未进入重连，说明只是瞬时抖动，取消补偿。
     * 若已进入 [roomReconnecting]，交给 [onRoomReconnected] 取消。
     */
    fun onLocalNetworkRestoredCheck() {
        if (reconnectGraceRunnable == null) {
            return
        }
        if (roomReconnecting) {
            eLogD(TAG, "网络已恢复但仍在 LiveKit 重连中，继续等待 Reconnected / 超时")
            return
        }
        val connected = try {
            CallUtil.isRoomConnected() && CallUtil.hasRemoteParticipant()
        } catch (_: Exception) {
            false
        }
        if (connected) {
            eLogD(TAG, "网络已恢复且房间仍连通，取消断网补偿")
            cancelReconnectGrace(clearDeferred = true)
        } else {
            eLogD(TAG, "网络已恢复但房间未就绪，继续等待 Reconnected / 超时")
        }
    }

    /** 重连成功：取消补偿定时器。 */
    fun onRoomReconnected() {
        roomReconnecting = false
        cancelReconnectGrace(clearDeferred = true)
    }

    /**
     * 房间已断开。若仍在 30s 补偿期内则延后挂断；否则立即执行 [hangupNow]。
     * @return true 表示已延后，调用方勿立刻拆 UI
     */
    fun onRoomDisconnectedOrDefer(hangupNow: () -> Unit): Boolean {
        roomReconnecting = false
        if (reconnectGraceRunnable != null) {
            deferredHangupAction = hangupNow
            eLogD(TAG, "Disconnected 处于 ${RECONNECT_GRACE_MS}ms 补偿期内，延后挂断")
            return true
        }
        cancelReconnectGrace(clearDeferred = true)
        return false
    }

    fun isReconnectGraceActive(): Boolean = reconnectGraceRunnable != null

    fun isPeerGoneGraceActive(): Boolean = peerGoneGraceRunnable != null

    /**
     * 对端 ParticipantDisconnected（非本端重连清远端）：提示对方网络不佳，并启动补偿等待其重连回来。
     */
    fun onPeerTemporarilyGone(hangupIfTimeout: () -> Unit) {
        CallNetworkQualityHelper.onRemoteNetworkIssue("peer-temporarily-gone")
        peerTemporarilyGone = true
        peerGoneHangupAction = hangupIfTimeout
        if (peerGoneGraceRunnable != null) {
            eLogD(TAG, "对端离房补偿已在进行中，刷新挂断回调")
            return
        }
        val r = Runnable {
            peerGoneGraceRunnable = null
            peerTemporarilyGone = false
            eLogD(TAG, "对端离房补偿 ${RECONNECT_GRACE_MS}ms 超时，自动挂断")
            val action = peerGoneHangupAction
            peerGoneHangupAction = null
            action?.invoke()
        }
        peerGoneGraceRunnable = r
        eLogD(TAG, "启动对端离房补偿 ${RECONNECT_GRACE_MS}ms")
        mainHandler.postDelayed(r, RECONNECT_GRACE_MS)
    }

    /** 对端重新进房 / 重新订阅轨道：取消对端离房补偿。 */
    fun onPeerBack() {
        if (peerGoneGraceRunnable == null && !peerTemporarilyGone) {
            return
        }
        eLogD(TAG, "对端已恢复，取消离房补偿")
        peerTemporarilyGone = false
        peerGoneGraceRunnable?.let { mainHandler.removeCallbacks(it) }
        peerGoneGraceRunnable = null
        peerGoneHangupAction = null
    }

    private fun cancelPeerGoneGrace() {
        peerTemporarilyGone = false
        peerGoneGraceRunnable?.let { mainHandler.removeCallbacks(it) }
        peerGoneGraceRunnable = null
        peerGoneHangupAction = null
    }

    private fun scheduleReconnectGraceHangup() {
        if (reconnectGraceRunnable != null) {
            eLogD(TAG, "断网重连补偿定时器已存在，跳过重复启动")
            return
        }
        val r = Runnable {
            reconnectGraceRunnable = null
            roomReconnecting = false
            eLogD(TAG, "断网重连补偿 ${RECONNECT_GRACE_MS}ms 超时，自动挂断")
            val action = deferredHangupAction ?: hangupAction
            deferredHangupAction = null
            if (action != null) {
                action.invoke()
            } else {
                eLogD(TAG, "补偿超时但 hangupAction 为空，强制 leaveRoom + clear")
                try {
                    CallUtil.leaveRoom()
                } catch (_: Exception) {
                }
                clear()
            }
        }
        reconnectGraceRunnable = r
        eLogD(TAG, "启动断网重连补偿 ${RECONNECT_GRACE_MS}ms")
        mainHandler.postDelayed(r, RECONNECT_GRACE_MS)
    }

    private fun cancelReconnectGrace(clearDeferred: Boolean) {
        reconnectGraceRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectGraceRunnable = null
        if (clearDeferred) {
            deferredHangupAction = null
        }
    }

    fun saveFrom(activity: CallActivity) {
        targetId = activity.exportTargetId()
        participantIds = ArrayList(activity.exportParticipantIds())
        groupId = activity.exportGroupId()
        callId = activity.exportCallId()
        roomName = activity.exportRoomName()
        callUrl = activity.exportCallUrl()
        callToken = activity.exportCallToken()
        video = activity.exportVideo()
        outgoing = activity.exportOutgoing()
        isMultiCall = activity.exportIsMultiCall()
        muted = activity.exportMuted()
        speakerOn = activity.exportSpeakerOn()
        callConnected = activity.exportCallConnected()
        connectedAtMs = activity.exportConnectedAtMs()
        openedAtMs = activity.exportOpenedAtMs()
        leaveAsTimeout = false
        connectedParticipantIds.clear()
        connectedParticipantIds.addAll(activity.exportConnectedParticipantIds())
        active = true
    }

    fun fillIntent(intent: Intent) {
        intent.putExtra(CallActivity.EXTRA_TARGET_ID, targetId)
        intent.putStringArrayListExtra(CallActivity.EXTRA_PARTICIPANT_IDS, ArrayList(participantIds))
        intent.putExtra(CallActivity.EXTRA_GROUP_ID, groupId)
        intent.putExtra(CallActivity.EXTRA_CALL_ID, callId)
        intent.putExtra(CallActivity.EXTRA_ROOM_NAME, roomName)
        intent.putExtra(CallActivity.EXTRA_URL, callUrl)
        intent.putExtra(CallActivity.EXTRA_TOKEN, callToken)
        intent.putExtra(CallActivity.EXTRA_VIDEO, video)
        intent.putExtra(CallActivity.EXTRA_OUTGOING, outgoing)
        intent.putExtra(CallActivity.EXTRA_FROM_FLOATING_VIEW, true)
    }

    fun matches(matchId: String?): Boolean {
        if (!active || matchId.isNullOrBlank()) return false
        return TextUtils.equals(matchId, callId) || TextUtils.equals(matchId, roomName)
    }

    fun markRemoteConnected(fromUserId: String?) {
        if (!active) return
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        if (!fromUserId.isNullOrBlank() && fromUserId != selfId) {
            connectedParticipantIds.add(fromUserId)
            if (!participantIds.contains(fromUserId)) {
                participantIds.add(fromUserId)
            }
        }
        if (!callConnected) {
            callConnected = true
            connectedAtMs = System.currentTimeMillis()
            CallNetworkQualityHelper.onCallConnected()
        }
    }

    fun removeParticipant(fromUserId: String?) {
        if (fromUserId.isNullOrBlank()) return
        participantIds.remove(fromUserId)
        connectedParticipantIds.remove(fromUserId)
    }

    fun shouldEndMultiCall(): Boolean {
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        val remaining = participantIds.count { it.isNotBlank() && it != selfId }
        return remaining <= 0
    }

    fun remoteAnsweredCount(): Int {
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        return connectedParticipantIds.count { it.isNotBlank() && it != selfId }
    }

    fun clear() {
        cancelReconnectGrace(clearDeferred = true)
        cancelPeerGoneGrace()
        hangupAction = null
        active = false
        keepRoomOnDestroy = false
        roomReconnecting = false
        targetId = ""
        participantIds = arrayListOf()
        groupId = ""
        callId = ""
        roomName = ""
        callUrl = ""
        callToken = ""
        video = false
        outgoing = true
        isMultiCall = false
        muted = false
        speakerOn = true
        callConnected = false
        connectedAtMs = 0L
        openedAtMs = 0L
        leaveAsTimeout = false
        connectedParticipantIds.clear()
        CallNetworkQualityHelper.reset()
    }
}
