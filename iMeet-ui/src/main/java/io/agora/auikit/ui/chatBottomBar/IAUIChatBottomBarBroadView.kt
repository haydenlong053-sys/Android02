package io.agora.auikit.ui.chatBottomBar

import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickBroadListener

interface IAUIChatBottomBarBroadView {

    fun setMenuItemClickListener(listener: AUIMenuItemClickBroadListener?){}

    fun setSoftKeyListener(){}

    fun hideKeyboard(){}

    fun setEnableMic(isEnable: Boolean){}

    fun setShowMic(isShow:Boolean){}

    fun setShowMoreStatus(isOwner: Boolean?, isShowHandStatus: Boolean){}

}