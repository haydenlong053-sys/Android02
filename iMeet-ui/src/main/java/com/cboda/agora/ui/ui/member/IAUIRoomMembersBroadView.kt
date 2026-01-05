package com.cboda.agora.ui.ui.member

import com.cboda.agora.ui.ui.basic.AUIImageBroadView
import com.cboda.agora.ui.ui.member.listener.AUIRoomMembersActionBroadListener

interface IAUIRoomMembersBroadView {

    fun setMemberActionListener(listener: AUIRoomMembersActionBroadListener?){}

    fun setRightIconResources(url:String,view: AUIImageBroadView){}

    fun setMemberData(rankList: List<MemberBroadInfo?>){}



}