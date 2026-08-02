package com.freddy.webtrc.floatcall

import android.content.Intent
import android.text.TextUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.freddy.webtrc.activity.CallActivity
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 自研通话缩小到悬浮窗后，Activity 会 finish，LiveKit 房间与通话状态由此保活。
 */
object CallSessionHolder {

    @Volatile
    var active: Boolean = false
        private set

    /** 正在为悬浮窗 finish Activity，onDestroy 时不要 leaveRoom。 */
    @Volatile
    var keepRoomOnDestroy: Boolean = false

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
        active = false
        keepRoomOnDestroy = false
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
    }
}
