/**
 * AppPermissionBroadUtils 权限请求功能使用示例
 * 
 * 本示例展示了如何使用 AppPermissionBroadUtils 进行权限请求和处理，包括：
 * 1. 在Activity和Fragment中请求权限
 * 2. 处理权限请求结果和回调
 * 3. 正确判断权限状态：首次请求、每次询问、永不询问
 * 4. 在Adapter中直接处理权限请求
 * 5. 在任何自定义类中使用权限请求功能
 * 6. 使用简化的权限请求接口
 * 
 * 权限状态说明：
 * - GRANTED: 权限已授予
 * - FIRST_REQUEST: 首次请求权限（从未请求过）
 * - DENIED: 权限被拒绝，但用户选择"每次询问"，可以再次请求
 * - NEVER_ASK_AGAIN: 权限被永不询问，用户选择了"不再询问"
 * 
 * ⚠️ 重要说明：hasPermission() 方法说明
 * 
 * hasPermission() 方法使用标准的系统API检查权限状态：
 * 
 * 1. 标准检查：使用系统API检查当前权限的实际授权状态
 * 2. 简单可靠：返回权限是否已被授予的布尔值
 * 3. 详细状态：对于复杂的权限状态判断，建议使用 getPermissionStatus() 方法
 * 4. 配合使用：hasPermission() 和 getPermissionStatus() 可以配合使用获得完整信息
 * 
 * 🔧 "仅限这一次"权限的特殊处理：
 * 在某些Android版本中，"仅限这一次"权限可能会在短时间内（几分钟到几小时）保持有效，
 * 即使app重启。为了正确处理这种情况，我们的 getPermissionStatus() 方法会：
 * - 检测 hasPermission=true && shouldShowRationale=true 的组合
 * - 自动清理权限记录，避免误判为持久授权
 * - 将状态标记为 DENIED，提示需要重新请求
 * 
 * 正确的权限状态判断方式：
 * - 推荐使用 getPermissionStatus() 来获取详细的权限状态
 * - hasPermission() 用于简单的是/否判断
 * - 使用 isPermissionNeverAskAgain() 来判断是否为"永不询问"状态
 * - 注意：对于"仅限这一次"权限，hasPermission() 可能返回 true，但 getPermissionStatus() 会返回 DENIED
 * 
 * 示例场景：
 * 1. 用户首次打开应用 → hasPermission() = false, getPermissionStatus() = FIRST_REQUEST
 * 2. 用户选择"每次都询问"并授予 → hasPermission() = true, getPermissionStatus() = DENIED
 * 3. 应用重启后 → hasPermission() = false, getPermissionStatus() = DENIED
 * 4. 用户选择"永不询问" → hasPermission() = false, getPermissionStatus() = NEVER_ASK_AGAIN
 *
 * 主要特性：
 * - 支持单个和多个权限请求
 * - 准确检测权限状态，包括"永不询问"
 * - 可在任何地方（Activity、Fragment、Adapter等）使用
 * - 提供简化的回调接口，减少样板代码
 * - 包含完整的权限处理逻辑和用户引导
 * - 提供 PermissionBroadDebugger 工具来调试权限状态问题
 *
 * 使用建议：
 * 1. 现在可以安全使用 hasPermission() 或 getPermissionStatus() 来判断权限状态
 * 2. 使用 checkAndRequestPermission() 系列方法简化权限请求流程
 * 3. 对于"永不询问"状态，引导用户到设置页面手动开启权限
 * 4. 使用 PermissionBroadDebugger 工具来调试权限状态问题，包括 testHasPermissionConsistency() 方法
 */

package com.cboda.baselibrary.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
object AppPermissionRequestBroadExample {

    /**
     * Activity中使用权限请求的示例
     */
    class ActivityBroadExample : Activity() {
        
        companion object {
            private const val REQUEST_STORAGE = 1001
            private const val REQUEST_CAMERA = 1002
            private const val REQUEST_LOCATION = 1003
            private const val REQUEST_MULTIPLE = 1004
        }

        /**
         * 示例1：请求存储权限
         */
        private fun requestStoragePermissionExample() {
            // 检查是否已有权限
            if (AppPermissionBroadUtils.hasStoragePermission(this)) {
                // 已有权限，直接执行操作
                performStorageOperation()
            } else {
                // 请求存储权限
                AppPermissionBroadUtils.requestStoragePermission(this, REQUEST_STORAGE)
            }
        }

