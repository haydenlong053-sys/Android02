package com.cboda.baselibrary.utils

import android.Manifest
import com.cboda.baselibrary.R
import com.cboda.baselibrary.app.UtilBroadApplication
import com.blankj.utilcode.util.PermissionUtils
import com.luck.picture.lib.permissions.PermissionConfig

fun requestPermission(callback: OnPermissionBroadCallback? = null, vararg permissions: String) {
    val permissionList = ArrayList<String>()
    PermissionUtils.permission(
        *permissions
    ).callback { _, _, _, _ ->
        //是否全部同意
        var isAll = true
        for (i in permissions.indices) {
            val permission = permissions[i]
            if (!PermissionUtils.isGranted(permission)) {
                val per = getPermissionName(permission)
                if (!permissionList.contains(per)) {
                    permissionList.add(per)
                }
                isAll = false
            }
        }
        if (isAll) {
            callback?.onAllPermissionGranted()
        } else {
//            showTipDialog(permissionList) { callback?.onPermissionRefuse() }
            callback?.onPermissionRefuse()
        }
    }.rationale { _, shouldRequest ->
        shouldRequest.again(true)
    }.request()
}


fun requestPermission2(callback: OnPermissionBroadCallback2? = null, vararg permissions: String) {
    val permissionList = ArrayList<String>()
    PermissionUtils.permission(
        *permissions
    ).callback { _, _, _, _ ->
        //是否全部同意
        var isAll = true
        for (i in permissions.indices) {
            val permission = permissions[i]
            if (!PermissionUtils.isGranted(permission)) {
                val per = getPermissionName(permission)
                if (!permissionList.contains(per)) {
                    permissionList.add(per)
                }
                isAll = false
            }
        }
        if (isAll) {
            callback?.onAllPermissionGranted()
        } else {
//            showTipDialog(permissionList) { callback?.onPermissionRefuse() }
            callback?.onRationale()
        }
    }.rationale { _, _ ->
        callback?.onRationale()
    }.request()
}


fun requestPermission(permissions: Array<String>, callBack: () -> Unit) {
    requestPermission(object : OnPermissionBroadCallback {
        override fun onAllPermissionGranted() {
            callBack.invoke()
        }

        override fun onPermissionRefuse() {

        }
    }, *permissions)
}

interface OnPermissionBroadCallback {
    fun onAllPermissionGranted()

    fun onPermissionRefuse()

}

interface OnPermissionBroadCallback2 {
    fun onAllPermissionGranted()

    fun onPermissionRefuse()

    fun onRationale()
}

fun getPermissionName(permission: String): String {
    val permissionId = when (permission) {
        Manifest.permission.CAMERA -> R.string.camera
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> R.string.storage
        Manifest.permission.READ_EXTERNAL_STORAGE -> R.string.read
        Manifest.permission.READ_PHONE_STATE -> R.string.phone
        Manifest.permission.ACCESS_FINE_LOCATION -> R.string.location_p
        Manifest.permission.RECORD_AUDIO -> R.string.microphone
        PermissionConfig.READ_MEDIA_IMAGES -> R.string.storage
        PermissionConfig.READ_MEDIA_VIDEO -> R.string.storage
        PermissionConfig.READ_MEDIA_AUDIO -> R.string.storage
        else -> R.string.about_per

    }
//    if (TextUtils.isEmpty(permissionName)) {
//        permissionName = permission
//    }
    return UtilBroadApplication.cxt.getString(permissionId)
}