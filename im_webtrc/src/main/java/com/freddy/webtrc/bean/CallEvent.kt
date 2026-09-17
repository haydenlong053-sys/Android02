package com.freddy.webtrc.bean

/**
 * 通话房间事件（app 内部模型，不暴露 LiveKit SDK 类型）。
 */
sealed class CallEvent {
    /** 本端已连上房间 */
    data object Connected : CallEvent()

    /** 有人进入房间 */
    data class ParticipantConnected(val userId: String) : CallEvent()

    /** 有人离开房间 */
    data class ParticipantDisconnected(val userId: String) : CallEvent()

    /** 收到对方音/视频轨 */
    data class TrackSubscribed(val userId: String) : CallEvent()

    /** 对方音/视频轨取消订阅 */
    data class TrackUnsubscribed(val userId: String) : CallEvent()

    /**
     * 远端音频 mute 状态变化（对齐声网 onUserMuteAudio）。
     * [muted] true=关麦，false=开麦。
     */
    data class RemoteAudioMuteChanged(val userId: String, val muted: Boolean) : CallEvent()

    /** 正在说话的人变化 */
    data class ActiveSpeakersChanged(val speakerIds: List<String>) : CallEvent()

    /** 本端已离房 */
    data object Disconnected : CallEvent()

    /** 连接失败 */
    data class FailedToConnect(val errorMessage: String? = null) : CallEvent()


    data object UnKnownEvent : CallEvent()
}
