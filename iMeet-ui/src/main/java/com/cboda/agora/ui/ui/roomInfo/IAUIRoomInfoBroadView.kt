package com.cboda.agora.ui.ui.roomInfo

import android.net.Uri
import com.cboda.agora.ui.ui.roomInfo.listener.AUIRoomInfoActionBroadListener

interface IAUIRoomInfoBroadView {

    fun setRoomInfoActionListener(listener: AUIRoomInfoActionBroadListener?){}

    fun setVoiceTitle(title:String){}

    fun setVoiceSubTitle(subtitle:String){}

    fun setMemberAvatar(url:String){}

    fun setMemberAvatar(uri: Uri){}

    fun setRightIcon(url:String){}

    fun setRightIcon(uri: Uri){}
}