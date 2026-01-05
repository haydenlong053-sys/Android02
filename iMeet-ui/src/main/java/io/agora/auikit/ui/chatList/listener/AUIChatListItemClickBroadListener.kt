package io.agora.auikit.ui.chatList.listener

import io.agora.auikit.ui.chatList.AUIChatBroadInfo


interface AUIChatListItemClickBroadListener {
    fun onItemClickListener(message: AUIChatBroadInfo?){}
    fun onChatListViewClickListener(){}
}