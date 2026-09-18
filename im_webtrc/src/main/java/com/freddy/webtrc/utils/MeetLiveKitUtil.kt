package com.freddy.webtrc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.text.TextUtils
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.APP_IM_TAG
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.freddy.webtrc.bean.CallEvent
import io.livekit.android.LiveKit
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.LocalAudioTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import livekit.org.webrtc.AudioTrackSink
import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * 群语音会议室专用 LiveKit Room（与 [CallUtil] 1v1/群通话房间隔离）。
 */
object MeetLiveKitUtil {
    private const val TAG = "MeetLiveKitUtil"
    private const val LOCAL_SPEAK_EMIT_INTERVAL_MS = 250L
    private const val LOCAL_SPEAK_RMS_THRESHOLD = 350.0

    @SuppressLint("StaticFieldLeak")
    private var meetRoom: Room? = null
    private var eventJob: Job? = null
    private var localSpeakJob: Job? = null
    private var speakerphoneEnabled: Boolean = true
    private var localAudioSink: AudioTrackSink? = null
    private var localAudioTrack: LocalAudioTrack? = null
    @Volatile
    private var lastLocalSpeakAt = 0L
    private var meetOnEvent: ((CallEvent) -> Unit)? = null

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
                    eLogD(APP_IM_TAG,"会议sdk真正加入房间canPublish=${canPublish}---url=$url---token=$token")
                    room.connect(url.orEmpty(), token.orEmpty())
                    room.localParticipant.setMicrophoneEnabled(canPublish)
                    applySpeakerphone(speakerphoneEnabled)
                    onEvent?.let {
                        meetOnEvent = it
                        listenEvents(runner, it)
                        listenLocalSpeaking(room, runner, it)
                        if (canPublish) {
                            attachLocalAudioLevelSink(room, it)
                        }
                    }
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
                val room = meetRoom
                room?.localParticipant?.setMicrophoneEnabled(enabled)
                val callback = meetOnEvent
                if (enabled && room != null && callback != null) {
                    attachLocalAudioLevelSink(room, callback)
                } else if (!enabled) {
                    detachLocalAudioLevelSink()
                }
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
            localSpeakJob?.cancel()
            localSpeakJob = null
            detachLocalAudioLevelSink()
            meetOnEvent = null
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
                val room = getOrCreateRoom()
                room.events.events.collect { event ->
                    if (event is RoomEvent.TrackPublished &&
                        event.participant is LocalParticipant &&
                        isMicrophonePublication(event.publication)
                    ) {
                        attachLocalAudioLevelSink(room, onEvent)
                    } else if (event is RoomEvent.TrackUnpublished &&
                        event.participant is LocalParticipant &&
                        isMicrophonePublication(event.publication)
                    ) {
                        detachLocalAudioLevelSink()
                    }
                    onEvent(event.toCallEvent())
                }
            } catch (e: Exception) {
                e.toLog(TAG)
            }
        }
    }

    /** 本端说话：房间 ActiveSpeakers 在主播本端经常不含自己，对齐声网 uid=0。 */
    private fun listenLocalSpeaking(room: Room, scope: CoroutineScope, onEvent: (CallEvent) -> Unit) {
        localSpeakJob?.cancel()
        localSpeakJob = scope.launch(Dispatchers.IO) {
            try {
                room.localParticipant.events.events.collect { event ->
                    when (event) {
                        is ParticipantEvent.LocalTrackPublished -> {
                            if (isMicrophonePublication(event.publication)) {
                                attachLocalAudioLevelSink(room, onEvent)
                            }
                        }
                        is ParticipantEvent.LocalTrackUnpublished -> {
                            if (isMicrophonePublication(event.publication)) {
                                detachLocalAudioLevelSink()
                            }
                        }
                        is ParticipantEvent.SpeakingChanged -> {
                            if (event.isSpeaking) {
                                emitLocalSpeaking(room, onEvent)
                            }
                        }
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                e.toLog(TAG)
            }
        }
    }

    private fun isMicrophonePublication(publication: TrackPublication): Boolean {
        return publication.kind == Track.Kind.AUDIO || publication.source == Track.Source.MICROPHONE
    }

    private fun attachLocalAudioLevelSink(room: Room, onEvent: (CallEvent) -> Unit) {
        val track = room.localParticipant.getTrackPublication(Track.Source.MICROPHONE)?.track as? LocalAudioTrack
            ?: return
        if (localAudioTrack === track && localAudioSink != null) {
            return
        }
        detachLocalAudioLevelSink()
        val sink = AudioTrackSink { audioData, bitsPerSample, _, _, numberOfFrames, _ ->
            if (!isLocalPcmSpeaking(audioData, bitsPerSample, numberOfFrames)) {
                return@AudioTrackSink
            }
            val now = System.currentTimeMillis()
            if (now - lastLocalSpeakAt < LOCAL_SPEAK_EMIT_INTERVAL_MS) {
                return@AudioTrackSink
            }
            lastLocalSpeakAt = now
            emitLocalSpeaking(room, onEvent)
        }
        localAudioSink = sink
        localAudioTrack = track
        try {
            track.addSink(sink)
        } catch (e: Exception) {
            e.toLog(TAG)
            localAudioSink = null
            localAudioTrack = null
        }
    }

    private fun detachLocalAudioLevelSink() {
        try {
            val sink = localAudioSink
            val track = localAudioTrack
            if (sink != null && track != null) {
                track.removeSink(sink)
            }
        } catch (e: Exception) {
            e.toLog(TAG)
        }
        localAudioSink = null
        localAudioTrack = null
    }

    private fun emitLocalSpeaking(room: Room, onEvent: (CallEvent) -> Unit) {
        val userId = identityOf(room.localParticipant)
        if (userId.isBlank()) {
            return
        }
        onEvent(CallEvent.LocalSpeakingChanged(userId, true))
    }

    private fun isLocalPcmSpeaking(audioData: ByteBuffer, bitsPerSample: Int, numberOfFrames: Int): Boolean {
        if (bitsPerSample != 16 || numberOfFrames <= 0) {
            return false
        }
        val copy = audioData.asReadOnlyBuffer()
        var sumSquares = 0.0
        var count = 0
        while (copy.remaining() >= 2) {
            val sample = copy.short.toInt()
            sumSquares += sample.toDouble() * sample
            count++
        }
        if (count == 0) {
            return false
        }
        return sqrt(sumSquares / count) > LOCAL_SPEAK_RMS_THRESHOLD
    }

    private fun RoomEvent.toCallEvent(): CallEvent {
        return when (this) {
            is RoomEvent.Connected -> CallEvent.Connected
            is RoomEvent.ParticipantConnected -> CallEvent.ParticipantConnected(identityOf(participant))
            is RoomEvent.ParticipantDisconnected -> CallEvent.ParticipantDisconnected(identityOf(participant))
            is RoomEvent.TrackSubscribed -> CallEvent.TrackSubscribed(identityOf(participant))
            is RoomEvent.TrackUnsubscribed -> CallEvent.TrackUnsubscribed(identityOf(participant))
            is RoomEvent.TrackMuted -> toRemoteAudioMuteEvent(participant, publication, muted = true)
            is RoomEvent.TrackUnmuted -> toRemoteAudioMuteEvent(participant, publication, muted = false)
            is RoomEvent.ActiveSpeakersChanged -> {
                CallEvent.ActiveSpeakersChanged(
                    speakers.mapNotNull { identityOf(it).takeIf { id -> id.isNotBlank() } }
                )
            }
            is RoomEvent.Disconnected -> CallEvent.Disconnected
            is RoomEvent.FailedToConnect -> CallEvent.FailedToConnect(error.message)
            else -> CallEvent.UnKnownEvent
        }
    }

    /** 仅转发远端麦克风 mute，对齐声网 onUserMuteAudio。 */
    private fun toRemoteAudioMuteEvent(
        participant: Participant,
        publication: TrackPublication,
        muted: Boolean,
    ): CallEvent {
        if (participant !is RemoteParticipant) {
            return CallEvent.UnKnownEvent
        }
        val isAudio = publication.kind == Track.Kind.AUDIO
                || publication.source == Track.Source.MICROPHONE
        if (!isAudio) {
            return CallEvent.UnKnownEvent
        }
        val userId = identityOf(participant)
        if (userId.isBlank()) {
            return CallEvent.UnKnownEvent
        }
        return CallEvent.RemoteAudioMuteChanged(userId, muted)
    }

    private fun identityOf(participant: Participant?): String {
        val identity = participant?.identity?.value
        if (!identity.isNullOrBlank()) {
            return identity
        }
        if (participant is LocalParticipant) {
            return MMKVBroadUtils.getRealUserId().orEmpty()
        }
        return participant?.sid?.value.orEmpty()
    }
}
