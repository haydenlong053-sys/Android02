package com.cboda.agora.ui.ui.roomInfo.listener

import android.view.View

interface AUIRoomInfoActionBroadListener {
    fun onBackClickListener(view: View){}
    fun onClickUpperLeftAvatar(view: View){}
    fun onLongClickUpperLeftAvatar(view: View): Boolean { return false }
    fun onUpperLeftRightIconClickListener(view: View){}
}