package io.agora.auikit.ui.action

import io.agora.auikit.ui.action.listener.AUIApplyDialogEventBroadListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventBroadListener

interface IAUIListViewBinderBroadRefresh {
    fun refreshApplyData(userList:List<AUIActionUserInfo>){}
    fun setApplyDialogListener(listener: AUIApplyDialogEventBroadListener){}
    fun refreshInvitationData(userList:List<AUIActionUserInfo>){}
    fun setInvitationDialogListener(listener: AUIInvitationDialogEventBroadListener){}
}