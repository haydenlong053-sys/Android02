package com.cboda.agora.service.service.rtm

data class AUIRtmReceiptBroadHandler(
    val uniqueId: String,
    val closure: (AUIRtmBroadException?) -> Unit,
    val runnable: Runnable,
)