package com.cboda.agora.ui.chatBottomBar

import com.cboda.agora.ui.chatBottomBar.listener.AUIMenuItemClickBroadListener

interface IAUIChatBottomBarBroadView {

    fun setMenuItemClickListener(listener: AUIMenuItemClickBroadListener?){}

    fun setSoftKeyListener(){}

    fun hideKeyboard(){}

    fun setEnableMic(isEnable: Boolean){}

    fun setShowMic(isShow:Boolean){}

    fun setShowMoreStatus(isOwner: Boolean?, isShowHandStatus: Boolean){}

}