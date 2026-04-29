package com.cboda.baselibrary.network.config

import com.cboda.baselibrary.utils.getAppContext
import com.cboda.select.picture.utils.LanguageUtil.getAppLanguageKey
import com.cboda.select.picture.utils.LanguageUtil.getLanList
import com.cboda.select.picture.utils.LanguageUtil.getSystemLocal


/**
 * 网络配置类
 * 管理网络请求的全局配置
 * 
 * @author CommonLibrary
 */
object NetworkBroadConfig {

    /**
     * 默认基础URL
     */
    var defaultBaseUrl: String = "https://api.example.com/"

    /**
     * 连接超时时间（秒）
     */
    var connectTimeout: Long = 30

    /**
     * 读取超时时间（秒）
     */
    var readTimeout: Long = 30

    /**
     * 写入超时时间（秒）
     */
    var writeTimeout: Long = 30

    /**
     * 是否启用日志
     */
    var enableLogging: Boolean = true

    /**
     * App Token
     */
    var appToken: String? = null

    /**
     * 通用请求头
     */
    var commonHeaders: MutableMap<String, String> = mutableMapOf()

    /**
     * 初始化网络配置
     * 
     * @param baseUrl 基础URL
     * @param connectTimeout 连接超时时间（秒）
     * @param readTimeout 读取超时时间（秒）
     * @param writeTimeout 写入超时时间（秒）
     * @param enableLogging 是否启用日志
     */
    fun init(
        baseUrl: String,
        connectTimeout: Long = 30,
        readTimeout: Long = 30,
        writeTimeout: Long = 30,
        enableLogging: Boolean = true
    ) {
        this.defaultBaseUrl = baseUrl
        this.connectTimeout = connectTimeout
        this.readTimeout = readTimeout
        this.writeTimeout = writeTimeout
        this.enableLogging = enableLogging
    }

    /**
     * 设置基础URL
     * 
     * @param baseUrl 基础URL
     */
    fun setBaseUrl(baseUrl: String) {
        this.defaultBaseUrl = baseUrl
    }

    /**
     * 设置超时时间
     * 
     * @param connectTimeout 连接超时时间（秒）
     * @param readTimeout 读取超时时间（秒）
     * @param writeTimeout 写入超时时间（秒）
     */
    fun setTimeout(
        connectTimeout: Long = this.connectTimeout,
        readTimeout: Long = this.readTimeout,
        writeTimeout: Long = this.writeTimeout
    ) {
        this.connectTimeout = connectTimeout
        this.readTimeout = readTimeout
        this.writeTimeout = writeTimeout
    }

    /**
     * 设置日志开关
     * 
     * @param enable 是否启用日志
     */
    fun setLoggingEnabled(enable: Boolean) {
        this.enableLogging = enable
    }

    /**
     * 设置App Token
     * 
     * @param token App Token
     */
    fun updateAppToken(token: String?) {
        this.appToken = token
    }

    /**
     * 添加通用请求头
     * 
     * @param key 请求头名称
     * @param value 请求头值
     */
    fun addCommonHeader(key: String, value: String) {
        this.commonHeaders[key] = value
    }

    /**
     * 移除通用请求头
     * 
     * @param key 请求头名称
     */
    fun removeCommonHeader(key: String) {
        this.commonHeaders.remove(key)
    }

    /**
     * 清空所有通用请求头
     */
    fun clearCommonHeaders() {
        this.commonHeaders.clear()
    }

    /**
     * 批量设置通用请求头
     * 
     * @param headers 请求头Map
     */
    fun updateCommonHeaders(headers: Map<String, String>) {
        this.commonHeaders.clear()
        this.commonHeaders.putAll(headers)
    }


    //此处转换成app业务接口的多语言参数
    fun getNetAppLanguage(): String {
//        if (getSystemLocal(getAppContext(null)).language.startsWith("zh")) {
//            //中文
//            return "zh-CN"
//        } else {
//            return "en"
//        }
        val strValue = getLanList(getAppLanguageKey(getAppContext(null)))
        if ("en-US" == strValue || strValue.isEmpty()) {
            return "en"
        } else {
            return "zh-CN"
        }
    }
}