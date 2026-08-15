package com.cboda.baselibrary.network.interceptor

import android.text.TextUtils
import com.cboda.baselibrary.network.config.NetworkBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.APP_TOKEN
import com.cboda.baselibrary.utils.getAppVersion
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

/**
 * Header拦截器
 * 用于添加通用的请求头，如apptoken等
 * 
 * @author CommonLibrary
 */
class HeaderBroadInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        var appToken = NetworkBroadConfig.appToken ?: ""
        if (TextUtils.isEmpty(appToken)) {
            appToken = MMKVBroadUtils.getUserAppToken() ?: ""
        }
        // 添加apptoken
//        NetworkBroadConfig.appToken?.let { token ->
//            SharePrefBroadBroadUtil.getString(context, AppBroadConsts.APPTOKEN, "")


        requestBuilder.addHeader("App-Platform", "Android")
        requestBuilder.addHeader("App-Version", getAppVersion(null))
        requestBuilder.addHeader(APP_TOKEN, appToken)
        requestBuilder.addHeader("token", appToken)
        //测试钱包专用 到时候要去掉
        requestBuilder.addHeader("x-user-id", MMKVBroadUtils.getRealUserId() ?: "")
//        }

        // 添加其他通用header
        NetworkBroadConfig.commonHeaders.forEach { (key, value) ->
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

        // 添加多语言头（如果没有设置）
        if (originalRequest.header("Accept-Language") == null) {
            requestBuilder.addHeader("Accept-Language", NetworkBroadConfig.getNetAppLanguage())
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}