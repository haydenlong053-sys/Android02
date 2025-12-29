package com.base.commonlibrary.network.interceptor

import android.util.Log
import com.base.commonlibrary.utils.AESUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * 解密拦截器
 * 根据请求头X-Decrypt-Response决定是否对响应数据进行解密
 * 
 * @author CommonLibrary
 */
class DecryptionInterceptor : Interceptor {
    
    companion object {
        private const val DECRYPT_HEADER = "X-Decrypt-Response"
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // 检查是否需要解密
        val shouldDecrypt = request.header(DECRYPT_HEADER)?.equals("true", ignoreCase = true) ?: false
        
        if (!shouldDecrypt) {
            return response
        }


        return try {
            val responseBody = response.body
//            Log.d("DecryptionInterceptor", "Decrypting response body")
            if (responseBody != null) {
                val decryptedBody = decryptResponseBody(responseBody)
                response.newBuilder()
                    .body(decryptedBody)
                    .build()
            } else {
                response
            }
        } catch (e: Exception) {
            // 解密失败，返回原始响应
            response
        }
    }
    
    /**
     * 解密响应体
     */
    private fun decryptResponseBody(responseBody: ResponseBody): ResponseBody {
        val originalContent = responseBody.string()
//        Log.d("DecryptionInterceptor", "Decrypting $originalContent")
        
        // 尝试解密
        val decryptedContent = try {
            AESUtil.decrypt(originalContent)
        } catch (e: Exception) {
            // 解密失败，返回原始内容
            originalContent
        }
//        Log.d("DecryptionInterceptor", "decryptedContent $decryptedContent")
        
        // 创建新的响应体
        val contentType = responseBody.contentType() ?: "application/json; charset=utf-8".toMediaType()
        return decryptedContent.toResponseBody(contentType)
    }
}