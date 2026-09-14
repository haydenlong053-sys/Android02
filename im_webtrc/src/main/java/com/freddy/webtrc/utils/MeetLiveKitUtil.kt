package com.freddy.webtrc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.text.TextUtils
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.toLog
import com.freddy.webtrc.bean.CallEvent
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 群语音会议室专用 LiveKit Room（与 [CallUtil] 1v1/群通话房间隔离）。
 */
object MeetLiveKitUtil {
    private const val TAG = "MeetLiveKitUtil"

    @SuppressLint("StaticFieldLeak")
    private var meetRoom: Room? = null
    private var eventJob: Job? = null
    private var speakerphoneEnabled: Boolean = true

    private fun getOrCreateRoom(): Room {
        val existing = meetRoom
        if (existing != null) {
            return existing
        }
        val created = LiveKit.create(UtilBroadApplication.cxt)
        meetRoom = created
        return created
    }

    fun isRoomConnected(): Boolean {
        return try {
            meetRoom?.state == Room.State.CONNECTED
        } catch (e: Exception) {
            e.toLog(TAG)
            false
        }
    }

    fun joinRoom(
        url: String?,
        token: String?,
        canPublish: Boolean,
        scope: CoroutineScope? = null,
        onEvent: ((CallEvent) -> Unit)? = null,
    ) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(token)) {
            return
        }
        val runner = scope ?: GlobalScope
        runner.launch {
            withContext(Dispatchers.IO) {
                try {
                    val room = getOrCreateRoom()
                    if (room.state == Room.State.CONNECTED) {
                        room.disconnect()
                    }
                    room.connect(url.orEmpty(), token.orEmpty())
                    room.localParticipant.setMicrophoneEnabled(canPublish)
                    applySpeakerphone(speakerphoneEnabled)
                    onEvent?.let { listenEvents(runner, it) }
                } catch (e: Exception) {
                    e.toLog(TAG)
                    onEvent?.invoke(CallEvent.FailedToConnect(e.message))
                }
            }
        }
    }

    /** 上麦/下麦后换 token：断开再进同一房间。 */
    fun reconnect(
        url: String?,
        token: String?,
        canPublish: Boolean,
        scope: CoroutineScope? = null,
        onEvent: ((CallEvent) -> Unit)? = null,
    ) {
        leaveRoom()
        joinRoom(url, token, canPublish, scope, onEvent)
    }

    fun setMicrophoneEnabled(enabled: Boolean, scope: CoroutineScope? = null) {
        val runner = scope ?: GlobalScope
        runner.launch(Dispatchers.IO) {
            try {
                meetRoom?.localParticipant?.setMicrophoneEnabled(enabled)
            } catch (e: Exception) {
                e.toLog(TAG)
            }
        }
    }

    fun isSpeakerphoneEnabled(): Boolean = speakerphoneEnabled

    fun setEnableSpeakerphone(enabled: Boolean) {
        speakerphoneEnabled = enabled
        applySpeakerphone(enabled)
    }

    private fun applySpeakerphone(enabled: Boolean) {
        try {
            val am = UtilBroadApplication.cxt.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return
            am.mode = AudioManager.MODE_IN_COMMUNICATION
            am.isSpeakerphoneOn = enabled
        } catch (e: Exception) {
            e.toLog(TAG)
        }
    }

    fun leaveRoom() {
        try {
            eventJob?.cancel()
            eventJob = null
            meetRoom?.disconnect()
        } catch (e: Exception) {
            e.toLog(TAG)
        }
    }

    fun destroyRoom() {
        leaveRoom()
        try {
            meetRoom = null
        } catch (e: Exception) {
            e.toLog(TAG)
        }
    }

    fun observeRoomEvents(scope: CoroutineScope? = null, onEvent: (CallEvent) -> Unit) {
        listenEvents(scope ?: GlobalScope, onEvent)
    }

    private fun listenEvents(scope: CoroutineScope, onEvent: (CallEvent) -> Unit) {
        eventJob?.cancel()
        eventJob = scope.launch(Dispatchers.IO) {
            try {
                getOrCreateRoom().events.events.collect { event ->
                    onEvent(event.toCallEvent())
                }
            } catch (e: Exception) {
                e.toLog(TAG)
            }
        }
    }

    private fun RoomEvent.toCallEvent(): CallEvent {
        return when (this) {
            is RoomEvent.Connected -> CallEvent.Connected
            is RoomEvent.ParticipantConnected -> CallEvent.ParticipantConnected(identityOf(participant))
            is RoomEvent.ParticipantDisconnected -> CallEvent.ParticipantDisconnected(identityOf(participant))
            is RoomEvent.TrackSubscribed -> CallEvent.TrackSubscribed(identityOf(participant))
            is RoomEvent.TrackUnsubscribed -> CallEvent.TrackUnsubscribed(identityOf(participant))
            is RoomEvent.ActiveSpeakersChanged -> {
                CallEvent.ActiveSpeakersChanged(
                    speakers.mapNotNull { it.identity?.value?.takeIf { id -> id.isNotBlank() } }
                )
            }
            is RoomEvent.Disconnected -> CallEvent.Disconnected
            is RoomEvent.FailedToConnect -> CallEvent.FailedToConnect(error.message)
            else -> CallEvent.UnKnownEvent
        }
    }

    private fun identityOf(participant: Participant?): String {
        return participant?.identity?.value.orEmpty()
    }
}
