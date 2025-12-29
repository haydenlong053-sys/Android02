package com.base.commonlibrary.network.interceptor

import com.base.commonlibrary.network.config.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Header拦截器
 * 用于添加通用的请求头，如apptoken等
 * 
 * @author CommonLibrary
 */
class HeaderInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        // 添加apptoken
        NetworkConfig.appToken?.let { token ->
            requestBuilder.addHeader("apptoken", token)
            requestBuilder.addHeader("token", token)
        }
        
        // 添加其他通用header
        NetworkConfig.commonHeaders.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }
        
        // 添加默认Content-Type（如果没有设置）
        if (originalRequest.header("Content-Type") == null) {
            requestBuilder.addHeader("Content-Type", "application/json")
        }
        
        // 添加默认Accept（如果没有设置）
        if (originalRequest.header("Accept") == null) {
            requestBuilder.addHeader("Accept", "application/json")
        }
        
        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}