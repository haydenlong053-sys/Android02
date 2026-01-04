package com.cboda.baselibrary.network.repository

import android.util.Log
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.result.NetworkBroadResult
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.utils.getIsDebug
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * NetworkBroadUtils 的 Repository 扩展
 * 提供基于 NetworkBroadResult 的协程支持
 *
 * @author CommonLibrary
 */
object NetworkBroadRepository {

    /**
     * 辅助函数：将失败时的data转换为目标类型
     */
    private fun <T> convertFailureData(data: Any?, clazz: Class<T>): T? {
        return try {
            data?.let {
                val gson = GsonUtils.getGson()
                val json = gson.toJson(it)
                gson.fromJson(json, clazz)
            }
        } catch (e: Exception) {
            eLogD("数据转换失败", "Failed to convert failure data: ${e.message}")
            null
        }
    }

    /**
     * 协程版本的 GET 请求（类型安全版本）
     *
     * @param url 请求URL
     * @param clazz 目标类型的 Class 对象
     * @param params 查询参数
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> get(
        url: String,
        clazz: Class<T>,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.get<Any>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<Any> {
                override fun onSuccess(data: Any?) {
                    try {
                        val result = if (data != null) {
                            // 使用 Gson 进行类型安全的转换
                            val gson = GsonUtils.getGson()
                            val json = gson.toJson(data)
                            gson.fromJson(json, clazz)
                        } else {
                            null
                        }
                        continuation.resume(NetworkBroadResult.BroadSuccess(result))
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }
                }

                override fun onFailure(code: Int, message: String, data: Any?) {
                    val convertedData = convertFailureData(data, clazz)
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, convertedData))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /***这里是api新生成的api start**/
    /**
     * 协程版本的列表 GET 请求
     * 当服务端返回 data 为数组(list)时，使用该方法。
     *
     * @param url 请求URL
     * @param params 查询参数
     * @return NetworkBroadResult<NetworkListBroadResult<T>>
     */
    suspend fun <T> getList(
        url: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getList<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的列表 GET 请求（指定 baseUrl）
     */
    suspend fun <T> getList(
        url: String,
        baseUrl: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getList<T>(
            url = url,
            baseUrl = baseUrl,
            params = params,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的列表 POST 请求
     */
    suspend fun <T> postList(
        url: String,
        body: Any
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postList<T>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密列表 GET 请求
     */
    suspend fun <T> getListEncrypted(
        url: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getListEncrypted<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密列表 POST 请求
     */
    suspend fun <T> postListEncrypted(
        url: String,
        clazz: Class<T>,
        body: Any
    ): NetworkBroadResult<List<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postEncryptedListByDataKey<T>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    try {
                        val gson = GsonUtils.getGson()
                        val json = gson.toJson(list)
                        val type = TypeToken.getParameterized(List::class.java, clazz).getType()
                        val result = gson.fromJson<List<T>>(json, type)
                        if (getIsDebug()) {
                            eLogD("data返回体list:", json)
                        }
                        continuation.resume(
                            NetworkBroadResult.BroadSuccess(result)
                        )
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }

                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /***end***/


    /**
     * 协程版本的 GET 请求（泛型版本，可能存在类型擦除问题）
     *
     * @param url 请求URL
     * @param params 查询参数
     * @return NetworkBroadResult<T>
     * @deprecated 使用 get(url, clazz, params) 方法以避免类型擦除问题
     */
    @Deprecated("使用 get(url, clazz, params) 方法以避免类型擦除问题")
    suspend fun <T> get(
        url: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.get<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的 GET 请求（指定 baseUrl）
     *
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param params 查询参数
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> get(
        url: String,
        baseUrl: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getWithBaseUrl<T>(
            url = url,
            baseUrl = baseUrl,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密 GET 请求
     *
     * @param url 请求URL
     * @param params 查询参数
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> getEncrypted(
        url: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getEncrypted<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的 POST 请求（类型安全版本）
     *
     * @param url 请求URL
     * @param clazz 响应数据类型的 Class 对象
     * @param body 请求体
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> post(
        url: String,
        clazz: Class<T>,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.post<Any>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<Any> {
                override fun onSuccess(data: Any?) {
                    try {
                        val gson = GsonUtils.getGson()
                        val json = gson.toJson(data)
                        val result = gson.fromJson(json, clazz)
                        continuation.resume(NetworkBroadResult.BroadSuccess(result))
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }
                }

                override fun onFailure(code: Int, message: String, data: Any?) {
                    val convertedData = convertFailureData(data, clazz)
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, convertedData))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的 POST 请求
     *
     * @param url 请求URL
     * @param body 请求体
     * @return NetworkBroadResult<T>
     */
    @Deprecated("使用 post(url, clazz, body) 方法以避免类型擦除问题")
    suspend fun <T> post(
        url: String,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.post<T>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的 POST 请求（指定 baseUrl）
     *
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param body 请求体
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> post(
        url: String,
        baseUrl: String,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postWithBaseUrl<T>(
            url = url,
            baseUrl = baseUrl,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的表单 POST 请求（类型安全版本）
     *
     * @param url 请求URL
     * @param clazz 响应数据类型的 Class 对象
     * @param params 表单参数
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> postForm(
        url: String,
        clazz: Class<T>,
        params: Map<String, String>
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postForm<Any>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<Any> {
                override fun onSuccess(data: Any?) {
                    try {
                        val gson = GsonUtils.getGson()
                        val json = gson.toJson(data)
                        val result = gson.fromJson(json, clazz)
                        continuation.resume(NetworkBroadResult.BroadSuccess(result))
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }
                }

                override fun onFailure(code: Int, message: String, data: Any?) {
                    val convertedData = convertFailureData(data, clazz)
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, convertedData))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的表单 POST 请求
     *
     * @param url 请求URL
     * @param params 表单参数
     * @return NetworkBroadResult<T>
     */
    @Deprecated("使用 postForm(url, clazz, params) 方法以避免类型擦除问题")
    suspend fun <T> postForm(
        url: String,
        params: Map<String, String>
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postForm<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密 POST 请求（类型安全版本）
     *
     * @param url 请求URL
     * @param clazz 响应数据类型的 Class 对象
     * @param body 请求体
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> postEncrypted(
        url: String,
        clazz: Class<T>,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postEncrypted<Any>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<Any> {
                override fun onSuccess(data: Any?) {
                    data?.let {
//                        eLogD("返回体",it.toString())
                    }
                    try {
                        val gson = GsonUtils.getGson()
                        val json = gson.toJson(data)
                        val result = gson.fromJson(json, clazz)
                        continuation.resume(NetworkBroadResult.BroadSuccess(result))
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }
                }

                override fun onFailure(code: Int, message: String, data: Any?) {
                    eLogD("请求失败", "code $code message $message")
                    data?.let {
                        eLogD("失败时的数据", it.toString())
                    }
                    val convertedData = convertFailureData(data, clazz)
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, convertedData))
                }

                override fun onError(throwable: Throwable) {
                    eLogD("请求失败", "onError: ${throwable.message}")
                    throwable.printStackTrace()
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    suspend fun <T> postEncryptedDataToList(
        url: String,
        clazz: Class<T>,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postEncrypted<Any>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<Any> {
                override fun onSuccess(data: Any?) {
                    data?.let {
                        eLogD("返回体", it.toString())
                    }
                    try {
                        val gson = GsonUtils.getGson()
                        val json = gson.toJson(data)
                        val result = gson.fromJson(json, clazz)
                        continuation.resume(NetworkBroadResult.BroadSuccess(result))
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }
                }

                override fun onFailure(code: Int, message: String, data: Any?) {
                    eLogD("请求失败", "code $code message $message")
                    data?.let {
                        eLogD("失败时的数据", it.toString())
                    }
                    val convertedData = convertFailureData(data, clazz)
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, convertedData))
                }

                override fun onError(throwable: Throwable) {
                    eLogD("请求失败", "onError: ${throwable.message}")
                    throwable.printStackTrace()
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密 POST 请求（类型安全版本）
     *
     * @param url 请求URL
     * @param clazz 响应数据类型的 Class 对象
     * @param body 请求体
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> postEncrypted2(
        url: String,
        clazz: Class<T>,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postEncrypted<Any>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<Any> {
                override fun onSuccess(data: Any?) {
                    if (getIsDebug()) {
                        eLogD("返回体", data.toString())
                    }
                    if (data == null) {
                        continuation.resume(NetworkBroadResult.BroadSuccess(data))
                    } else {
                        try {
                            val gson = GsonUtils.getGson()
                            val json = gson.toJson(data)
                            val result = gson.fromJson(json, clazz)
                            continuation.resume(NetworkBroadResult.BroadSuccess(result))
                        } catch (e: Exception) {
                            continuation.resume(NetworkBroadResult.BroadError(e))
                        }
                    }

                }

                override fun onFailure(code: Int, message: String, data: Any?) {
                    if (getIsDebug()) {
                        eLogD("请求失败", "code $code message $message")
                        data?.let {
                            eLogD("失败时的数据", it.toString())
                        }
                    }
                    val convertedData = convertFailureData(data, clazz)
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, convertedData))
                }

                override fun onError(throwable: Throwable) {
                    if (getIsDebug()) {
                        eLogD("请求失败", "onError: ${throwable.message}")
                    }
                    throwable.printStackTrace()
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }


    /**
     * 协程版本的列表 POST 请求
     *
     * @param url 请求URL
     * @param body 请求体
     * @return NetworkBroadResult<NetworkListBroadResult<T>>
     */
    suspend fun <T> postEncryptedList2(
        url: String,
        clazz: Class<T>,
        body: Any
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postEncryptedList<T>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    try {
                        val gson = GsonUtils.getGson()
                        val json = gson.toJson(list)
                        val type = TypeToken.getParameterized(List::class.java, clazz).getType()
                        val result = gson.fromJson<List<T>>(json, type)
                        if (getIsDebug()) {
                            eLogD("返回体listGson", result.toString())
                        }
                        continuation.resume(
                            NetworkBroadResult.BroadSuccess(NetworkListBroadResult(result, totalCount, totalPage))
                        )
                    } catch (e: Exception) {
                        continuation.resume(NetworkBroadResult.BroadError(e))
                    }


                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密 POST 请求
     *
     * @param url 请求URL
     * @param body 请求体
     * @return NetworkBroadResult<T>
     */
    @Deprecated("使用 postEncrypted(url, clazz, body) 方法以避免类型擦除问题")
    suspend fun <T> postEncrypted(
        url: String,
        body: Any
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postEncrypted<T>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的加密表单 POST 请求
     *
     * @param url 请求URL
     * @param params 表单参数
     * @return NetworkBroadResult<T>
     */
    suspend fun <T> postFormEncrypted(
        url: String,
        params: Map<String, String>
    ): NetworkBroadResult<T> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postFormEncrypted<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                override fun onSuccess(data: T?) {
                    continuation.resume(NetworkBroadResult.BroadSuccess(data))
                }

                override fun onFailure(code: Int, message: String, data: T?) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    suspend fun <T> postFile(url: String, filePath: String): NetworkBroadResult<T> =
        suspendCancellableCoroutine { continuation ->
            NetworkBroadUtils.uploadFile(
                url,
                filePath,
                callback = object : NetworkBroadUtils.NetworkBroadCallback<T> {
                    override fun onSuccess(data: T?) {
//                        data?.let {
//                            eLogD("postFileClass返回体", it.toString())
//                        }
                        eLogD("postFileClass", "onSuccess")
                        continuation.resume(NetworkBroadResult.BroadSuccess(data))
                    }

                    override fun onFailure(code: Int, message: String, data: T?) {
                        eLogD("postFileClass", "onFailure:$code ,$message")
                        continuation.resume(NetworkBroadResult.BroadFailure(code, message, data))
                    }

                    override fun onError(throwable: Throwable) {
                        eLogD("postFileClass", "onError:${throwable.toLog()}")
                        continuation.resume(NetworkBroadResult.BroadError(throwable))
                    }
                })
        }

}

/**
 * 列表请求结果
 */
data class NetworkListBroadResult<T>(
    val list: List<T>?,
    val totalCount: Int,
    val totalPage: Int
)

/**
 * 列表请求的 Repository 扩展
 */
object NetworkListBroadRepository {

    /**
     * 协程版本的列表 GET 请求
     *
     * @param url 请求URL
     * @param params 查询参数
     * @return NetworkBroadResult<NetworkListBroadResult<T>>
     */
    suspend fun <T> getList(
        url: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getList<T>(
            url = url,
            params = params,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的列表 GET 请求（指定 baseUrl）
     *
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param params 查询参数
     * @return NetworkBroadResult<NetworkListBroadResult<T>>
     */
    suspend fun <T> getList(
        url: String,
        baseUrl: String,
        params: Map<String, String> = emptyMap()
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.getList<T>(
            url = url,
            baseUrl = baseUrl,
            params = params,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }

    /**
     * 协程版本的列表 POST 请求
     *
     * @param url 请求URL
     * @param body 请求体
     * @return NetworkBroadResult<NetworkListBroadResult<T>>
     */
    suspend fun <T> postList(
        url: String,
        body: Any
    ): NetworkBroadResult<NetworkListBroadResult<T>> = suspendCancellableCoroutine { continuation ->
        NetworkBroadUtils.postList<T>(
            url = url,
            body = body,
            callback = object : NetworkBroadUtils.ListBroadCallback<T> {
                override fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int) {
                    continuation.resume(
                        NetworkBroadResult.BroadSuccess(
                            NetworkListBroadResult(list, totalCount, totalPage)
                        )
                    )
                }

                override fun onFailure(code: Int, message: String) {
                    continuation.resume(NetworkBroadResult.BroadFailure(code, message))
                }

                override fun onError(throwable: Throwable) {
                    continuation.resume(NetworkBroadResult.BroadError(throwable))
                }
            }
        )
    }


}