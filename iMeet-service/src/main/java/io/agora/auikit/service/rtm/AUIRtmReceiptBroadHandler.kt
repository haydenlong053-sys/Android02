package io.agora.auikit.service.rtm

data class AUIRtmReceiptBroadHandler(
    val uniqueId: String,
    val closure: (AUIRtmBroadException?) -> Unit,
    val runnable: Runnable,
)