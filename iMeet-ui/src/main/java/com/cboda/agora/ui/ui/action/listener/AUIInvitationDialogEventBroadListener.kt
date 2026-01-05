package com.cboda.agora.ui.ui.action.listener

import android.view.View
import com.cboda.agora.ui.action.AUIActionUserInfo

interface AUIInvitationDialogEventBroadListener {
    fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUIActionUserInfo?){}
}