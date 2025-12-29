package com.cboda.baselibrary.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLConnection
import java.util.concurrent.TimeUnit

/**
 * 通用网络文件保存工具类（Android 10~16 适配）
 * 
 * 功能：
 * - 从 URL 下载任意文件并保存到外部存储（默认保存到 Downloads）
 * - API 29+ 使用 MediaStore（Downloads/Images/Video/Audio），API 28- 回退到公共下载目录
 * - 自动识别 MIME 类型并选择合适的媒体集合（Auto 模式）
 */
object NetworkFileSaver {

    enum class TargetCollection { Auto, Downloads, Images, Video, Audio }

    data class SaveResult(
        val success: Boolean,
        val uri: Uri? = null,
        val absolutePath: String? = null,
        val mimeType: String? = null,
        val bytesWritten: Long = 0,
        val error: String? = null
    )

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 从 URL 下载并保存文件（协程版）
     */
    @JvmStatic
    suspend fun saveFileFromUrl(
        context: Context,
        url: String,
        fileNameHint: String? = null,
        target: TargetCollection = TargetCollection.Downloads,
        subFolder: String? = "SeekWeb4"
    ): SaveResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SaveResult(false, error = "HTTP ${response.code}")
                }

                val contentType = response.header("Content-Type")
                val body = response.body ?: return@withContext SaveResult(false, error = "Empty body")
                val stream = body.byteStream()
                val contentLength = body.contentLength()

                val finalFileName = resolveFileName(url, fileNameHint, contentType)
                val mime = contentType ?: guessMimeType(finalFileName)

                return@withContext saveStreamInternal(
                    context = context,
                    input = stream,
                    fileName = finalFileName,
                    mimeType = mime,
                    target = resolveAutoTarget(target, mime),
                    subFolder = subFolder,
                    contentLength = if (contentLength > 0) contentLength else null
                )
            }
        } catch (e: Exception) {
            return@withContext SaveResult(false, error = e.message)
        }
    }

    /**
     * 以字节数组保存（协程版）
     */
    @JvmStatic
    suspend fun saveBytes(
        context: Context,
        bytes: ByteArray,
        fileName: String,
        mimeType: String? = null,
        target: TargetCollection = TargetCollection.Downloads,
        subFolder: String? = "SeekWeb4"
    ): SaveResult = withContext(Dispatchers.IO) {
        try {
            val mime = mimeType ?: guessMimeType(fileName)
            return@withContext saveStreamInternal(
                context,
                bytes.inputStream(),
                fileName,
                mime,
                resolveAutoTarget(target, mime),
                subFolder,
                contentLength = bytes.size.toLong()
            )
        } catch (e: Exception) {
            return@withContext SaveResult(false, error = e.message)
        }
    }

    /**
     * 阻塞版本，便于 Java 代码调用
     */
    @JvmStatic
    fun saveFileFromUrlBlocking(
        context: Context,
        url: String,
        fileNameHint: String? = null,
        target: TargetCollection = TargetCollection.Downloads,
        subFolder: String? = "SeekWeb4"
    ): SaveResult {
        return try {
            val request = Request.Builder().url(url).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return SaveResult(false, error = "HTTP ${response.code}")
                }
                val contentType = response.header("Content-Type")
                val body = response.body ?: return SaveResult(false, error = "Empty body")
                val stream = body.byteStream()
                val contentLength = body.contentLength()
                val finalFileName = resolveFileName(url, fileNameHint, contentType)
                val mime = contentType ?: guessMimeType(finalFileName)
                saveStreamInternal(
                    context,
                    stream,
                    finalFileName,
                    mime,
                    resolveAutoTarget(target, mime),
                    subFolder,
                    contentLength = if (contentLength > 0) contentLength else null
                )
            }
        } catch (e: Exception) {
            SaveResult(false, error = e.message)
        }
    }

    // --- 内部实现 ---

    private fun resolveAutoTarget(target: TargetCollection, mimeType: String?): TargetCollection {
        if (target != TargetCollection.Auto) return target
        val mt = mimeType ?: return TargetCollection.Downloads
        return when {
            mt.startsWith("image/") -> TargetCollection.Images
            mt.startsWith("video/") -> TargetCollection.Video
            mt.startsWith("audio/") -> TargetCollection.Audio
            else -> TargetCollection.Downloads
        }
    }

    private fun resolveFileName(url: String, fileNameHint: String?, contentType: String?): String {
        val hinted = fileNameHint?.trim()?.takeIf { it.isNotEmpty() }
        if (hinted != null) return hinted
        val rawName = url.substringAfterLast('/')
            .substringBefore('?')
            .takeIf { it.isNotEmpty() } ?: "download"
        // 如果没有扩展名，依据 MIME 添加常见扩展
        return if (rawName.contains('.')) rawName else {
            when {
                (contentType ?: "").startsWith("image/") -> "$rawName.jpg"
                (contentType ?: "").startsWith("video/") -> "$rawName.mp4"
                (contentType ?: "").startsWith("audio/") -> "$rawName.mp3"
                else -> rawName
            }
        }
    }

    private fun guessMimeType(fileName: String): String {
        return URLConnection.guessContentTypeFromName(fileName) ?: "application/octet-stream"
    }

    private fun saveStreamInternal(
        context: Context,
        input: InputStream,
        fileName: String,
        mimeType: String?,
        target: TargetCollection,
        subFolder: String?,
        contentLength: Long?
    ): SaveResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, input, fileName, mimeType, target, subFolder, contentLength)
        } else {
            saveLegacy(context, input, fileName, target, subFolder)
        }
    }

    private fun resolveCollection(target: TargetCollection): Uri {
        return when (target) {
            TargetCollection.Images -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            TargetCollection.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            TargetCollection.Audio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            TargetCollection.Downloads, TargetCollection.Auto -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }
    }

    private fun resolveRelativePath(target: TargetCollection, subFolder: String?): String {
        val base = when (target) {
            TargetCollection.Images -> Environment.DIRECTORY_PICTURES
            TargetCollection.Video -> Environment.DIRECTORY_MOVIES
            TargetCollection.Audio -> Environment.DIRECTORY_MUSIC
            TargetCollection.Downloads, TargetCollection.Auto -> Environment.DIRECTORY_DOWNLOADS
        }
        return if (subFolder.isNullOrBlank()) base else "$base/${subFolder}"
    }

    private fun saveWithMediaStore(
        context: Context,
        input: InputStream,
        fileName: String,
        mimeType: String?,
        target: TargetCollection,
        subFolder: String?,
        contentLength: Long?
    ): SaveResult {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                if (!mimeType.isNullOrBlank()) put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, resolveRelativePath(target, subFolder))
                if (contentLength != null && contentLength > 0) {
                    put(MediaStore.MediaColumns.SIZE, contentLength)
                }
            }
            val collection = resolveCollection(target)
            val resolver = context.contentResolver
            val uri = resolver.insert(collection, values)
                ?: return SaveResult(false, error = "Insert failed")
            resolver.openOutputStream(uri)?.use { out ->
                val bytesWritten = input.copyTo(out)
                return SaveResult(true, uri = uri, mimeType = mimeType, bytesWritten = bytesWritten)
            } ?: run {
                return SaveResult(false, error = "OpenOutputStream failed")
            }
        } catch (e: Exception) {
            SaveResult(false, error = e.message)
        }
    }

    private fun saveLegacy(
        context: Context,
        input: InputStream,
        fileName: String,
        target: TargetCollection,
        subFolder: String?
    ): SaveResult {
        return try {
            val baseDir = when (target) {
                TargetCollection.Images -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                TargetCollection.Video -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                TargetCollection.Audio -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                TargetCollection.Downloads, TargetCollection.Auto -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            val dir = if (subFolder.isNullOrBlank()) baseDir else File(baseDir, subFolder)
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, fileName)
            FileOutputStream(outFile).use { out ->
                val bytesWritten = input.copyTo(out)
                // 通知媒体扫描
                val uri = Uri.fromFile(outFile)
                context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                SaveResult(true, uri = uri, absolutePath = outFile.absolutePath, bytesWritten = bytesWritten)
            }
        } catch (e: Exception) {
            SaveResult(false, error = e.message)
        }
    }
}