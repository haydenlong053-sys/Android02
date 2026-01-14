package com.cboda.agora.ui.ui.action.listener


import android.view.View
import com.cboda.agora.ui.ui.action.AUIActionUserInfo

interface AUIApplyDialogEventBroadListener {
    fun onApplyItemClick(view: View, applyIndex: Int?, user: AUIActionUserInfo?, position:Int){}
}