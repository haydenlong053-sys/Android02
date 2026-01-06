package com.cboda.agora.service.service.callback

import com.cboda.agora.service.model.AUIChatBroadEntity
import com.cboda.agora.service.model.AgoraChatBroadMessage
import io.agora.chat.ChatMessage

interface AUIChatMsgBroadCallback {
    fun onResult(error: AUIBroadException?,message: AgoraChatBroadMessage?){}
    fun onEntityResult(error: AUIBroadException?,message: AUIChatBroadEntity?){}
    fun onOriginalResult(error: AUIBroadException?,message: ChatMessage?){}
}