package io.agora.auikit.ui.member.listener

import android.view.View
import io.agora.auikit.ui.member.MemberBroadInfo

interface AUIRoomMembersActionBroadListener {
    fun onMemberRankClickListener(view: View){}
    fun onMemberRightUserMoreClickListener(view: View){}
    fun onMemberRightShutDownClickListener(view: View){}

    fun  onKickOutUser(view: View,user: MemberBroadInfo?){}
}