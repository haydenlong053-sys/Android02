package com.cboda.baselibrary.network

import com.cboda.baselibrary.network.config.NetworkBroadConfig
import com.cboda.baselibrary.network.interceptor.DecryptionBroadInterceptor
import com.cboda.baselibrary.network.interceptor.EncryptionBroadInterceptor
import com.cboda.baselibrary.network.interceptor.HeaderBroadInterceptor
import com.cboda.baselibrary.utils.getIsDebug
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * 网络管理类
 * 管理OkHttpClient和Retrofit实例
 *
 * @author CommonLibrary
 */
object NetworkBroadManager {

    @Volatile
    private var okHttpClient: OkHttpClient? = null

    @Volatile
    private var defaultRetrofit: Retrofit? = null

    /**
     * 获取OkHttpClient实例（单例）
     */
    fun getOkHttpClient(): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            okHttpClient ?: createOkHttpClient().also { okHttpClient = it }
        }
    }

    /**
     * 创建OkHttpClient
     */
    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(NetworkBroadConfig.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(NetworkBroadConfig.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(NetworkBroadConfig.writeTimeout, TimeUnit.SECONDS)
            .addInterceptor(HeaderBroadInterceptor())
            .addInterceptor(EncryptionBroadInterceptor())
            .addInterceptor(DecryptionBroadInterceptor())

        // 添加日志拦截器
        if (getIsDebug()) {
//            val loggingInterceptor = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }

            val loggingInterceptor = ResBroadInterceptor("AppHttpOkHttp")
            builder.addInterceptor(loggingInterceptor)
//            val loggingInterceptor = LiXinMyHttpLoggingInterceptor("OKGO")
//            //log打印级别，决定了log显示的详细程度
//            loggingInterceptor.setPrintLevel(com.lzy.okgo.interceptor.HttpLoggingInterceptor.Level.BODY)
//            //log颜色级别，决定了log在控制台显示的颜色
//            loggingInterceptor.setColorLevel(Level.INFO)
//            builder.addInterceptor(loggingInterceptor)

        }

        return builder.build()
    }

    /**
     * 获取默认Retrofit实例
     */
    fun getDefaultRetrofit(): Retrofit {
        return defaultRetrofit ?: synchronized(this) {
            defaultRetrofit ?: createRetrofit(NetworkBroadConfig.defaultBaseUrl).also {
                defaultRetrofit = it
            }
        }
    }

    /**
     * 创建指定baseUrl的Retrofit实例
     *
     * @param baseUrl 基础URL
     * @return Retrofit实例
     */
    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())

            .build()
    }

    /**
     * 创建API服务实例（使用默认baseUrl）
     *
     * @param serviceClass API服务接口类
     * @return API服务实例
     */
    fun <T> createApiService(serviceClass: Class<T>): T {
        return getDefaultRetrofit().create(serviceClass)
    }

    /**
     * 创建API服务实例（使用指定baseUrl）
     *
     * @param serviceClass API服务接口类
     * @param baseUrl 基础URL
     * @return API服务实例
     */
    fun <T> createApiService(serviceClass: Class<T>, baseUrl: String): T {
        return createRetrofit(baseUrl).create(serviceClass)
    }

    /**
     * 重置网络管理器（清除缓存的实例）
     * 当网络配置发生变化时调用
     */
    fun reset() {
        synchronized(this) {
            okHttpClient = null
            defaultRetrofit = null
        }
    }

    /**
     * 更新网络配置并重置实例
     *
     * @param baseUrl 基础URL
     * @param connectTimeout 连接超时时间（秒）
     * @param readTimeout 读取超时时间（秒）
     * @param writeTimeout 写入超时时间（秒）
     * @param enableLogging 是否启用日志
     */
    fun updateConfig(
        baseUrl: String? = null,
        connectTimeout: Long? = null,
        readTimeout: Long? = null,
        writeTimeout: Long? = null,
        enableLogging: Boolean? = null
    ) {
        baseUrl?.let { NetworkBroadConfig.setBaseUrl(it) }
        if (connectTimeout != null || readTimeout != null || writeTimeout != null) {
            NetworkBroadConfig.setTimeout(
                connectTimeout ?: NetworkBroadConfig.connectTimeout,
                readTimeout ?: NetworkBroadConfig.readTimeout,
                writeTimeout ?: NetworkBroadConfig.writeTimeout
            )
        }
        NetworkBroadConfig.setLoggingEnabled(getIsDebug())
        // 重置实例以应用新配置
        reset()
    }
}