package com.cboda.intelligent.holder

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.select.picture.basic.PictureSelector
import com.cboda.select.picture.config.SelectMimeType
import com.cboda.select.picture.config.SelectModeConfig
import com.cboda.select.picture.entity.LocalMedia
import com.cboda.select.picture.interfaces.OnResultCallbackListener
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.GlideBroadBroadEngine
import com.cboda.intelligent.R

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/3/31.
 */
class ImagePickerBroadHolder(private val activity: Context?) {
    var callBack: OnResultCallback? = null
    private var bottomHoder: BottomDialogBroadHolder? = null
    private var handler :Handler? = null
    fun show(
        isVideo: Boolean = false,
        isSingle: Boolean = true,
        isNumber: Int? = 1,
        isGif: Boolean? = false
    ) {
        if (bottomHoder == null) {
            bottomHoder = BottomDialogBroadHolder(activity, contentList = arrayListOf(activity?.getString(R.string.拍摄) ?: "拍摄", activity?.getString(R.string.从相册中选取) ?: "从相册中选取"))
        }
        bottomHoder?.callBack = object : BottomDialogBroadHolder.BottomDialogClickCallBack {
            override fun clickItem(position: Int) {
                when (position) {
                    0 -> {

                        val isGranted = XXPermissions.isGranted(activity!!, Permission.CAMERA)
                        if (isGranted) {
                            camera(isVideo)
                        } else {
                            val holder3 = CommanConfimBroadHolder(
                                activity,
                                activity?.getString(R.string.温馨提示) ?: "温馨提示",
                                activity?.getString(R.string.需要相机权限是否请求权限, AppBroadUtils.getAppName(activity)) ?: "${AppBroadUtils.getAppName(activity)}需要相机权限,是否请求权限?",
                                activity?.getString(R.string.同意) ?: "同意",
                                activity?.getString(R.string.拒绝) ?: "拒绝"
                            )
                            holder3.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                                override fun onClickConfim(isConfim: Boolean) {
                                    if (isConfim) {
                                        XXPermissions.with(activity!!)
                                            .permission(Permission.CAMERA).request { _, _ ->
                                                camera(isVideo)
                                            }
                                    }
                                }
                            }
                            holder3.show()

                        }

                    }
                    1 -> {
                        // 音频 READ_MEDIA_AUDIO
                        val permission =
                            if (isVideo) Permission.READ_MEDIA_VIDEO else Permission.READ_MEDIA_IMAGES
                        val isGranted = XXPermissions.isGranted(activity!!, permission)
                        if (isGranted) {
                            openGallery(isVideo, isNumber, isGif,isSingle)
                        } else {
                            val holder3 = CommanConfimBroadHolder(
                                activity,
                                activity?.getString(R.string.温馨提示) ?: "温馨提示",
                                activity?.getString(R.string.需要存储权限是否请求权限, AppBroadUtils.getAppName(activity)) ?: "${AppBroadUtils.getAppName(activity)}需要存储权限,是否请求权限?",
                                activity?.getString(R.string.同意) ?: "同意",
                                activity?.getString(R.string.拒绝) ?: "拒绝"
                            )
                            holder3.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                                override fun onClickConfim(isConfim: Boolean) {
                                    if (isConfim) {
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                            // 10以下
                                            val delayMillis = 2000L
                                            if (handler == null){
                                                handler = Handler(Looper.getMainLooper())
                                            }
                                            handler?.postDelayed({
                                                XXPermissions.with(activity!!)
                                                    .permission(permission).request { _, _ ->
                                                        openGallery(isVideo, isNumber, isGif, isSingle = isSingle)
                                                    }
                                            },delayMillis)

                                        } else {
                                            XXPermissions.with(activity!!)
                                                .permission(permission).request { _, _ ->
                                                    openGallery(isVideo, isNumber, isGif, isSingle = isSingle)
                                                }
                                        }

                                    }
                                }
                            }
                            holder3.show()

                        }
                    }
                }
            }

        }
        bottomHoder?.show()
    }


    private fun camera(isVideo: Boolean) {
        PictureSelector.create(activity)
            ?.openCamera(if (isVideo) SelectMimeType.ofVideo() else SelectMimeType.ofImage())
            ?.forResult(object :
                OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    callBack?.onResult(result)
                }

                override fun onCancel() {
                    callBack?.onCancel()
                }

            })
    }

    private fun openGallery(
        isVideo: Boolean,
        isNumber: Int? = 1,
        isGif: Boolean? = false,
        isSingle: Boolean = false
    ) {
        val callback = object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>?) {
                if (result?.isNotEmpty() == true) {
                    callBack?.onResult(result)
                }
            }

            override fun onCancel() {
                callBack?.onCancel()
            }
        }
        if (isVideo) {
            PictureSelector.create(activity)
                ?.openSystemGallery(SelectMimeType.ofVideo())
                ?.setSelectionMode(if (isSingle) SelectModeConfig.SINGLE else SelectModeConfig.MULTIPLE)
                ?.forSystemResult(callback)
        } else {
            PictureSelector.create(activity)
                ?.openGallery(SelectMimeType.ofImage())
                ?.setImageEngine(GlideBroadBroadEngine.createGlideEngine())
                ?.setMaxSelectNum(isNumber ?: 1)
                ?.setMinSelectNum(1)
                ?.isGif(isGif ?: false)
                ?.isWithSelectVideoImage(false)
                ?.isDisplayCamera(false)
                ?.setSelectionMode(if (isSingle) SelectModeConfig.SINGLE else SelectModeConfig.MULTIPLE)
                ?.forResult(callback)
        }
    }

    interface OnResultCallback {
        fun onResult(result: ArrayList<LocalMedia>?)
        fun onCancel()
    }
}