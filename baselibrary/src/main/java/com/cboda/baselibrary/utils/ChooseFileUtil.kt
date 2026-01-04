package com.cboda.baselibrary.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.cboda.baselibrary.toast.ToastUtils
import com.blankj.utilcode.util.FileUtils
import java.io.File
import kotlin.plus

object ChooseFileUtil {

    //选择PDF、Word、Excel文档Intent
    fun getChooseFileIntent(): Intent {
        val newIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        newIntent.addCategory(Intent.CATEGORY_OPENABLE)
        newIntent.type = "*/*"
        newIntent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf(
                "application/pdf",                                     // PDF
                "application/msword",                                  // .doc
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "application/vnd.ms-excel",                            // .xls
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"        // .xlsx
            )
        )
//        newIntent.putExtra(Intent.EXTRA_TITLE, "选择文档")
        return newIntent
    }

    //选择mp3,wav录音文件Intent
    fun getChooseAudioIntent(): Intent {
        val newIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        newIntent.addCategory(Intent.CATEGORY_OPENABLE)
        newIntent.type = "*/*"
        newIntent.putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf(
                "audio/mpeg",    // MP3
                "audio/wav",    // WAV
                "audio/x-wav",    // WAV
                "audio/wave",    // WAV
                "audio/ogg", //oog
                "audio/aac", //aac
                "audio/flac", //flac
                "audio/x-m4a" //m4a
//            ,"audio/ogg",
//            "audio/flac"
            )
        )
        return newIntent
    }


    fun getFileExtension(uri: Uri?, context: Context? = null): String? {
        return uri?.let {
            val mContext = getAppContext(context)
            // 1. 尝试获取 DISPLAY_NAME
            var fileName = getFileNameWithExtension(mContext, uri)
//            eLogD("ChooseFileUtilClass", "fileName:$fileName")
//            var fileName = ""
            // 2. 如果没有扩展名，或文件名为空，则根据 MIME 类型生成
            if (fileName.isNullOrEmpty() || !fileName.contains(".")) {
                fileName = getExtensionFromMimeType(mContext, uri)
//                eLogD("ChooseFileUtilClass", "fileName2:$fileName")
            }
            getFileExtensionByName(fileName)
        }
    }


    private fun getFileExtensionByName(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    private fun getExtensionFromMimeType(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
//        eLogD("ChooseFileUtilClass", "mime:$mime")
        return when (mime) {
            "audio/mpeg" -> ".mp3"
            "audio/x-wav", "audio/wav" -> ".wav"
            "audio/ogg" -> ".ogg"
            "audio/flac" -> ".flac"
            "audio/aac" -> ".aac"
            "audio/amr" -> ".amr"
            "audio/x-m4a" -> ".m4a"
            "application/pdf" -> ".pdf"
            "application/msword" -> ".doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx"
            "application/vnd.ms-excel" -> ".xls"
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> ".xlsx"
            else -> ".bin" // 未知类型
        }
    }

    private fun getFileSize(uri: Uri, context: Context): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else {
                    -1L // 无法获取大小
                }
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }

    private fun getFileExtensionAndSize(uri: Uri?, context: Context? = null): Pair<String?, Long?> {
        val mContext = getAppContext(context)
        return uri?.let {
            if (uri.scheme == "content") {
                mContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (displayNameIndex != -1 && sizeIndex != -1) {
                            Pair(
                                getFileExtensionByName(cursor.getString(displayNameIndex)),
                                cursor.getLong(sizeIndex)
                            )
                        } else {
                            Pair(null, null)
                        }
                    } else {
                        Pair(null, null)
                    }
                } ?: Pair(null, null)
            } else {
                Pair(getFileExtensionByName(uri.lastPathSegment ?: ""), null)
            }
        } ?: Pair(null, null)
    }

    private fun getFileNameWithExtension(context: Context, uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        cursor.getString(displayNameIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } else {
            uri.lastPathSegment // fallback for file://
        }
    }

    fun checkFileSizeAndIsLive(
        uri: Uri?,
        maxSize: Long = 100 * 1024 * 1024L,
        context: Context? = null,
        callback: (File) -> Unit = {}
    ) {
        uri?.also {
            val mContext = getAppContext(context)
            val pair = getFileExtensionAndSize(uri, mContext)
            if (pair.first != null && pair.second != null) {
                if (pair.second!! <= maxSize) {
                    val file = copyUriToFile(
                        uri,
                        pair.first,
                        mContext
                    )
                    if (null != file && FileUtils.isFileExists(file)) {
                        callback.invoke(file)
                    } else {
                        ToastUtils.show("选择的文件不存在！")
                    }
                } else {
                    ToastUtils.show("文件过大（上限 100MB）")
                }
            }
        }

    }

    private fun copyUriToFile(uri: Uri?, fileExtension: String?, context: Context?): File? {
        return uri?.let {
            val mContext = getAppContext(context)
            // 获取带扩展名的文件名（防止无扩展名）
            val fileName = getUuIdMd5() + "." + fileExtension
            // 复制到本地（可选）
            copyUriToFile(mContext, uri, File(mContext.cacheDir, fileName))
        }
    }

    private fun copyUriToFile(context: Context, uri: Uri, targetFile: File): File? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (_: Exception) {
            null
        }
    }
}