        /**
         * 示例2：请求相机权限
         */
        private fun requestCameraPermissionExample() {
            if (AppPermissionBroadUtils.hasCameraPermission(this)) {
                openCamera()
            } else {
                AppPermissionBroadUtils.requestCameraPermission(this, REQUEST_CAMERA)
            }
        }

        /**
         * 示例3：请求位置权限
         */
        private fun requestLocationPermissionExample() {
            if (AppPermissionBroadUtils.hasLocationPermission(this)) {
                startLocationService()
            } else {
                // 请求精确位置权限（包含粗略位置）
                AppPermissionBroadUtils.requestLocationPermission(this, REQUEST_LOCATION, true)
            }
        }

        /**
         * 示例4：请求多个权限
         */
        private fun requestMultiplePermissionsExample() {
            val permissions = arrayOf(
                AppPermissionBroadUtils.BroadPermissions.CAMERA,
                AppPermissionBroadUtils.BroadPermissions.RECORD_AUDIO,
                AppPermissionBroadUtils.BroadPermissions.ACCESS_FINE_LOCATION
            )
            
            val result = AppPermissionBroadUtils.checkPermissions(this, permissions.toList())
            if (result.allGranted) {
                // 所有权限都已授予
                startCameraWithAudioAndLocation()
            } else {
                // 请求缺失的权限
                AppPermissionBroadUtils.requestPermissions(this, result.deniedPermissions, REQUEST_MULTIPLE)
            }
        }

        /**
         * 权限请求结果处理
         */
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            
            when (requestCode) {
                REQUEST_STORAGE -> {
                    AppPermissionBroadUtils.handlePermissionResult(
                        this, requestCode, permissions, grantResults,
                        object : AppPermissionBroadUtils.PermissionBroadCallback {
                            override fun onPermissionGranted(permissions: List<String>) {
                                performStorageOperation()
                            }

                            override fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean) {
                                if (neverAskAgain) {
                                    showGoToSettingsDialog("存储权限被永久拒绝，请在设置中手动开启")
                                } else {
                                    showMessage("需要存储权限才能保存文件")
                                }
                            }
                        }
                    )
                }
                
                REQUEST_CAMERA -> {
                    AppPermissionBroadUtils.handlePermissionResult(
                        this, requestCode, permissions, grantResults,
                        object : AppPermissionBroadUtils.PermissionBroadCallback {
                            override fun onPermissionGranted(permissions: List<String>) {
                                openCamera()
                            }

                            override fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean) {
                                showMessage("需要相机权限才能拍照")
                            }
                        }
                    )
                }
                
