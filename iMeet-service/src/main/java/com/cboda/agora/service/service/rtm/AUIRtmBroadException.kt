package com.cboda.agora.service.service.rtm

class AUIRtmBroadException(
    val code: Int,
    val reason: String,
    val operation: String
) : Throwable(reason)