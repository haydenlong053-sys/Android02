package com.cboda.agora.ui.ui.action

import com.cboda.agora.ui.action.AUIActionUserInfo
import com.cboda.agora.ui.ui.action.listener.AUIApplyDialogEventBroadListener
import com.cboda.agora.ui.ui.action.listener.AUIInvitationDialogEventBroadListener


interface IAUIListViewBinderBroadRefresh {
    fun refreshApplyData(userList:List<AUIActionUserInfo>){}
    fun setApplyDialogListener(listener: AUIApplyDialogEventBroadListener){}
    fun refreshInvitationData(userList:List<AUIActionUserInfo>){}
    fun setInvitationDialogListener(listener: AUIInvitationDialogEventBroadListener){}
}