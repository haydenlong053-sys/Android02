package com.base.commonlibrary.network.result

/**
 * 网络请求结果封装类
 * 支持三种状态：成功、业务失败、网络错误
 * 
 * @author CommonLibrary
 * @param T 数据类型
 */
sealed class NetworkResult<out T> {
    
    /**
     * 请求成功
     * @param data 响应数据
     */
    data class Success<T>(val data: T?) : NetworkResult<T>()
    
    /**
     * 业务失败（API返回错误码）
     * @param code 错误码
     * @param message 错误信息
     * @param data 失败时的数据（可选）
     */
    data class Failure<T>(val code: Int, val message: String, val data: T? = null) : NetworkResult<T>()
    
    /**
     * 网络错误（网络异常、超时等）
     * @param throwable 异常信息
     */
    data class Error(val throwable: Throwable) : NetworkResult<Nothing>()
    
    /**
     * 判断是否成功
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * 判断是否业务失败
     */
    val isFailure: Boolean
        get() = this is Failure
    
    /**
     * 判断是否网络错误
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * 获取成功数据，如果不是成功状态则返回null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * 获取成功数据，如果不是成功状态则返回默认值
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data ?: defaultValue
        else -> defaultValue
    }
    
    /**
     * 获取错误信息
     */
    fun getErrorMessage(): String? = when (this) {
        is Failure -> message
        is Error -> throwable.message
        else -> null
    }
    
    /**
     * 获取错误码（仅业务失败时有效）
     */
    fun getErrorCode(): Int? = when (this) {
        is Failure -> code
        else -> null
    }
    
    /**
     * 获取异常信息（仅网络错误时有效）
     */
    @JvmName("fetchThrowableForJvm")
    fun getThrowable(): Throwable? = when (this) {
        is Error -> throwable
        else -> null
    }
}

/**
 * 扩展函数：处理成功状态
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (data: T?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * 扩展函数：处理业务失败状态
 */
inline fun <T> NetworkResult<T>.onFailure(action: (code: Int, message: String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Failure) {
        action(code, message)
    }
    return this
}

/**
 * 扩展函数：处理业务失败状态（包含data参数）
 */
inline fun <T> NetworkResult<T>.onFailure(action: (code: Int, message: String, data: T?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Failure) {
        action(code, message, data)
    }
    return this
}

/**
 * 扩展函数：处理网络错误状态
 */
inline fun <T> NetworkResult<T>.onError(action: (throwable: Throwable) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(throwable)
    }
    return this
}

/**
 * 扩展函数：处理任何错误状态（业务失败或网络错误）
 */
inline fun <T> NetworkResult<T>.onAnyError(action: (errorMessage: String) -> Unit): NetworkResult<T> {
    when (this) {
        is NetworkResult.Failure -> action(message)
        is NetworkResult.Error -> action(throwable.message ?: "网络错误")
        else -> {}
    }
    return this
}

/**
 * 扩展函数：数据转换
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T?) -> R?): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Failure -> NetworkResult.Failure(code, message)
        is NetworkResult.Error -> NetworkResult.Error(throwable)
    }
}

/**
 * 扩展函数：数据转换（仅在成功时）
 */
inline fun <T, R> NetworkResult<T>.mapSuccess(transform: (T?) -> R?): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Failure -> NetworkResult.Failure(code, message)
        is NetworkResult.Error -> NetworkResult.Error(throwable)
    }
}

/**
 * 扩展函数：链式调用
 */
inline fun <T, R> NetworkResult<T>.flatMap(transform: (T?) -> NetworkResult<R>): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> transform(data)
        is NetworkResult.Failure -> NetworkResult.Failure(code, message)
        is NetworkResult.Error -> NetworkResult.Error(throwable)
    }
}