package com.cboda.agora.ui.ui.member.listener

import android.view.View
import com.cboda.agora.ui.ui.member.MemberBroadInfo

interface AUIRoomMembersActionBroadListener {
    fun onMemberRankClickListener(view: View){}
    fun onMemberRightUserMoreClickListener(view: View){}
    fun onMemberRightShutDownClickListener(view: View){}

    fun  onKickOutUser(view: View,user: MemberBroadInfo?){}
}