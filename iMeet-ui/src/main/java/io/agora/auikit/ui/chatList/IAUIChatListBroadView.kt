package io.agora.auikit.ui.chatList

import io.agora.auikit.ui.chatList.impl.AUIBroadcastMessageBroadLayout
import io.agora.auikit.ui.chatList.listener.AUIChatListItemClickBroadListener

interface IAUIChatListBroadView {
    fun setChatListItemClickListener(listener:AUIChatListItemClickBroadListener?){}

    fun refresh(msgList:List<AUIChatBroadInfo>){}

    fun refreshSelectLast(msgList:List<AUIChatBroadInfo>?){}

    // broadcast view
    fun setScrollSpeed(speed: Int){}

    fun showSubtitleView(content:String){}

    fun setSubtitleStatusChangeListener(listener: AUIBroadcastMessageBroadLayout.SubtitleStatusChangeListener){}
}