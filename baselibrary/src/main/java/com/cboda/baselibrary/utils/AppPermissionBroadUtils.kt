package com.cboda.baselibrary.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cboda.baselibrary.popup.PermissionExplainBroadPopup
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.UtilsTransActivity

/**
 * App权限检查工具类
 * 支持单个权限、多个权限检查，动态权限传递
 * 兼容View框架和Compose
 */
object AppPermissionBroadUtils {

    /**
     * 常用权限常量
     */
    object BroadPermissions {
        // 存储权限
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

        // 相机权限
        const val CAMERA = Manifest.permission.CAMERA

        // 录音权限
        const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

        // 位置权限
        const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        // 通讯录权限
        const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
        const val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS

        // 电话权限
        const val CALL_PHONE = Manifest.permission.CALL_PHONE
        const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE

        // 短信权限
        const val SEND_SMS = Manifest.permission.SEND_SMS
        const val READ_SMS = Manifest.permission.READ_SMS
        const val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS

        // 日历权限
        const val READ_CALENDAR = Manifest.permission.READ_CALENDAR
        const val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR

        // 通知权限 (Android 13+)
        val POST_NOTIFICATIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            ""
        }

        // 媒体权限 (Android 13+)
        val READ_MEDIA_IMAGES = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            ""
        }

        val READ_MEDIA_VIDEO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            ""
        }

        val READ_MEDIA_AUDIO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            ""
        }
    }

    /**
     * 权限请求结果回调接口
     */
    interface PermissionCallback {
        /**
         * 权限被授予
         * @param permissions 被授予的权限列表
         */
        fun onPermissionGranted(permissions: List<String>)

        /**
         * 权限被拒绝
         * @param permissions 被拒绝的权限列表
         * @param neverAskAgain 是否勾选了"不再询问"
         */
        fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean = false)
    }

    /**
     * 简化的权限请求接口
     * 支持在任何地方（如Adapter）直接处理权限请求
     */
    interface SimplePermissionCallback {
        /**
         * 权限被授予时调用
         */
        fun onGranted()

        /**
         * 权限被拒绝时调用
         * @param neverAskAgain 是否选择了"不再询问"
         */
        fun onDenied(neverAskAgain: Boolean = false)
    }

    /**
     * 权限状态枚举
     */
    enum class PermissionBroadStatus {
        GRANTED,        // 已授予
        DENIED,         // 被拒绝（可以再次请求）
        NEVER_ASK_AGAIN,// 永不询问（用户选择了不再询问）
        FIRST_REQUEST   // 首次请求（从未请求过该权限）
    }

    /**
     * 权限请求结果数据类
     */
    data class PermissionRequestBroadResult(
        val requestCode: Int,
        val permissions: Array<String>,
        val grantResults: IntArray
    ) {
        /**
         * 获取被授予的权限列表
         */
        val grantedPermissions: List<String>
            get() = permissions.filterIndexed { index, _ ->
                grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED
            }

        /**
         * 获取被拒绝的权限列表
         */
        val deniedPermissions: List<String>
            get() = permissions.filterIndexed { index, _ ->
                grantResults.getOrNull(index) != PackageManager.PERMISSION_GRANTED
            }

        /**
         * 是否所有权限都被授予
         */
        val allGranted: Boolean
            get() = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        /**
         * 是否有权限被拒绝
         */
        val hasDenied: Boolean
            get() = !allGranted

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PermissionRequestBroadResult

            if (requestCode != other.requestCode) return false
            if (!permissions.contentEquals(other.permissions)) return false
            if (!grantResults.contentEquals(other.grantResults)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = requestCode
            result = 31 * result + permissions.contentHashCode()
            result = 31 * result + grantResults.contentHashCode()
            return result
        }
    }

    /**
     * 请求单个权限
     * @param activity Activity实例
     * @param permission 权限名称
     * @param requestCode 请求码
     */
    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        markPermissionAsRequested(activity, permission)
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    /**
     * 请求多个权限
     * @param activity Activity实例
     * @param permissions 权限列表
     * @param requestCode 请求码
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        permissions.forEach { markPermissionAsRequested(activity, it) }
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /**
     * 请求多个权限（List版本）
     */
    fun requestPermissions(activity: Activity, permissions: List<String>, requestCode: Int) {
        requestPermissions(activity, permissions.toTypedArray(), requestCode)
    }

    /**
     * 请求单个权限（Fragment版本）
     */
    fun requestPermission(fragment: Fragment, permission: String, requestCode: Int) {
        fragment.requestPermissions(arrayOf(permission), requestCode)
    }

    /**
     * 请求多个权限（Fragment版本）
     */
    fun requestPermissions(fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        fragment.requestPermissions(permissions, requestCode)
    }

    /**
     * 请求多个权限（Fragment List版本）
     */
    fun requestPermissions(fragment: Fragment, permissions: List<String>, requestCode: Int) {
        requestPermissions(fragment, permissions.toTypedArray(), requestCode)
    }

    /**
     * 处理权限请求结果
     * @param requestCode 请求码
     * @param permissions 权限列表
     * @param grantResults 授权结果
     * @param callback 回调接口
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        callback: PermissionCallback?
    ) {
        val result = PermissionRequestBroadResult(requestCode, permissions, grantResults)

        if (result.allGranted) {
            callback?.onPermissionGranted(result.grantedPermissions)
        } else {
            callback?.onPermissionDenied(result.deniedPermissions)
        }
    }

    /**
     * 处理权限请求结果（带Activity参数，用于检查是否勾选"不再询问"）
     */
    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        callback: PermissionCallback?
    ) {
        val result = PermissionRequestBroadResult(requestCode, permissions, grantResults)

        if (result.allGranted) {
            callback?.onPermissionGranted(result.grantedPermissions)
        } else {
            // 检查是否有权限被永久拒绝（用户勾选了"不再询问"）
            val neverAskAgain = result.deniedPermissions.any { permission ->
                !shouldShowRequestPermissionRationale(activity, permission)
            }
            callback?.onPermissionDenied(result.deniedPermissions, neverAskAgain)
        }
    }

    /**
     * 处理权限请求结果（Fragment版本，用于检查是否勾选"不再询问"）
     */
    fun handlePermissionResult(
        fragment: Fragment,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        callback: PermissionCallback?
    ) {
        val result = PermissionRequestBroadResult(requestCode, permissions, grantResults)

        if (result.allGranted) {
            callback?.onPermissionGranted(result.grantedPermissions)
        } else {
            val neverAskAgain = result.deniedPermissions.any { permission ->
                !shouldShowRequestPermissionRationale(fragment, permission)
            }
            callback?.onPermissionDenied(result.deniedPermissions, neverAskAgain)
        }
    }

    /**
     * 请求存储权限
     * @param activity Activity实例
     * @param requestCode 请求码
     */
    fun requestStoragePermission(activity: Activity, requestCode: Int) {
        val permissions = getStoragePermissions()
        if (permissions.isNotEmpty()) {
            requestPermissions(activity, permissions, requestCode)
        }
    }

    /**
     * 请求存储权限（Fragment版本）
     */
    fun requestStoragePermission(fragment: Fragment, requestCode: Int) {
        val permissions = getStoragePermissions()
        if (permissions.isNotEmpty()) {
            requestPermissions(fragment, permissions, requestCode)
        }
    }

    /**
     * 请求相机权限
     */
    fun requestCameraPermission(activity: Activity, requestCode: Int) {
        requestPermission(activity, BroadPermissions.CAMERA, requestCode)
    }

    /**
     * 请求相机权限（Fragment版本）
     */
    fun requestCameraPermission(fragment: Fragment, requestCode: Int) {
        requestPermission(fragment, BroadPermissions.CAMERA, requestCode)
    }

    /**
     * 请求录音权限
     */
    fun requestAudioPermission(activity: Activity, requestCode: Int) {
        requestPermission(activity, BroadPermissions.RECORD_AUDIO, requestCode)
    }

    /**
     * 请求录音权限（Fragment版本）
     */
    fun requestAudioPermission(fragment: Fragment, requestCode: Int) {
        requestPermission(fragment, BroadPermissions.RECORD_AUDIO, requestCode)
    }

    /**
     * 请求位置权限
     */
    fun requestLocationPermission(
        activity: Activity,
        requestCode: Int,
        includeFineLocation: Boolean = true
    ) {
        val permissions = if (includeFineLocation) {
            arrayOf(BroadPermissions.ACCESS_FINE_LOCATION, BroadPermissions.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(BroadPermissions.ACCESS_COARSE_LOCATION)
        }
        requestPermissions(activity, permissions, requestCode)
    }

    /**
     * 请求位置权限（Fragment版本）
     */
    fun requestLocationPermission(
        fragment: Fragment,
        requestCode: Int,
        includeFineLocation: Boolean = true
    ) {
        val permissions = if (includeFineLocation) {
            arrayOf(BroadPermissions.ACCESS_FINE_LOCATION, BroadPermissions.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(BroadPermissions.ACCESS_COARSE_LOCATION)
        }
        requestPermissions(fragment, permissions, requestCode)
    }

    /**
     * 请求通知权限（Android 13+）
     */
    fun requestNotificationPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(activity, BroadPermissions.POST_NOTIFICATIONS, requestCode)
        }
    }

    /**
     * 请求通知权限（Fragment版本）
     */
    fun requestNotificationPermission(fragment: Fragment, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(fragment, BroadPermissions.POST_NOTIFICATIONS, requestCode)
        }
    }

    // ==================== 便捷的权限请求方法（支持在任何地方使用） ====================

    /**
     * 便捷的权限检查和请求方法
     * 支持在任何地方（如Adapter）直接使用
     * @param activity Activity实例
     * @param permission 权限名称
     * @param requestCode 请求码
     * @param callback 简化的回调接口
     * @param showRationaleMessage 是否显示权限说明消息
     */
    fun checkAndRequestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int,
        callback: SimplePermissionCallback,
        showRationaleMessage: String? = null
    ) {
        when (getPermissionStatus(activity, permission)) {
            PermissionBroadStatus.GRANTED -> {
                callback.onGranted()
            }

            PermissionBroadStatus.FIRST_REQUEST -> {
                // 首次请求权限，直接请求
                requestPermission(activity, permission, requestCode)
            }

            PermissionBroadStatus.DENIED -> {
                if (showRationaleMessage != null && shouldShowRequestPermissionRationale(
                        activity,
                        permission
                    )
                ) {
                    // 可以在这里显示说明对话框
                    // 这里简化处理，直接请求权限
                }
                requestPermission(activity, permission, requestCode)
            }

            PermissionBroadStatus.NEVER_ASK_AGAIN -> {
                callback.onDenied(neverAskAgain = true)
            }
        }
    }

    /**
     * 便捷的权限检查和请求方法（Fragment版本）
     */
    fun checkAndRequestPermission(
        fragment: Fragment,
        permission: String,
        requestCode: Int,
        callback: SimplePermissionCallback,
        showRationaleMessage: String? = null
    ) {
        when (getPermissionStatus(fragment.requireActivity(), permission)) {
            PermissionBroadStatus.GRANTED -> {
                callback.onGranted()
            }

            PermissionBroadStatus.FIRST_REQUEST -> {
                requestPermission(fragment, permission, requestCode)
            }

            PermissionBroadStatus.DENIED -> {
                if (showRationaleMessage != null && shouldShowRequestPermissionRationale(
                        fragment,
                        permission
                    )
                ) {
                    // 可在此展示说明UI，这里简化为直接请求
                }
                requestPermission(fragment, permission, requestCode)
            }

            PermissionBroadStatus.NEVER_ASK_AGAIN -> {
                callback.onDenied(neverAskAgain = true)
            }
        }
    }

    /**
     * 便捷的多权限检查和请求方法
     * @param activity Activity实例
     * @param permissions 权限列表
     * @param requestCode 请求码
     * @param callback 简化的回调接口
     */
    fun checkAndRequestPermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        val deniedPermissions = getDeniedPermissions(activity, permissions)

        if (deniedPermissions.isEmpty()) {
            callback.onGranted()
            return
        }

        val neverAskAgainPermissions = getNeverAskAgainPermissions(activity, permissions)

        if (neverAskAgainPermissions.isNotEmpty()) {
            callback.onDenied(neverAskAgain = true)
            return
        }

        requestPermissions(activity, permissions, requestCode)
    }

    /**
     * 便捷的多权限检查和请求方法（Fragment版本）
     */
    fun checkAndRequestPermissions(
        fragment: Fragment,
        permissions: Array<String>,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        val deniedPermissions = getDeniedPermissions(fragment.requireContext(), permissions)

        if (deniedPermissions.isEmpty()) {
            callback.onGranted()
            return
        }

        val neverAskAgainPermissions =
            getNeverAskAgainPermissions(fragment.requireActivity(), permissions)

        if (neverAskAgainPermissions.isNotEmpty()) {
            callback.onDenied(neverAskAgain = true)
            return
        }

        requestPermissions(fragment, permissions, requestCode)
    }

    /**
     * 便捷的存储权限检查和请求
     */
    fun checkAndRequestStoragePermission(
        activity: Activity,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        val permissions = getStoragePermissions()
        checkAndRequestPermissions(activity, permissions, requestCode, callback)
    }

    /**
     * 便捷的存储权限检查和请求（Fragment版本）
     */
    fun checkAndRequestStoragePermission(
        fragment: Fragment,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        val permissions = getStoragePermissions()
        checkAndRequestPermissions(fragment, permissions, requestCode, callback)
    }

    /**
     * 便捷的相机权限检查和请求
     */
    fun checkAndRequestCameraPermission(
        activity: Activity,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        checkAndRequestPermission(activity, BroadPermissions.CAMERA, requestCode, callback)
    }

    /**
     * 便捷的相机权限检查和请求（Fragment版本）
     */
    fun checkAndRequestCameraPermission(
        fragment: Fragment,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        checkAndRequestPermission(fragment, BroadPermissions.CAMERA, requestCode, callback)
    }

    /**
     * 便捷的录音权限检查和请求
     */
    fun checkAndRequestAudioPermission(
        activity: Activity,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        checkAndRequestPermission(activity, BroadPermissions.RECORD_AUDIO, requestCode, callback)
    }

    /**
     * 便捷的录音权限检查和请求（Fragment版本）
     */
    fun checkAndRequestAudioPermission(
        fragment: Fragment,
        requestCode: Int,
        callback: SimplePermissionCallback
    ) {
        checkAndRequestPermission(fragment, BroadPermissions.RECORD_AUDIO, requestCode, callback)
    }

    /**
     * 便捷的位置权限检查和请求
     */
    fun checkAndRequestLocationPermission(
        activity: Activity,
        requestCode: Int,
        callback: SimplePermissionCallback,
        includeFineLocation: Boolean = true
    ) {
        val permissions = if (includeFineLocation) {
            arrayOf(BroadPermissions.ACCESS_FINE_LOCATION, BroadPermissions.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(BroadPermissions.ACCESS_COARSE_LOCATION)
        }
        checkAndRequestPermissions(activity, permissions, requestCode, callback)
    }

    /**
     * 便捷的位置权限检查和请求（Fragment版本）
     */
    fun checkAndRequestLocationPermission(
        fragment: Fragment,
        requestCode: Int,
        callback: SimplePermissionCallback,
        includeFineLocation: Boolean = true
    ) {
        val permissions = if (includeFineLocation) {
            arrayOf(BroadPermissions.ACCESS_FINE_LOCATION, BroadPermissions.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(BroadPermissions.ACCESS_COARSE_LOCATION)
        }
        checkAndRequestPermissions(fragment, permissions, requestCode, callback)
    }

    /**
     * 获取权限的详细状态（Fragment便捷版本）
     */
    fun getPermissionStatus(fragment: Fragment, permission: String): PermissionBroadStatus {
        return getPermissionStatus(fragment.requireActivity(), permission)
    }

    /**
     * 检查多个权限是否有被永不询问的（Fragment便捷版本）
     */
    fun getNeverAskAgainPermissions(fragment: Fragment, permissions: Array<String>): List<String> {
        return getNeverAskAgainPermissions(fragment.requireActivity(), permissions)
    }

    /**
     * 检查单个权限是否已授予
     *
     * @param context 上下文
     * @param permission 权限名称
     * @return true表示已授予，false表示未授予
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return if (permission.isEmpty()) {
            true // 空权限认为已授予
        } else {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查多个权限是否都已授予
     * @param context 上下文
     * @param permissions 权限列表
     * @return true表示所有权限都已授予，false表示至少有一个权限未授予
     */
    fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            hasPermission(context, permission)
        }
    }

    /**
     * 检查多个权限是否都已授予（List版本）
     */
    fun hasAllPermissions(context: Context, permissions: List<String>): Boolean {
        return hasAllPermissions(context, permissions.toTypedArray())
    }

    /**
     * 获取未授予的权限列表
     * @param context 上下文
     * @param permissions 要检查的权限列表
     * @return 未授予的权限列表
     */
    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { permission ->
            !hasPermission(context, permission)
        }
    }

    /**
     * 获取未授予的权限列表（List版本）
     */
    fun getDeniedPermissions(context: Context, permissions: List<String>): List<String> {
        return getDeniedPermissions(context, permissions.toTypedArray())
    }

    /**
     * 检查是否需要显示权限说明
     * @param activity Activity实例
     * @param permission 权限名称
     * @return true表示需要显示说明，false表示不需要
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 获取权限的详细状态
     * @param activity Activity实例
     * @param permission 权限名称
     * @return 权限状态枚举
     */
    fun getPermissionStatus(activity: Activity, permission: String): PermissionBroadStatus {
        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        val shouldShowRationale = shouldShowRequestPermissionRationale(activity, permission)
        val isFirstTime = isFirstTimeRequestingPermission(activity, permission)

        return when {
            // 首次请求：从未请求过该权限
            isFirstTime -> PermissionBroadStatus.FIRST_REQUEST

            // 已授予：有权限且不需要显示说明（持久授权）
            hasPermission && !shouldShowRationale -> PermissionBroadStatus.GRANTED

            // 每次都询问：没有权限但需要显示说明
            !hasPermission && shouldShowRationale -> PermissionBroadStatus.DENIED

            // 永不询问：没有权限且不需要显示说明，且不是首次请求
            !hasPermission && !shouldShowRationale -> PermissionBroadStatus.NEVER_ASK_AGAIN

            // 特殊情况：有权限但需要显示说明（可能是"仅限这一次"的临时权限）
            // 这种情况下，我们认为是临时授权，应该清理记录并重新判断
            hasPermission && shouldShowRationale -> {
                // 清理权限记录，下次将被视为首次请求
                clearPermissionRecord(activity, permission)
                PermissionBroadStatus.DENIED
            }

            else -> PermissionBroadStatus.NEVER_ASK_AGAIN
        }
    }

    /**
     * 检查是否是首次请求权限
     * 通过SharedPreferences记录权限请求历史
     * @param activity Activity实例
     * @param permission 权限名称
     * @return true表示首次请求，false表示之前已经请求过
     */
    private fun isFirstTimeRequestingPermission(activity: Activity, permission: String): Boolean {
        val prefs = activity.getSharedPreferences("permission_requests", Context.MODE_PRIVATE)
        val key = "requested_$permission"
        return !prefs.getBoolean(key, false)
    }

    /**
     * 标记权限已经被请求过
     * @param activity Activity实例
     * @param permission 权限名称
     */
    private fun markPermissionAsRequested(activity: Activity, permission: String) {
        val prefs = activity.getSharedPreferences("permission_requests", Context.MODE_PRIVATE)
        val key = "requested_$permission"
        prefs.edit().putBoolean(key, true).apply()
    }

    /**
     * 清理权限请求记录
     * 用于处理"仅限这一次"权限的情况，当权限被撤销时清理记录
     * @param activity Activity实例
     * @param permission 权限名称
     */
    fun clearPermissionRecord(activity: Activity, permission: String) {
        val prefs = activity.getSharedPreferences("permission_requests", Context.MODE_PRIVATE)
        val key = "requested_$permission"
        prefs.edit().remove(key).apply()
    }

    /**
     * 清理所有权限请求记录
     * @param activity Activity实例
     */
    fun clearAllPermissionRecords(activity: Activity) {
        val prefs = activity.getSharedPreferences("permission_requests", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * 检查权限是否被永不询问
     * @param activity Activity实例
     * @param permission 权限名称
     * @return true表示被永不询问，false表示可以继续请求
     */
    fun isPermissionNeverAskAgain(activity: Activity, permission: String): Boolean {
        return getPermissionStatus(activity, permission) == PermissionBroadStatus.NEVER_ASK_AGAIN
    }

    /**
     * 检查多个权限是否有被永不询问的
     * @param activity Activity实例
     * @param permissions 权限列表
     * @return 被永不询问的权限列表
     */
    fun getNeverAskAgainPermissions(activity: Activity, permissions: Array<String>): List<String> {
        return permissions.filter { permission ->
            isPermissionNeverAskAgain(activity, permission)
        }
    }

    /**
     * 检查是否需要显示权限说明（Fragment版本）
     */
    fun shouldShowRequestPermissionRationale(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }

    /**
     * 检查多个权限中是否有需要显示说明的
     */
    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        permissions: Array<String>
    ): Boolean {
        return permissions.any { permission ->
            shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    /**
     * 检查存储权限（兼容不同Android版本）
     * @param context 上下文
     * @return true表示有存储权限，false表示没有
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用新的媒体权限
            hasPermission(context, BroadPermissions.READ_MEDIA_IMAGES) &&
                    hasPermission(context, BroadPermissions.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 不需要WRITE_EXTERNAL_STORAGE权限
            hasPermission(context, BroadPermissions.WRITE_EXTERNAL_STORAGE)
        } else {
            // Android 9及以下需要存储权限
            hasPermission(context, BroadPermissions.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun applyAndCheckReadStoragePermission(context: Context, callBack: () -> Unit) {
        if (hasGetReadStoragePermission(context)) {
            callBack.invoke()
        } else {
//            ToastBroadUtils.show("暂无权限")
            applyReadStoragePermission(context) {
                callBack.invoke()
            }
        }
    }

    /**
     * 检查获取图片视频权限（兼容不同Android版本）
     * @param context 上下文
     * @return true表示有存储权限，false表示没有
     */
    fun hasGetReadStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用新的媒体权限
//            hasPermission(context, BroadPermissions.READ_MEDIA_IMAGES) &&
//                    hasPermission(context, BroadPermissions.READ_MEDIA_VIDEO)
            PermissionUtils.isGranted(BroadPermissions.READ_MEDIA_IMAGES, BroadPermissions.READ_MEDIA_VIDEO)
        } else {
//            hasPermission(context, BroadPermissions.READ_EXTERNAL_STORAGE)
            PermissionUtils.isGranted(BroadPermissions.READ_EXTERNAL_STORAGE)
        }
    }

    //申请相册权限
    fun applyReadStoragePermission(context: Context, callBack: () -> Unit) {
        PermissionExplainBroadPopup.BroadBuilder(context)
            .setTitle("温馨提示")
            .addPermissionExplain("使用选择图片功能需要相机、存储权限，是否请求权限？")
            .setOnConfirm {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionUtils.permission(
                        BroadPermissions.READ_MEDIA_IMAGES,
                        BroadPermissions.READ_MEDIA_VIDEO
                    ).callback(object :
                        PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            callBack.invoke()
                        }

                        override fun onDenied() {
                            PermissionUtils.launchAppDetailsSettings()
                        }
                    }).request()
                } else {
                    PermissionUtils.permission(BroadPermissions.READ_EXTERNAL_STORAGE).callback(object :
                        PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            callBack.invoke()
                        }

                        override fun onDenied() {
                            PermissionUtils.launchAppDetailsSettings()
                        }
                    }).request()
                }
            }
            .show()

    }

    /**
     * 获取存储权限列表（根据Android版本）
     * @return 需要请求的存储权限列表
     */
    fun getStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用新的媒体权限
            arrayOf(BroadPermissions.READ_MEDIA_IMAGES, BroadPermissions.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 不需要存储权限
            arrayOf(BroadPermissions.WRITE_EXTERNAL_STORAGE)
        } else {
            // Android 9及以下需要存储权限
            arrayOf(BroadPermissions.WRITE_EXTERNAL_STORAGE)
        }
    }

    /**
     * 检查相机权限
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, BroadPermissions.CAMERA)
    }

    /**
     * 检查录音权限
     */
    fun hasAudioPermission(context: Context): Boolean {
        return hasPermission(context, BroadPermissions.RECORD_AUDIO)
    }

    /**
     * 检查位置权限
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasPermission(context, BroadPermissions.ACCESS_FINE_LOCATION) ||
                hasPermission(context, BroadPermissions.ACCESS_COARSE_LOCATION)
    }

    /**
     * 检查精确位置权限
     */
    fun hasFineLocationPermission(context: Context): Boolean {
        return hasPermission(context, BroadPermissions.ACCESS_FINE_LOCATION)
    }

    /**
     * 检查通知权限（Android 13+）
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, BroadPermissions.POST_NOTIFICATIONS)
        } else {
            true // Android 13以下默认有通知权限
        }
    }

    /**
     * 动态检查权限
     * @param context 上下文
     * @param permissions 权限列表（可变参数）
     * @return PermissionBroadResult 权限检查结果
     */
    fun checkPermissions(context: Context, vararg permissions: String): PermissionBroadResult {
        val permissionsList = permissions.toList()
        val deniedPermissions = getDeniedPermissions(context, permissionsList)
        return PermissionBroadResult(
            allGranted = deniedPermissions.isEmpty(),
            grantedPermissions = permissions.filter { hasPermission(context, it) },
            deniedPermissions = deniedPermissions
        )
    }

    /**
     * 动态检查权限（List版本）
     */
    fun checkPermissions(context: Context, permissions: List<String>): PermissionBroadResult {
        return checkPermissions(context, *permissions.toTypedArray())
    }

    /**
     * 权限检查结果数据类
     */
    data class PermissionBroadResult(
        val allGranted: Boolean,
        val grantedPermissions: List<String>,
        val deniedPermissions: List<String>
    ) {
        /**
         * 是否有权限被拒绝
         */
        val hasDenied: Boolean get() = deniedPermissions.isNotEmpty()

        /**
         * 是否部分权限被授予
         */
        val partialGranted: Boolean get() = grantedPermissions.isNotEmpty() && deniedPermissions.isNotEmpty()
    }
}