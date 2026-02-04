package com.cboda.baselibrary.utils

import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.FileUtils.getFileExtension
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

object CompressionBroadUtil {

    private const val tAG = "CompressionUtilClass"

    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun imgCompression(
        context: Context? = null,
        path: String?,
        ignoreSize: Int = 100,
        callback: (String) -> Unit
    ) {
        eLogD(tAG, "imgCompressionSize:${FileUtils.getSize(path)} , $path")
        if (!TextUtils.isEmpty(path) && FileUtils.isFileExists(path)) {
            try {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val saveFilePath = getSaveImgFilePath()
                        if (FileUtils.createOrExistsDir(saveFilePath)) {
                            Luban.with(getAppContext(context))
                                .load(path)
                                .ignoreBy(ignoreSize)
                                .setTargetDir(saveFilePath)
                                .setCompressListener(object : OnCompressListener {
                                    override fun onStart() {

                                    }

                                    override fun onSuccess(
                                        index: Int,
                                        file: File?
                                    ) {
                                        val path1 =
                                            if (FileUtils.isFileExists(file)) file?.absolutePath else path
                                        eLogD(
                                            tAG,
                                            "压缩图片成功path1Size:${FileUtils.getSize(path1)} ,$path1"
                                        )
                                        callback.invoke(path1 ?: "")
                                    }

                                    override fun onError(index: Int, e: Throwable?) {
                                        eLogD(tAG, "压缩图片失败：${e?.message}")
                                        callback.invoke(path ?: "")
                                    }


                                }).launch()
                        } else {
                            eLogD(tAG, "imgCompression创建目录失败")
                            callback.invoke(path ?: "")
                        }
                    } catch (e: Exception) {
                        eLogD(tAG, "imgCompression异常1：${e.message}")
                        callback.invoke(path ?: "")
                    }
                }
            } catch (e: Exception) {
                eLogD(tAG, "imgCompression异常2：${e.message}")
                callback.invoke(path ?: "")
            }
        } else {
            eLogD(tAG, "imgCompression-----文件不存在")
            callback.invoke(path ?: "")
        }
    }


    fun imgCompression(
        context: Context? = null,
        file: File,
        callback: (Throwable?, File?) -> Unit
    ) {
        Luban.with(getAppContext(context)).load(file)
            .setCompressListener(object :
                OnCompressListener {
                override fun onStart() {

                }

                override fun onSuccess(index: Int, compressFile: File?) {
                    callback.invoke(null, compressFile)
                }

                override fun onError(index: Int, e: Throwable?) {
                    callback.invoke(e, null)
                }
            }).launch()
    }


    private fun compressVideo(path: String, block: (String?, String?, Float) -> Unit) {
//        eLogD(tAG, "开始压缩视频3")
        try {
            val targetPath = getSaveImgFilePath()
            if (FileUtils.createOrExistsDir(targetPath)) {
//                eLogD(tAG, "开始压缩视频5")
                val desPath =
                    targetPath + File.separator + getUuIdMd5() + "." + getFileExtension(path)
                val quality =
                    com.cboda.videocompressor.VideoController.COMPRESS_QUALITY_MEDIUM
//                        2 -> com.vincent.videocompressor.VideoController.COMPRESS_QUALITY_MEDIUM
//                        3 -> com.vincent.videocompressor.VideoController.COMPRESS_QUALITY_HIGH
//                        else -> com.vincent.videocompressor.VideoController.COMPRESS_QUALITY_LOW
//                    }
                com.cboda.videocompressor.VideoCompress.compressVideo(
                    path,
                    desPath,
                    quality,
                    object :
                        com.cboda.videocompressor.VideoCompress.CompressListener {
                        override fun onStart() {
//                            eLogD(tAG, "开始压缩视频6")
                        }

                        override fun onSuccess() {
                            eLogD(
                                tAG,
                                "压缩视频成功path1Size:${FileUtils.getSize(desPath)} ,$desPath"
                            )
                            block.invoke(
                                if (FileUtils.isFileExists(desPath)) desPath else path,
                                null, 100f
                            )
                        }

                        override fun onFail() {
                            eLogD(tAG, "压缩视频失败")
                            block.invoke(path, null, 100f)
                        }

                        override fun onProgress(percent: Float) {
//                            eLogD(tAG, "压缩视频percent:$percent")
                            block.invoke(null, null, percent)
                        }
                    })
            } else {
                eLogD(tAG, "compressVideo创建目录失败")
                block.invoke(path, null, 100f)
            }
        } catch (e: Exception) {
            eLogD(tAG, "videoCompression异常1：${e.message}")
            block.invoke(path, e.toLog(tAG), 100f)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun videoCompress(
        path: String?,
        viewModelScope: CoroutineScope? = null,
        block: (String?, String?, Float) -> Unit
    ) {
//        block.invoke(path ?: "", "")
//        showLoadingDialog()
        eLogD(tAG, "原视频videoCompressionSize:${FileUtils.getSize(path)} , $path")
        if (path != null) {
            if (FileUtils.isFileExists(path)) {
                try {
                    viewModelScope?.launch {
//                        eLogD(tAG, "开始压缩视频1")
                        withContext(Dispatchers.IO) {
                            compressVideo(path, block)
                        }
                    } ?: let {
//                        eLogD(tAG, "开始压缩视频2")
//                        ThreadUtils.getIoPool().execute {
                        GlobalScope.launch(Dispatchers.IO) {
                            compressVideo(path, block)
                        }
                    }
                } catch (e: Exception) {
                    eLogD(tAG, "videoCompression异常2：${e.message}")
                    block.invoke(path, e.toLog(tAG), 100f)
                }
            } else {
                eLogD(tAG, "videoCompression原视频文件不存在")
                block.invoke(path, "原视频文件不存在", 100f)
            }
        } else {
            eLogD(tAG, "videoCompression原视频路径为null")
            block.invoke(path ?: "", "原视频路径为null", 100f)
        }
    }

    @JvmStatic
    fun sendVideoCompress(
        path: String?,
        context: Context?,
        block: (String?, String?) -> Unit
    ) {
//        block.invoke(path ?: "", "")
        val inCompressedVideo = getAppContext(context).getString(R.string.in_compressed_video1)
        val loadingBroadDialog = showLoadingDialog(context, R.string.in_compressed_video2)
        videoCompress(path) { resultPath, errorMsg, percent ->
            if (!TextUtils.isEmpty(resultPath)) {
                dismissLoadingDialog()
                block.invoke(resultPath, errorMsg)
            } else {
                loadingBroadDialog?.updateTitle(inCompressedVideo + "(" + percent.toInt() + "%)...")
            }
        }
    }
}
