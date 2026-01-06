package com.lxkj.aiim.holder

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
import com.lxkj.aiim.utils.AppUtil

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/3/31.
 */
class ImagePickerHolder(private val activity: Context?) {
    var callBack: OnResultCallback? = null
    private var bottomHoder: BottomDialogHolder? = null
    private var handler :Handler? = null
    fun show(
        isVideo: Boolean = false,
        isSingle: Boolean = true,
        isNumber: Int? = 1,
        isGif: Boolean? = false
    ) {
        if (bottomHoder == null) {
            bottomHoder = BottomDialogHolder(activity, contentList = arrayListOf("拍摄", "从相册中选取"))
        }
        bottomHoder?.callBack = object : BottomDialogHolder.BottomDialogClickCallBack {
            override fun clickItem(position: Int) {
                when (position) {
                    0 -> {

                        val isGranted = XXPermissions.isGranted(activity!!, Permission.CAMERA)
                        if (isGranted) {
                            camera(isVideo)
                        } else {
                            val holder3 = CommanConfimHolder(
                                activity,
                                "温馨提示",
                                "${AppUtil.getAppName(activity)}需要相机权限,是否请求权限?",
                                "同意",
                                "拒绝"
                            )
                            holder3.callBack = object : CommanConfimHolder.OnConfimCallBack {
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
                            val holder3 = CommanConfimHolder(
                                activity,
                                "温馨提示",
                                "${AppUtil.getAppName(activity)}需要存储权限,是否请求权限?",
                                "同意",
                                "拒绝"
                            )
                            holder3.callBack = object : CommanConfimHolder.OnConfimCallBack {
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
//        var glideEngine = GlideBroadEngine().createGlideEngine()
//        //相册
//        PictureSelector.create(activity) //在Fragment中使用则是 fragment.this
//            ?.openGallery(if (isVideo) SelectMimeType.ofVideo() else SelectMimeType.ofImage()) //全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
//            ?.setImageEngine(glideEngine)
//            ?.setMaxVideoSelectNum(1) //视频最大选择数量
//            ?.setMaxSelectNum(isNumber?:1)
//            ?.setMinSelectNum(1)
//            ?.setMinVideoSelectNum(1) //视频最小选择数量
//            ?.isPreviewVideo(true) //是否支持预览视频
//            ?.isGif(isGif?:false) //是否显示gif文件
//            ?.setFilterVideoMinSecond(2)
//            ?.isMaxSelectEnabledMask(true) //达到最大选择数是否开启禁选蒙层
//            ?.setImageSpanCount(3) // 每行显示个数 int
//            ?.isDisplayCamera(false) // 是否显示拍照按钮 true or false
//            ?.forResult(object :
//                OnResultCallbackListener<LocalMedia> {
//                override fun onResult(result: ArrayList<LocalMedia>?) {
//                    if (result?.isNotEmpty() == true) {
//                        callBack?.onResult(result)
//                    }
//                }
//
//                override fun onCancel() {
//                    callBack?.onCancel()
//                }
//                }) //结果回调onActivityResult code


        PictureSelector.create(activity)
            ?.openSystemGallery(if (isVideo) SelectMimeType.ofVideo() else SelectMimeType.ofImage())
            ?.setSelectionMode(if (isSingle) SelectModeConfig.SINGLE else SelectModeConfig.MULTIPLE)
            ?.forSystemResult(object :
                OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    if (result?.isNotEmpty() == true) {
                        callBack?.onResult(result)
                    }
                }

                override fun onCancel() {
                    callBack?.onCancel()
                }

            })
    }

    interface OnResultCallback {
        fun onResult(result: ArrayList<LocalMedia>?)
        fun onCancel()
    }
}