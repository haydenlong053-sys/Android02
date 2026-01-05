package io.agora.auikit.ui.member

import io.agora.auikit.ui.basic.AUIImageBroadView
import io.agora.auikit.ui.member.listener.AUIRoomMembersActionBroadListener

interface IAUIRoomMembersBroadView {

    fun setMemberActionListener(listener: AUIRoomMembersActionBroadListener?){}

    fun setRightIconResources(url:String,view: AUIImageBroadView){}

    fun setMemberData(rankList: List<MemberBroadInfo?>){}



}