                REQUEST_LOCATION -> {
                    AppPermissionBroadUtils.handlePermissionResult(
                        this, requestCode, permissions, grantResults,
                        object : AppPermissionBroadUtils.PermissionBroadCallback {
                            override fun onPermissionGranted(permissions: List<String>) {
                                startLocationService()
                            }

                            override fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean) {
                                showMessage("需要位置权限才能获取定位")
                            }
                        }
                    )
                }
                
                REQUEST_MULTIPLE -> {
                    AppPermissionBroadUtils.handlePermissionResult(
                        this, requestCode, permissions, grantResults,
                        object : AppPermissionBroadUtils.PermissionBroadCallback {
                            override fun onPermissionGranted(permissions: List<String>) {
                                // 检查是否所有权限都被授予
                                val allPermissions = arrayOf(
                                    AppPermissionBroadUtils.BroadPermissions.CAMERA,
                                    AppPermissionBroadUtils.BroadPermissions.RECORD_AUDIO,
                                    AppPermissionBroadUtils.BroadPermissions.ACCESS_FINE_LOCATION
                                )
                                if (AppPermissionBroadUtils.hasAllPermissions(this@ActivityBroadExample, allPermissions)) {
                                    startCameraWithAudioAndLocation()
                                } else {
                                    showMessage("部分权限被授予，功能可能受限")
                                }
                            }

                            override fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean) {
                                showMessage("需要所有权限才能使用完整功能")
                            }
                        }
                    )
                }
            }
        }

        // 模拟业务方法
        private fun performStorageOperation() {
            // 执行存储操作
        }

        private fun openCamera() {
            // 打开相机
        }

        private fun startLocationService() {
            // 启动定位服务
        }

        private fun startCameraWithAudioAndLocation() {
            // 启动带音频和定位的相机功能
        }

        private fun showMessage(message: String) {
            // 显示消息
        }

        private fun showGoToSettingsDialog(message: String) {
            // 显示跳转设置的对话框
        }
    }

    /**
     * Fragment中使用权限请求的示例
     */
    class FragmentBroadExample : Fragment() {
        
        companion object {
            private const val REQUEST_AUDIO = 2001
            private const val REQUEST_NOTIFICATION = 2002
        }

        /**
         * Fragment中请求录音权限示例
         */
        private fun requestAudioPermissionExample() {
            if (AppPermissionBroadUtils.hasAudioPermission(requireContext())) {
                startRecording()
            } else {
                AppPermissionBroadUtils.requestAudioPermission(this, REQUEST_AUDIO)
            }
        }

        /**
         * Fragment中请求通知权限示例（Android 13+）
         */
        private fun requestNotificationPermissionExample() {
            if (AppPermissionBroadUtils.hasNotificationPermission(requireContext())) {
                enableNotifications()
            } else {
                AppPermissionBroadUtils.requestNotificationPermission(this, REQUEST_NOTIFICATION)
            }
        }

        /**
         * Fragment权限请求结果处理
         */
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            
            when (requestCode) {
                REQUEST_AUDIO -> {
                    AppPermissionBroadUtils.handlePermissionResult(
                        requestCode, permissions, grantResults,
                        object : AppPermissionBroadUtils.PermissionBroadCallback {
                            override fun onPermissionGranted(permissions: List<String>) {
                                startRecording()
                            }

                            override fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean) {
                                showMessage("需要录音权限才能录制音频")
                            }
                        }
                    )
                }
                
                REQUEST_NOTIFICATION -> {
                    AppPermissionBroadUtils.handlePermissionResult(
                        requestCode, permissions, grantResults,
                        object : AppPermissionBroadUtils.PermissionBroadCallback {
                            override fun onPermissionGranted(permissions: List<String>) {
                                enableNotifications()
                            }

                            override fun onPermissionDenied(permissions: List<String>, neverAskAgain: Boolean) {
                                showMessage("需要通知权限才能接收消息提醒")
                            }
                        }
                    )
                }
            }
        }

        // 模拟业务方法
        private fun startRecording() {
            // 开始录音
        }

        private fun enableNotifications() {
            // 启用通知
        }

        private fun showMessage(message: String) {
            // 显示消息
        }
    }

    /**
     * 高级用法示例
     */
    object AdvancedBroadUsage {
        
        /**
         * 批量检查权限状态
         */
        fun checkMultiplePermissionsStatus(activity: Activity) {
            val permissions = listOf(
                AppPermissionBroadUtils.BroadPermissions.CAMERA,
                AppPermissionBroadUtils.BroadPermissions.RECORD_AUDIO,
                AppPermissionBroadUtils.BroadPermissions.ACCESS_FINE_LOCATION
            )
            
            val result = AppPermissionBroadUtils.checkPermissions(activity, permissions)
            
            println("所有权限已授予: ${result.allGranted}")
            println("已授予的权限: ${result.grantedPermissions}")
            println("被拒绝的权限: ${result.deniedPermissions}")
            println("有权限被拒绝: ${result.hasDenied}")
            println("部分权限被授予: ${result.partialGranted}")
        }

        /**
         * 检查是否需要显示权限说明
         */
        fun checkShouldShowRationale(activity: Activity) {
            val permission = AppPermissionBroadUtils.BroadPermissions.CAMERA
            
            if (!AppPermissionBroadUtils.hasPermission(activity, permission)) {
                if (AppPermissionBroadUtils.shouldShowRequestPermissionRationale(activity, permission)) {
                    // 应该显示权限说明
                    showPermissionRationaleDialog(activity, permission)
                } else {
                    // 直接请求权限
                    AppPermissionBroadUtils.requestPermission(activity, permission, 1001)
                }
            }
        }

        private fun showPermissionRationaleDialog(activity: Activity, permission: String) {
            // 显示权限说明对话框
        }
    }

    // ==================== 权限状态调试工具 ====================
    
    /**
     * 详细的权限状态调试工具
     * 用于分析hasPermission方法的行为
     */
    object PermissionBroadDebugger {
        
        /**
         * 全面分析单个权限的状态
         */
        fun analyzePermissionStatus(activity: Activity, permission: String) {
            println("=== 权限状态详细分析 ===")
            println("权限: $permission")
            
            // 1. 检查hasPermission的结果
            val hasPermission = AppPermissionBroadUtils.hasPermission(activity, permission)
            println("hasPermission(): $hasPermission")
            
            // 2. 直接调用系统API检查
            val systemCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                activity, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            println("系统直接检查: $systemCheck")
            
            // 3. 检查shouldShowRequestPermissionRationale
            val shouldShowRationale = androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                activity, permission
            )
            println("shouldShowRequestPermissionRationale(): $shouldShowRationale")
            
            // 4. 使用我们的getPermissionStatus方法
            val permissionStatus = AppPermissionBroadUtils.getPermissionStatus(activity, permission)
            println("getPermissionStatus(): $permissionStatus")
            
            // 5. 检查是否首次请求（通过状态判断）
             val isFirstTime = (permissionStatus == AppPermissionBroadUtils.PermissionBroadStatus.FIRST_REQUEST)
             println("是否首次请求: $isFirstTime")
            
            // 6. 检查是否永不询问
            val isNeverAskAgain = AppPermissionBroadUtils.isPermissionNeverAskAgain(activity, permission)
            println("是否永不询问: $isNeverAskAgain")
            
            println("=== 分析结束 ===")
            
            // 显示Toast提示
            Toast.makeText(activity, 
                "权限分析完成，请查看日志\nhasPermission: $hasPermission\n状态: $permissionStatus", 
                Toast.LENGTH_LONG).show()
        }
        
        /**
         * 在权限请求前后对比状态
         */
        fun comparePermissionStatusBeforeAfterRequest(activity: Activity, permission: String, requestCode: Int) {
            println("=== 权限请求前后状态对比 ===")
            
            // 请求前的状态
            println("--- 请求前 ---")
            analyzePermissionStatus(activity, permission)
            
            // 请求权限
            println("--- 开始请求权限 ---")
            AppPermissionBroadUtils.requestPermission(activity, permission, requestCode)
        }
        
        /**
         * 在onRequestPermissionsResult中调用，分析请求后的状态
         */
        fun analyzeAfterPermissionResult(
            activity: Activity, 
            requestCode: Int, 
            permissions: Array<String>, 
            grantResults: IntArray
        ) {
            println("=== 权限请求结果分析 ===")
            println("requestCode: $requestCode")
            
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                
                println("--- 权限: $permission ---")
                println("grantResult: ${if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"}")
                
                // 分析请求后的状态
                analyzePermissionStatus(activity, permission)
            }
        }
        
        /**
         * 测试"仅限这一次"权限的处理
         * 这个方法可以帮助验证修复后的逻辑
         */
        fun testOneTimePermissionHandling(activity: Activity, permission: String) {
            println("=== 测试仅限这一次权限处理 ===")
            println("权限: $permission")
            
            val hasPermission = AppPermissionBroadUtils.hasPermission(activity, permission)
            val shouldShowRationale = AppPermissionBroadUtils.shouldShowRequestPermissionRationale(activity, permission)
            val status = AppPermissionBroadUtils.getPermissionStatus(activity, permission)
            
            println("hasPermission: $hasPermission")
            println("shouldShowRationale: $shouldShowRationale")
            println("权限状态: $status")
            
            // 特别关注这种组合：hasPermission=true && shouldShowRationale=true
            if (hasPermission && shouldShowRationale) {
                println("⚠️ 检测到可能的'仅限这一次'权限状态")
                println("系统将清理权限记录并重新判断状态")
            }
            
            // 建议的处理方式
            when (status) {
                AppPermissionBroadUtils.PermissionBroadStatus.GRANTED -> {
                    println("✅ 权限已持久授予，可以正常使用")
                }
                AppPermissionBroadUtils.PermissionBroadStatus.DENIED -> {
                    println("⚠️ 权限被拒绝或为临时权限，建议重新请求")
                }
                AppPermissionBroadUtils.PermissionBroadStatus.FIRST_REQUEST -> {
                    println("🆕 首次请求权限")
                }
                AppPermissionBroadUtils.PermissionBroadStatus.NEVER_ASK_AGAIN -> {
                    println("🚫 用户选择了永不询问，需要引导到设置页面")
                }
            }
        }
        
        /**
     * 测试优化后的 hasPermission 方法与 getPermissionStatus 的一致性
     * 验证两个方法的结果是否匹配
     */
