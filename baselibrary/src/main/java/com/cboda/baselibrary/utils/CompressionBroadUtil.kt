package com.cboda.baselibrary.utils

import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.FileUtils.getFileExtension
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


    private fun compressVideo(path: String, block: (String, String?) -> Unit) {

        try {
            val targetPath = getSaveImgFilePath()
            if (FileUtils.createOrExistsDir(targetPath)) {
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
                        }

                        override fun onSuccess() {
                            eLogD(
                                tAG,
                                "压缩视频成功path1Size:${FileUtils.getSize(desPath)} ,$desPath"
                            )
                            block.invoke(
                                if (FileUtils.isFileExists(desPath)) desPath else path,
                                null
                            )
                        }

                        override fun onFail() {
                            eLogD(tAG, "压缩视频失败")
                            block.invoke(path, null)
                        }

                        override fun onProgress(percent: Float) {

                        }
                    })
            } else {
                eLogD(tAG, "compressVideo创建目录失败")
                block.invoke(path, null)
            }
        } catch (e: Exception) {
            eLogD(tAG, "videoCompression异常1：${e.message}")
            block.invoke(path, e.toLog(tAG))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun videoCompress(
        path: String?,
        viewModelScope: CoroutineScope? = null,
        block: (String, String?) -> Unit
    ) {
//        block.invoke(path ?: "", "")
//        showLoadingDialog()
        eLogD(tAG, "videoCompressionSize:${FileUtils.getSize(path)} , $path")
        if (path != null) {
            if (FileUtils.isFileExists(path)) {
                try {
                    viewModelScope?.launch {
                        withContext(Dispatchers.IO) {
                            compressVideo(path, block)
                        }
                    } ?: let {
                        GlobalScope.launch(Dispatchers.IO) {
                            compressVideo(path, block)
                        }
                    }
                } catch (e: Exception) {
                    eLogD(tAG, "videoCompression异常2：${e.message}")
                    block.invoke(path, e.toLog(tAG))
                }
            } else {
                eLogD(tAG, "videoCompression原视频文件不存在")
                block.invoke(path, "原视频文件不存在")
            }
        } else {
            eLogD(tAG, "videoCompression原视频路径为null")
            block.invoke(path ?: "", "原视频路径为null")
        }
    }

    @JvmStatic
    fun sendVideoCompress(
        path: String?,
        block: (String, String?) -> Unit
    ) {
        block.invoke(path ?: "", "")
//        showLoadingDialog()
//        videoCompress(path) { resultPath, errorMsg ->
//            dismissLoadingDialog()
//            block.invoke(resultPath, errorMsg)
//        }
    }
}
