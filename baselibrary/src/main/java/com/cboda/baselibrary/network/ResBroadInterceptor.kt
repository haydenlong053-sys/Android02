package com.cboda.baselibrary.network

import android.text.TextUtils
import com.cboda.baselibrary.utils.EncryptUtil
import com.cboda.baselibrary.utils.JavaBroadUtil.UStr_2_Str
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.gsonFormat
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * 功能：仅仅只是打印响应结果
 */
class ResBroadInterceptor(private val tAG: String, private val canEncrypt: Boolean = true) : Interceptor {

    companion object {
        /** 日志仅预览响应/请求体前 8KB，避免大 body + 并发请求导致 OOM */
        private const val MAX_LOG_BODY_BYTES = 8 * 1024L
        private const val MAX_LOG_MESSAGE_CHARS = 16 * 1024
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        try {
            logRequest(request, requestBody)
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            val resBody = try {
                response.peekBody(MAX_LOG_BODY_BYTES).string()
            } catch (e: Exception) {
                ""
            }
            var jmReqBody = ""
            try {
                if (!TextUtils.isEmpty(resBody) && canEncrypt) {
                    jmReqBody = EncryptUtil.aesDecrypt(resBody, "AES")
                }
            } catch (e: Exception) {
//                e.toLog(tAG)
            }
            val receive = "收到响应code:${response.code}\nurl：${request.url}\n响应headers： " +
                    "${response.headers}响应contentType：${response.body?.contentType()}" +
                    "\n响应body:${gsonFormat(UStr_2_Str(resBody))}${
                        if (!TextUtils.isEmpty(jmReqBody)) "\n解密body:${
                            gsonFormat(
                                UStr_2_Str(jmReqBody)
                            )
                        }" else ""
                    }\n响应时间================>:$tookMs"
            log(receive)
        } catch (e: Exception) {
//            e.toLog(tAG)
        }
        return response
    }

    fun logRequest(request: Request, requestBody: RequestBody?) {
        var reqBody = ""
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val contentType = requestBody.contentType()
            val charset = contentType?.charset(charsetUft8)
            if (null != charset) {
                reqBody = buffer.readString(charset)
            }
        }
        var jmReqBody = ""
        try {
            if (!TextUtils.isEmpty(reqBody)) {
                if (reqBody.contains("encrData")) {
                    val encryptionData = JSONObject(reqBody).getString("encrData")
                    jmReqBody = if (canEncrypt) {
                        EncryptUtil.aesDecrypt(encryptionData, "AES")
                    } else {
                        encryptionData
                    }
                }
            }
        } catch (e: Exception) {
//            e.toLog(tAG)
        }
        val send = if (request.url.toString().contains("AI/file")) {
            "发送请求method：${request.method}\nurl：${request.url}\n请求headers：" +
                    "${request.headers}请求contentType：${request.body?.contentType()}"

        } else {
            "发送请求method：${request.method}\nurl：${request.url}\n请求headers：" +
                    "${request.headers}请求contentType：${request.body?.contentType()}" +
                    "\n请求body:${gsonFormat(UStr_2_Str(reqBody))}" + if (!TextUtils.isEmpty(jmReqBody))
                "\n解密body:${gsonFormat(UStr_2_Str(jmReqBody))}" else ""
        }
        log(send)
    }

    private val charsetUft8 = Charset.forName("utf-8")

    private fun truncateForLog(message: String): String {
        if (message.length <= MAX_LOG_MESSAGE_CHARS) {
            return message
        }
        return message.take(MAX_LOG_MESSAGE_CHARS) +
                "...[truncated, totalChars=${message.length}]"
    }

    private fun log(message: String) {
        eLogD(tAG, truncateForLog(message))
    }
}