//    fun testHasPermissionConsistency() {
//        val permissions = arrayOf(
//            AppPermissionBroadUtils.BroadPermissions.CAMERA,
//            AppPermissionBroadUtils.BroadPermissions.RECORD_AUDIO,
//            AppPermissionBroadUtils.BroadPermissions.ACCESS_FINE_LOCATION,
//            AppPermissionBroadUtils.BroadPermissions.WRITE_EXTERNAL_STORAGE
//        )
//
//        Log.d("PermissionTest", "=== hasPermission 一致性测试 ===")
//
//        permissions.forEach { permission ->
//            if (permission.isNotEmpty()) {
//                val hasPermission = AppPermissionBroadUtils.hasPermission(this, permission)
//                val permissionStatus = AppPermissionBroadUtils.getPermissionStatus(this, permission)
//                val expectedHasPermission = (permissionStatus == AppPermissionBroadUtils.PermissionBroadStatus.GRANTED)
//
//                Log.d("PermissionTest", "权限: $permission")
//                Log.d("PermissionTest", "  hasPermission(): $hasPermission")
//                Log.d("PermissionTest", "  getPermissionStatus(): $permissionStatus")
//                Log.d("PermissionTest", "  期望的hasPermission: $expectedHasPermission")
//                Log.d("PermissionTest", "  一致性: ${hasPermission == expectedHasPermission}")
//
//                if (hasPermission != expectedHasPermission) {
//                    Log.w("PermissionTest", "  ⚠️ 发现不一致！")
//                }
//                Log.d("PermissionTest", "  ---")
//            }
//        }
//    }
    }

    // ==================== 新增功能示例 ====================

    /**
     * 示例：检查权限的详细状态（包括永不询问）
     */
    class PermissionStatusBroadExample : AppCompatActivity() {

        private fun checkPermissionDetailedStatus() {
            val permission = AppPermissionBroadUtils.BroadPermissions.CAMERA
            
            when (AppPermissionBroadUtils.getPermissionStatus(this, permission)) {
                AppPermissionBroadUtils.PermissionBroadStatus.GRANTED -> {
                    Toast.makeText(this, "相机权限已授予", Toast.LENGTH_SHORT).show()
                    // 执行需要权限的操作
                    openCamera()
                }
                AppPermissionBroadUtils.PermissionBroadStatus.FIRST_REQUEST -> {
                    Toast.makeText(this, "首次请求相机权限", Toast.LENGTH_SHORT).show()
                    // 首次请求权限，直接请求即可
                    AppPermissionBroadUtils.requestCameraPermission(this, REQUEST_CAMERA_PERMISSION)
                }
                AppPermissionBroadUtils.PermissionBroadStatus.DENIED -> {
                    Toast.makeText(this, "相机权限被拒绝，可以再次请求", Toast.LENGTH_SHORT).show()
                    // 可以显示权限说明，然后再次请求
                    showPermissionRationale()
                }
                AppPermissionBroadUtils.PermissionBroadStatus.NEVER_ASK_AGAIN -> {
                    Toast.makeText(this, "相机权限被永久拒绝，请到设置中手动开启", Toast.LENGTH_LONG).show()
                    // 引导用户到设置页面
                    showGoToSettingsDialog()
                }
            }
        }

        private fun checkMultiplePermissionsNeverAskAgain() {
            val permissions = arrayOf(
                AppPermissionBroadUtils.BroadPermissions.CAMERA,
                AppPermissionBroadUtils.BroadPermissions.RECORD_AUDIO
            )
            
            val neverAskAgainPermissions = AppPermissionBroadUtils.getNeverAskAgainPermissions(this, permissions)
            
            if (neverAskAgainPermissions.isNotEmpty()) {
                val message = "以下权限被永久拒绝，请到设置中手动开启：\n${neverAskAgainPermissions.joinToString("\n")}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                showGoToSettingsDialog()
            }
        }

        private fun openCamera() {
            // 打开相机的逻辑
        }

        private fun showPermissionRationale() {
            AlertDialog.Builder(this)
                .setTitle("需要相机权限")
                .setMessage("为了拍照功能，需要访问您的相机")
                .setPositiveButton("确定") { _, _ ->
                    AppPermissionBroadUtils.requestCameraPermission(this, REQUEST_CAMERA_PERMISSION)
                }
                .setNegativeButton("取消", null)
                .show()
        }

        private fun showGoToSettingsDialog() {
            AlertDialog.Builder(this)
                .setTitle("权限被永久拒绝")
                .setMessage("请到设置中手动开启相关权限")
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
        }

        companion object {
            private const val REQUEST_CAMERA_PERMISSION = 1001
        }
    }

    /**
     * 示例：在Adapter中直接处理权限请求
     * 这是新增的重要功能，允许在任何地方直接处理权限逻辑
     */
    class PhotoBroadAdapter(
        private val activity: Activity,
        private val photos: List<String>
    ) : RecyclerView.Adapter<PhotoBroadAdapter.PhotoViewBroadHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewBroadHolder {
            // 创建ViewHolder的逻辑
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return PhotoViewBroadHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewBroadHolder, position: Int) {
            // 绑定数据的逻辑
            holder.bind(photos[position])
        }

        override fun getItemCount(): Int = photos.size

        inner class PhotoViewBroadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            
            fun bind(photoUrl: String) {
                // 绑定照片数据
                
                // 点击拍照按钮
                itemView.setOnClickListener {
                    requestCameraPermissionAndTakePhoto()
                }
            }

            /**
             * 在Adapter中直接处理相机权限请求
             * 使用新的便捷方法，无需在Activity中写额外代码
             */
            private fun requestCameraPermissionAndTakePhoto() {
                AppPermissionBroadUtils.checkAndRequestCameraPermission(
                    activity,
                    REQUEST_CAMERA_PERMISSION,
                    object : AppPermissionBroadUtils.SimplePermissionBroadCallback {
                        override fun onGranted() {
                            // 权限已授予，直接执行拍照逻辑
                            takePhoto()
                        }

                        override fun onDenied(neverAskAgain: Boolean) {
                            if (neverAskAgain) {
                                // 权限被永久拒绝
                                showNeverAskAgainDialog()
                            } else {
                                // 权限被拒绝，但可以再次请求
                                Toast.makeText(activity, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            /**
             * 请求存储权限并保存照片
             */
            private fun requestStoragePermissionAndSavePhoto() {
                AppPermissionBroadUtils.checkAndRequestStoragePermission(
                    activity,
                    REQUEST_STORAGE_PERMISSION,
                    object : AppPermissionBroadUtils.SimplePermissionBroadCallback {
                        override fun onGranted() {
                            // 存储权限已授予，保存照片
                            savePhotoToGallery()
                        }

                        override fun onDenied(neverAskAgain: Boolean) {
                            val message = if (neverAskAgain) {
                                "存储权限被永久拒绝，请到设置中手动开启"
                            } else {
                                "需要存储权限才能保存照片"
                            }
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            /**
             * 请求多个权限（相机+存储）
             */
            private fun requestMultiplePermissions() {
                val permissions = arrayOf(
                    AppPermissionBroadUtils.BroadPermissions.CAMERA,
                    AppPermissionBroadUtils.BroadPermissions.WRITE_EXTERNAL_STORAGE
                )

                AppPermissionBroadUtils.checkAndRequestPermissions(
                    activity,
                    permissions,
                    REQUEST_MULTIPLE_PERMISSIONS,
                    object : AppPermissionBroadUtils.SimplePermissionBroadCallback {
                        override fun onGranted() {
                            // 所有权限都已授予
                            takePhotoAndSave()
                        }

                        override fun onDenied(neverAskAgain: Boolean) {
                            if (neverAskAgain) {
                                showGoToSettingsDialog()
                            } else {
                                Toast.makeText(activity, "需要相机和存储权限", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            private fun takePhoto() {
                // 拍照逻辑
                Toast.makeText(activity, "开始拍照", Toast.LENGTH_SHORT).show()
            }

            private fun savePhotoToGallery() {
                // 保存照片到相册
                Toast.makeText(activity, "照片已保存", Toast.LENGTH_SHORT).show()
            }

            private fun takePhotoAndSave() {
                // 拍照并保存
                Toast.makeText(activity, "拍照并保存", Toast.LENGTH_SHORT).show()
            }

            private fun showNeverAskAgainDialog() {
                AlertDialog.Builder(activity)
                    .setTitle("权限被永久拒绝")
                    .setMessage("相机权限被永久拒绝，请到设置中手动开启")
                    .setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", activity.packageName, null)
                        }
                        activity.startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }

            private fun showGoToSettingsDialog() {
                AlertDialog.Builder(activity)
                    .setTitle("权限不足")
                    .setMessage("部分权限被永久拒绝，请到设置中手动开启")
                    .setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", activity.packageName, null)
                        }
                        activity.startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }

        }

        companion object {
            private const val REQUEST_CAMERA_PERMISSION = 2001
            private const val REQUEST_STORAGE_PERMISSION = 2002
            private const val REQUEST_MULTIPLE_PERMISSIONS = 2003
        }
    }

    /**
     * 示例：在任何自定义类中使用权限请求
     */
    class PhotoBroadManager(private val activity: Activity) {

        /**
         * 拍照功能 - 展示如何在非Activity/Fragment类中处理权限
         */
        fun takePhoto() {
            // 使用便捷方法检查和请求权限
            AppPermissionBroadUtils.checkAndRequestCameraPermission(
                activity,
                REQUEST_CAMERA,
                object : AppPermissionBroadUtils.SimplePermissionBroadCallback {
                    override fun onGranted() {
                        // 权限已授予，执行拍照
                        performTakePhoto()
                    }

                    override fun onDenied(neverAskAgain: Boolean) {
                        handlePermissionDenied(neverAskAgain, "相机")
                    }
                }
            )
        }

        /**
         * 录音功能
         */
        fun startRecording() {
            AppPermissionBroadUtils.checkAndRequestAudioPermission(
                activity,
                REQUEST_AUDIO,
                object : AppPermissionBroadUtils.SimplePermissionBroadCallback {
                    override fun onGranted() {
                        performStartRecording()
                    }

                    override fun onDenied(neverAskAgain: Boolean) {
                        handlePermissionDenied(neverAskAgain, "录音")
                    }
                }
            )
        }

        /**
         * 获取位置功能
         */
        fun getLocation() {
            AppPermissionBroadUtils.checkAndRequestLocationPermission(
                activity,
                REQUEST_LOCATION,
                object : AppPermissionBroadUtils.SimplePermissionBroadCallback {
                    override fun onGranted() {
                        performGetLocation()
                    }

                    override fun onDenied(neverAskAgain: Boolean) {
                        handlePermissionDenied(neverAskAgain, "位置")
                    }
                },
                includeFineLocation = true
            )
        }

        private fun performTakePhoto() {
            Toast.makeText(activity, "开始拍照", Toast.LENGTH_SHORT).show()
            // 实际的拍照逻辑
        }

        private fun performStartRecording() {
            Toast.makeText(activity, "开始录音", Toast.LENGTH_SHORT).show()
            // 实际的录音逻辑
        }

        private fun performGetLocation() {
            Toast.makeText(activity, "开始获取位置", Toast.LENGTH_SHORT).show()
            // 实际的定位逻辑
        }

        private fun handlePermissionDenied(neverAskAgain: Boolean, permissionName: String) {
            val message = if (neverAskAgain) {
                "${permissionName}权限被永久拒绝，请到设置中手动开启"
            } else {
                "需要${permissionName}权限才能继续"
            }
            
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            
            if (neverAskAgain) {
                // 可以显示对话框引导用户到设置页面
                showSettingsDialog(permissionName)
            }
        }

        private fun showSettingsDialog(permissionName: String) {
            AlertDialog.Builder(activity)
                .setTitle("权限被永久拒绝")
                .setMessage("${permissionName}权限被永久拒绝，请到设置中手动开启")
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                    activity.startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
        }

        companion object {
            private const val REQUEST_CAMERA = 3001
            private const val REQUEST_AUDIO = 3002
            private const val REQUEST_LOCATION = 3003
        }
    }
}