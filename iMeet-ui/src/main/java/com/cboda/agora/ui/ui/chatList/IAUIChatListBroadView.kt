package com.cboda.agora.ui.ui.chatList

import com.cboda.agora.ui.chatList.AUIChatBroadInfo
import com.cboda.agora.ui.ui.chatList.impl.AUIBroadcastMessageBroadLayout
import com.cboda.agora.ui.ui.chatList.listener.AUIChatListItemClickBroadListener


interface IAUIChatListBroadView {
    fun setChatListItemClickListener(listener: AUIChatListItemClickBroadListener?){}

    fun refresh(msgList:List<AUIChatBroadInfo>){}

    fun refreshSelectLast(msgList:List<AUIChatBroadInfo>?){}

    // broadcast view
    fun setScrollSpeed(speed: Int){}

    fun showSubtitleView(content:String){}

    fun setSubtitleStatusChangeListener(listener: AUIBroadcastMessageBroadLayout.SubtitleStatusChangeListener){}
}