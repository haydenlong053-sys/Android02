package com.cboda.baselibrary.utils

import android.text.TextUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.toast.ToastBroadUtils

object E {
    //1:IMS连接失败 2:IMS连接成功 3:IMS连接中
    fun showNetToast(type: Int) {
        ThreadUtils.getCachedPool().execute {
            if (MMKVBroadUtils.getImPullErrToastState()) {
                val toastMsg = when (type) {
                    1 -> "IMS连接失败"
                    2 -> "IMS连接成功"
                    3 -> "IMS连接中 "
                    else -> ""
                }
                if (!TextUtils.isEmpty(toastMsg)) {
                    ThreadUtils.getMainHandler().post {
                        ToastBroadUtils.show(toastMsg)
                    }
                }
            }
        }
    }
}