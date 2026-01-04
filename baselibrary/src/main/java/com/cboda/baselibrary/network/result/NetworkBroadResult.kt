package com.cboda.baselibrary.network.result

/**
 * 网络请求结果封装类
 * 支持三种状态：成功、业务失败、网络错误
 * 
 * @author CommonLibrary
 * @param T 数据类型
 */
sealed class NetworkBroadResult<out T> {
    
    /**
     * 请求成功
     * @param data 响应数据
     */
    data class BroadSuccess<T>(val data: T?) : NetworkBroadResult<T>()
    
    /**
     * 业务失败（API返回错误码）
     * @param code 错误码
     * @param message 错误信息
     * @param data 失败时的数据（可选）
     */
    data class BroadFailure<T>(val code: Int, val message: String, val data: T? = null) : NetworkBroadResult<T>()
    
    /**
     * 网络错误（网络异常、超时等）
     * @param throwable 异常信息
     */
    data class BroadError(val throwable: Throwable) : NetworkBroadResult<Nothing>()
    
    /**
     * 判断是否成功
     */
    val isSuccess: Boolean
        get() = this is BroadSuccess
    
    /**
     * 判断是否业务失败
     */
    val isFailure: Boolean
        get() = this is BroadFailure
    
    /**
     * 判断是否网络错误
     */
    val isError: Boolean
        get() = this is BroadError
    
    /**
     * 获取成功数据，如果不是成功状态则返回null
     */
    fun getOrNull(): T? = when (this) {
        is BroadSuccess -> data
        else -> null
    }
    
    /**
     * 获取成功数据，如果不是成功状态则返回默认值
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is BroadSuccess -> data ?: defaultValue
        else -> defaultValue
    }
    
    /**
     * 获取错误信息
     */
    fun getErrorMessage(): String? = when (this) {
        is BroadFailure -> message
        is BroadError -> throwable.message
        else -> null
    }
    
    /**
     * 获取错误码（仅业务失败时有效）
     */
    fun getErrorCode(): Int? = when (this) {
        is BroadFailure -> code
        else -> null
    }
    
    /**
     * 获取异常信息（仅网络错误时有效）
     */
    @JvmName("fetchThrowableForJvm")
    fun getThrowable(): Throwable? = when (this) {
        is BroadError -> throwable
        else -> null
    }
}

/**
 * 扩展函数：处理成功状态
 */
inline fun <T> NetworkBroadResult<T>.onSuccess(action: (data: T?) -> Unit): NetworkBroadResult<T> {
    if (this is NetworkBroadResult.BroadSuccess) {
        action(data)
    }
    return this
}

/**
 * 扩展函数：处理业务失败状态
 */
inline fun <T> NetworkBroadResult<T>.onFailure(action: (code: Int, message: String) -> Unit): NetworkBroadResult<T> {
    if (this is NetworkBroadResult.BroadFailure) {
        action(code, message)
    }
    return this
}

/**
 * 扩展函数：处理业务失败状态（包含data参数）
 */
inline fun <T> NetworkBroadResult<T>.onFailure(action: (code: Int, message: String, data: T?) -> Unit): NetworkBroadResult<T> {
    if (this is NetworkBroadResult.BroadFailure) {
        action(code, message, data)
    }
    return this
}

/**
 * 扩展函数：处理网络错误状态
 */
inline fun <T> NetworkBroadResult<T>.onError(action: (throwable: Throwable) -> Unit): NetworkBroadResult<T> {
    if (this is NetworkBroadResult.BroadError) {
        action(throwable)
    }
    return this
}

/**
 * 扩展函数：处理任何错误状态（业务失败或网络错误）
 */
inline fun <T> NetworkBroadResult<T>.onAnyError(action: (errorMessage: String) -> Unit): NetworkBroadResult<T> {
    when (this) {
        is NetworkBroadResult.BroadFailure -> action(message)
        is NetworkBroadResult.BroadError -> action(throwable.message ?: "网络错误")
        else -> {}
    }
    return this
}

/**
 * 扩展函数：数据转换
 */
inline fun <T, R> NetworkBroadResult<T>.map(transform: (T?) -> R?): NetworkBroadResult<R> {
    return when (this) {
        is NetworkBroadResult.BroadSuccess -> NetworkBroadResult.BroadSuccess(transform(data))
        is NetworkBroadResult.BroadFailure -> NetworkBroadResult.BroadFailure(code, message)
        is NetworkBroadResult.BroadError -> NetworkBroadResult.BroadError(throwable)
    }
}

/**
 * 扩展函数：数据转换（仅在成功时）
 */
inline fun <T, R> NetworkBroadResult<T>.mapSuccess(transform: (T?) -> R?): NetworkBroadResult<R> {
    return when (this) {
        is NetworkBroadResult.BroadSuccess -> NetworkBroadResult.BroadSuccess(transform(data))
        is NetworkBroadResult.BroadFailure -> NetworkBroadResult.BroadFailure(code, message)
        is NetworkBroadResult.BroadError -> NetworkBroadResult.BroadError(throwable)
    }
}

/**
 * 扩展函数：链式调用
 */
inline fun <T, R> NetworkBroadResult<T>.flatMap(transform: (T?) -> NetworkBroadResult<R>): NetworkBroadResult<R> {
    return when (this) {
        is NetworkBroadResult.BroadSuccess -> transform(data)
        is NetworkBroadResult.BroadFailure -> NetworkBroadResult.BroadFailure(code, message)
        is NetworkBroadResult.BroadError -> NetworkBroadResult.BroadError(throwable)
    }
}