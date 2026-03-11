package com.cboda.chatui.quickmeet

import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.chatui.WfcBaseBroadActivity

fun showLoadingWfc() {
     if (getResumeActivity() is WfcBaseBroadActivity) {
        (getResumeActivity() as WfcBaseBroadActivity).showLoading()
    }

}

fun dismissLoadingWfc() {
     if (getResumeActivity() is WfcBaseBroadActivity) {
         (getResumeActivity() as WfcBaseBroadActivity).dismissLoading()
    }
}

fun goPermissionAudioMeet(goNext:()-> Unit={}){
    goNext.invoke()

}