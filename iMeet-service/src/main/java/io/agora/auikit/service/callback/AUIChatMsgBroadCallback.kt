package io.agora.auikit.service.callback

import io.agora.auikit.model.AUIChatBroadEntity
import io.agora.auikit.model.AgoraChatBroadMessage
import io.agora.chat.ChatMessage

interface AUIChatMsgBroadCallback {
    fun onResult(error: AUIBroadException?,message: AgoraChatBroadMessage?){}
    fun onEntityResult(error: AUIBroadException?,message: AUIChatBroadEntity?){}
    fun onOriginalResult(error: AUIBroadException?,message: ChatMessage?){}
}