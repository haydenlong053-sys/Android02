package com.cboda.agora.ui.ui.chatList.listener

import com.cboda.agora.ui.ui.chatList.AUIChatBroadInfo


interface AUIChatListItemClickBroadListener {
    fun onItemClickListener(message: AUIChatBroadInfo?){}
    fun onChatListViewClickListener(){}
}