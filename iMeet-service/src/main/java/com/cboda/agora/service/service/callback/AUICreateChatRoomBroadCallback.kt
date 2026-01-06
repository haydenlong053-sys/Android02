package com.cboda.agora.service.service.callback


interface AUICreateChatRoomBroadCallback {
    fun onResult(error: AUIBroadException?, chatRoomId: String?)
}