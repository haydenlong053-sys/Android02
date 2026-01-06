package io.agora.auikit.service.callback


interface AUICreateChatRoomBroadCallback {
    fun onResult(error: AUIBroadException?, chatRoomId: String?)
}