package com.cboda.chatui.quickmeet

import android.Manifest
import android.content.Context
import android.os.Build
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.ComBottomListBean
import com.cboda.baselibrary.popup.CommonBottomListPop
import com.cboda.baselibrary.popup.CommonTipBroadPop
import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.baselibrary.utils.requestPermission
import com.cboda.baselibrary.utils.showCommonBottomListPop
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

fun goPermissionAudioMeet(goNext: () -> Unit = {}) {
    requestPermission(getMeetAudioPermissions()) {
        goNext.invoke()
    }
}

// 获取体验实时音视频互动所需的录音、摄像头等权限
// Get the permissions required for real-time audio and video interaction
fun getMeetAudioPermissions(): Array<String> {
    // 判断 targetSDKVersion 31 及以上时所需的权限
    // Determine the permissions required when targetSDKVersion is 31 or higher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return arrayOf<String>(
            Manifest.permission.RECORD_AUDIO,  // 录音权限 // Recording permission
//                Manifest.permission.CAMERA,  // 摄像头权限 // Camera permission
//                Manifest.permission.READ_PHONE_STATE,  // 读取电话状态权限 // Read phone state permission
            Manifest.permission.BLUETOOTH_CONNECT // 蓝牙连接权限 // Bluetooth connection permission
        )
    } else {
        return arrayOf<String>(
            Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA
        )
    }
}

fun showMeetPreEndTip(time: String?, amount: String?, block: () -> Unit = {}) {
    try {
        getResumeActivity()?.let { mContext ->
            val commonTipPop = CommonTipBroadPop(
                mContext,
                mContext.getString(R.string.wenxintishi),
                mContext.getString(R.string.chat_meet_pre_end_tip) + "\n" + "${time}分钟(${amount} ODIC)"
            )
            commonTipPop.setCancelBtn(mContext.getString(R.string.chat_meet_time_tip_cancel))
            commonTipPop.setSureBtn(mContext.getString(R.string.chat_meet_time_tip_ok))
            commonTipPop.sureInvoke = {
                block.invoke()
            }
            commonTipPop.showPopupWindow()
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

//下麦弹窗
fun showDownMicDialog(context: Context? = null, block: (Int, ComBottomListBean) -> Unit): CommonBottomListPop {
    val list = arrayListOf<ComBottomListBean>()
    list.add(ComBottomListBean().apply {
        iconRes = com.cboda.chatui.R.mipmap.icon_down_mic
        itemContent = "下麦"
    })
    list.add(ComBottomListBean().apply {
        iconRes = com.cboda.chatui.R.mipmap.icon_mute
        itemContent = "静音"
    })
    return showCommonBottomListPop(list, context, block = block)
}