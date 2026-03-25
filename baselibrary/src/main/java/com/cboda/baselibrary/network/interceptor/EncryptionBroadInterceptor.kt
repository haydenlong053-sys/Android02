package com.cboda.baselibrary.network.interceptor

import com.cboda.baselibrary.utils.AESBroadUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getIsDebug
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException

/**
 * 加密拦截器
 * 根据请求头X-Encrypt-Request决定是否对请求参数进行加密
 *
 * @author CommonLibrary
 */
class EncryptionBroadInterceptor : Interceptor {

    companion object {
        private const val ENCRYPT_HEADER = "X-Encrypt-Request"
        private const val ENCRYPT_DATA_KEY = "encrData"
    }

    private val gson = Gson()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 检查是否需要加密
        val shouldEncrypt =
            originalRequest.header(ENCRYPT_HEADER)?.equals("true", ignoreCase = true) ?: false

        if (!shouldEncrypt) {
            return chain.proceed(originalRequest)
        }

        try {
            // 移除加密标识头
            val newRequestBuilder = originalRequest.newBuilder()
                .removeHeader(ENCRYPT_HEADER)

            // 处理请求体
            val originalBody = originalRequest.body
            if (originalBody != null) {
                val encryptedBody = encryptRequestBody(originalBody)
                newRequestBuilder.method(originalRequest.method, encryptedBody)
            }

            return chain.proceed(newRequestBuilder.build())
        } catch (e: Exception) {
            // 加密失败，返回原始请求
            return chain.proceed(originalRequest.newBuilder().removeHeader(ENCRYPT_HEADER).build())
        }
    }

    /**
     * 加密请求体
     */
    private fun encryptRequestBody(originalBody: RequestBody): RequestBody {
        val contentType = originalBody.contentType()
        val bodyString = bodyToString(originalBody)

        // 解析参数
        val params = when {
            contentType?.toString()?.contains("application/x-www-form-urlencoded") == true -> {
                parseFormParams(bodyString)
            }

            contentType?.toString()?.contains("application/json") == true -> {
                parseJsonParams(bodyString)
            }

            else -> {
                // 其他类型直接作为字符串处理
                mapOf("data" to bodyString)
            }
        }

        // 将参数转换为JSON字符串
        val jsonParams = gson.toJson(params)
        eLogD("请求体", jsonParams)
        // 加密参数
        val encryptedData = AESBroadUtil.encrypt(jsonParams)

        // 创建新的请求体，只包含加密后的数据
        val encryptedParams = mapOf(ENCRYPT_DATA_KEY to encryptedData)
        val encryptedJson = gson.toJson(encryptedParams)

        return encryptedJson.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    /**
     * 解析表单参数
     */
    private fun parseFormParams(body: String): Map<String, Any> {
        val params = mutableMapOf<String, Any>()
        if (body.isNotEmpty()) {
            body.split("&").forEach { pair ->
                val keyValue = pair.split("=", limit = 2)
                if (keyValue.size == 2) {
                    params[keyValue[0]] = keyValue[1]
                }
            }
        }
        return params
    }

    /**
     * 解析JSON参数
     */
    private fun parseJsonParams(body: String): Map<String, Any> {
        return try {
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(body, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            mapOf("data" to body)
        }
    }

    /**
     * 将RequestBody转换为字符串
     */
    private fun bodyToString(requestBody: RequestBody): String {
        return try {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            ""
        }
    }
}