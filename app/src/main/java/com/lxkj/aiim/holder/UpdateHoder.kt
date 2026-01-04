package com.lxkj.aiim.holder

//import com.mob.tools.utils.DH

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.widge.LiXInCenterDialog
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import razerdp.basepopup.BasePopupSDK.getApplication
import java.io.File


/**
 * 简述： <功能描述>
 * Created by ysh on 2023/8/15.
 */
@Deprecated("以后上架google应用市场要删除此代码")
class UpdateHoder(private val activity: Activity?) {
    private var dialog: AlertDialog? = null
    private var downLoadDialog: AlertDialog? = null
    private var bottomProgress: ProgressBar? = null
    private var fileLength = 0L
    var saveApkFile: File? = null

    var callBack: OnConfimCallBack? = null

    fun show(version: CommonResultBroadBean.CommonResult) {
        activity?.apply {
            if (this.isFinishing || this.isDestroyed) {
                return
            }
            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.update_dialog_layout,
                    null
                )
                val rootView = view.findViewById<View>(R.id.rootView)

                view.findViewById<View>(R.id.tvAgree)?.setOnClickListener {
                    dialog?.dismiss()
//                    callBack?.onClickConfim(version.url?:"")
//                    XXPermissions.with(activity!!)
//                        .permission(Permission.READ_MEDIA_IMAGES).request { _, _ ->
                            startDownLoadApp(version)
//                        }
                }
                view.findViewById<View>(R.id.tvNotAgree)?.setOnClickListener {
                    dialog?.dismiss()
                }

                view.findViewById<TextView>(R.id.tvUpdateContent)?.text = version.content
                view.findViewById<TextView>(R.id.tvUpdateVer)?.text = "V.${version.version}"

                dialog = LiXInCenterDialog(activity, view).createAndShowDialog()
                if (version.type == "1") {
                    view.findViewById<View>(R.id.tvNotAgree)?.visibility = View.GONE
                    dialog?.setCanceledOnTouchOutside(false)
                    dialog?.setCancelable(false)

                } else {
//                    view.findViewById<View>(R.id.tvNotAgree)?.visibility = View.VISIBLE
                    dialog?.setCanceledOnTouchOutside(true)
                    dialog?.setCancelable(true)
                }
            } else {
                dialog?.show()
            }

        }
    }

    private fun startDownLoadApp(version: CommonResultBroadBean.CommonResult) {
        activity?.apply {
            if (this.isFinishing || this.isDestroyed) {
                return
            }

            if (downLoadDialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.dialog_down_load_progress_layout,
                    null
                )
                bottomProgress = view.findViewById(R.id.bottom_progress)
                view.findViewById<View>(R.id.rl_cancle_down_load_layout)?.setOnClickListener {
                    OkGo.getInstance().cancelTag("startDownLoadApp")
                    downLoadDialog?.dismiss()
                }
                downLoadDialog = LiXInCenterDialog(activity, view).createAndShowDialog()
                if (version.type == "1") {
                    downLoadDialog?.setCanceledOnTouchOutside(false)
                    downLoadDialog?.setCancelable(false)
                    view.findViewById<View>(R.id.rl_cancle_down_load_layout)?.visibility = View.GONE


                } else {
                    downLoadDialog?.setCanceledOnTouchOutside(true)
                    downLoadDialog?.setCancelable(true)
                    view.findViewById<View>(R.id.rl_cancle_down_load_layout)?.visibility =
                        View.VISIBLE
                }
            } else {
                downLoadDialog?.show()
            }
            saveApkFile =
                File(activity.filesDir.path + "/images/sanciyuanyin/" + System.currentTimeMillis() + ".apk")
            OkGo.get<File>(version.url).tag("startDownLoadApp")
                .execute(object : FileCallback(saveApkFile?.name) {
                    override fun onSuccess(response: Response<File>?) {
                        downLoadApkHandler.sendMessage(downLoadApkHandler.obtainMessage(1))
                    }

                    override fun downloadProgress(progress: Progress) {
                        super.downloadProgress(progress)
                        fileLength = progress.totalSize
                        downLoadApkHandler.sendMessage(
                            downLoadApkHandler.obtainMessage(
                                0,
                                progress.currentSize
                            )
                        )
                        AppUtil.debug("文件下载进度", "${progress.currentSize} ${progress.totalSize}")
                    }

                })


        }

    }


    private var downLoadApkHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0 -> {
                    val curPercent = msg.obj as Long
                    val percent = (curPercent * 100 / fileLength).toInt()
                    bottomProgress?.progress = percent
                }
                1 -> {
                    downLoadDialog?.dismiss()
                    AppUtil.debug("下载完成", "文件路径：${saveApkFile?.path} ${saveApkFile?.length()}")
//                    saveApkFile = File("sdcard/download/app-shop-video-1.0.apk" )
//                    if (Build.VERSION.SDK_INT >= 26) {
//                        val b: Boolean =
//                            activity?.packageManager?.canRequestPackageInstalls() ?: false
//                        if (b) {
//                            callBack?.onClickConfim("")
//                        } else {
//                            //请求安装未知应用来源的权限
//                            Toast.makeText(activity, "请允许安装应用", Toast.LENGTH_SHORT)
//                                .show()
//                            startInstallPermissionSettingActivity()
//                        }
//                    } else {
//                        callBack?.onClickConfim("")
//                    }
                    activity?.apply {
                        XXPermissions.with(this)
                            .permission(Permission.REQUEST_INSTALL_PACKAGES)
                            .request(OnPermissionCallback { permissions, allGranted ->
                                if (!allGranted) {
                                    ToastBroadUtils.show("请允许安装应用")
                                    return@OnPermissionCallback
                                }
//                                callBack?.onClickConfim("")
                                installApk()
                            })
                    }


                }
            }
        }

        private var fileUri: Uri? = null
        private fun installApk() {
            try {
                val intent = Intent()
                if (Build.VERSION.SDK_INT >= 24) {
                    fileUri =
                        FileProvider.getUriForFile(activity!!.applicationContext,"lixindown", saveApkFile!!)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    fileUri = Uri.fromFile(saveApkFile)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
                activity?.startActivity(intent)
            } catch (e: Exception) {
                AppUtil.debug("安装apk异常", e.toString())
            }
        }


        /**
         * 跳转到设置-允许安装未知来源-页面
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        private fun startInstallPermissionSettingActivity() {
            //后面跟上包名，可以直接跳转到对应APP的未知来源权限设置界面。使用startActivityForResult 是为了在关闭设置界面之后，获取用户的操作结果，然后根据结果做其他处理
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + getApplication().packageName)
            )
            activity?.startActivityForResult(intent, 1)
        }
    }


    interface OnConfimCallBack {
        fun onClickConfim(url: String)
    }
}