package com.cboda.baselibrary.network.model

/**
 * 网络请求基础响应数据模型
 * 适用于所有API接口的通用响应格式
 * 
 * @author CommonLibrary
 * @param T 数据类型
 */
open class NetworkResponse<T>(
    /**
     * 响应状态码
     */
    var code: Int = 0,
    
    /**
     * 响应消息
     */
    var msg: String? = null,
    
    /**
     * 请求是否成功
     */
    var success: Boolean = false,
    
    /**
     * 响应数据，可能为空
     */
    var data: T? = null
) {
    
    /**
     * 判断请求是否成功
     * @return true表示成功，false表示失败
     */
    fun isRequestSuccess(): Boolean {
        return code == 200 && success
    }
    
    /**
     * 获取错误信息
     * @return 错误信息，如果成功则返回null
     */
    fun getErrorMessage(): String? {
        return if (isRequestSuccess()) {
            null
        } else {
            msg ?: "请求失败"
        }
    }
    
    /**
     * 判断是否有数据
     * @return true表示有数据，false表示无数据
     */
    fun hasData(): Boolean {
        return data != null
    }
    
    override fun toString(): String {
        return "NetworkResponse(code=$code, msg='$msg', success=$success, data=$data)"
    }
}