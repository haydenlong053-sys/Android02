package io.agora.asceneskit.voice

import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient

data class AUIAPIConfig(
    val rtcEngineEx: RtcEngineEx? = null,
    val rtmClient: RtmClient? = null
)