package com.base.commonlibrary.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.base.commonlibrary.network.api.BaseApi
import com.base.commonlibrary.network.config.NetworkConfig
import com.base.commonlibrary.network.model.NetworkListResponse
import com.base.commonlibrary.network.model.NetworkListResponse2
import com.base.commonlibrary.network.model.NetworkResponse
import com.base.commonlibrary.utils.eLogD
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * 网络请求工具类
 * 提供便捷的网络请求方法
 *
 * @author CommonLibrary
 */
object NetworkUtils {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 网络请求回调接口
     */
    interface NetworkCallback<T> {
        /**
         * 请求成功
         * @param data 响应数据
         */
        fun onSuccess(data: T?)

        /**
         * 请求失败
         * @param code 错误码
         * @param message 错误信息
         */
        fun onFailure(code: Int, message: String, data: T?)

        /**
         * 网络错误
         * @param throwable 异常信息
         */
        fun onError(throwable: Throwable)
    }

    /**
     * 列表请求回调接口
     */
    interface ListCallback<T> {
        /**
         * 请求成功
         * @param list 列表数据
         * @param totalCount 总记录数
         * @param totalPage 总页数
         */
        fun onSuccess(list: List<T>?, totalCount: Int, totalPage: Int)

        /**
         * 请求失败
         * @param code 错误码
         * @param message 错误信息
         */
        fun onFailure(code: Int, message: String)

        /**
         * 网络错误
         * @param throwable 异常信息
         */
        fun onError(throwable: Throwable)
    }

    // ==================== GET请求 ====================

    /**
     * GET请求（泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun <T> get(
        url: String,
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.get(fullUrl, params) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * GET请求（兼容旧版本）
     *
     * @param url 请求URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun getAny(
        url: String,
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<Any>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        executeCall(api.get(fullUrl, params), callback)
    }

    /**
     * GET请求（使用指定baseUrl，泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun <T> getWithBaseUrl(
        url: String,
        baseUrl: String,
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<T>
    ) {
        val api = createApi(baseUrl)
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(baseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.get(fullUrl, params) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * GET请求（使用指定baseUrl，兼容旧版本）
     *
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun getAnyWithBaseUrl(
        url: String,
        baseUrl: String,
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<Any>
    ) {
        val api = createApi(baseUrl)
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(baseUrl, url)
        executeCall(api.get(fullUrl, params), callback)
    }

    /**
     * 加密GET请求（泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun <T> getEncrypted(
        url: String,
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.getEncrypted(fullUrl, params) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * 加密GET请求（兼容旧版本）
     *
     * @param url 请求URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun getEncryptedAny(
        url: String,
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<Any>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        executeCall(api.getEncrypted(fullUrl, params), callback)
    }

    // ==================== POST请求 ====================

    /**
     * POST请求（泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> post(
        url: String,
        body: Any,
        callback: NetworkCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.post(fullUrl, body) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * POST请求（兼容旧版本）
     *
     * @param url 请求URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun postAny(
        url: String,
        body: Any,
        callback: NetworkCallback<Any>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        executeCall(api.post(fullUrl, body), callback)
    }

    /**
     * POST请求（使用指定baseUrl，泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> postWithBaseUrl(
        url: String,
        baseUrl: String,
        body: Any,
        callback: NetworkCallback<T>
    ) {
        val api = createApi(baseUrl)
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(baseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.post(fullUrl, body) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * POST请求（使用指定baseUrl，兼容旧版本）
     *
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun postAnyWithBaseUrl(
        url: String,
        baseUrl: String,
        body: Any,
        callback: NetworkCallback<Any>
    ) {
        val api = createApi(baseUrl)
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(baseUrl, url)
        executeCall(api.post(fullUrl, body), callback)
    }

    /**
     * POST请求（表单格式，泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param params 表单参数
     * @param callback 回调接口
     */
    fun <T> postForm(
        url: String,
        params: Map<String, String>,
        callback: NetworkCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.postForm(fullUrl, params) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * POST请求（表单格式，兼容旧版本）
     *
     * @param url 请求URL
     * @param params 表单参数
     * @param callback 回调接口
     */
    fun postFormAny(
        url: String,
        params: Map<String, String>,
        callback: NetworkCallback<Any>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        executeCall(api.postForm(fullUrl, params), callback)
    }

    /**
     * 加密POST请求（泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> postEncrypted(
        url: String,
        body: Any,
        callback: NetworkCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.postEncrypted(fullUrl, body) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * 列表类型POST请求
     *
     * @param url 请求URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> postEncryptedList(
        url: String,
        body: Any,
        callback: ListCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeListCall2(
            api.postListEncrypted2(fullUrl, body) as Call<NetworkListResponse2<T>>,
            callback
        )
    }

    fun <T> postEncryptedListByDataKey(
        url: String,
        body: Any,
        callback: ListCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeListCall(
            api.postListEncrypted(fullUrl, body) as Call<NetworkListResponse<T>>,
            callback
        )
    }


    /**
     * 加密POST请求（兼容旧版本）
     *
     * @param url 请求URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun postEncryptedAny(
        url: String,
        body: Any,
        callback: NetworkCallback<Any>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        executeCall(api.postEncrypted(fullUrl, body), callback)
    }

    /**
     * 加密POST请求（表单格式，泛型版本）
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param params 表单参数
     * @param callback 回调接口
     */
    fun <T> postFormEncrypted(
        url: String,
        params: Map<String, String>,
        callback: NetworkCallback<T>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        @Suppress("UNCHECKED_CAST")
        executeCall(api.postFormEncrypted(fullUrl, params) as Call<NetworkResponse<T>>, callback)
    }

    /**
     * 加密POST请求（表单格式，兼容旧版本）
     *
     * @param url 请求URL
     * @param params 表单参数
     * @param callback 回调接口
     */
    fun postFormEncryptedAny(
        url: String,
        params: Map<String, String>,
        callback: NetworkCallback<Any>
    ) {
        val api = createApi()
        // 手动拼接完整URL，因为@Url注解会忽略baseUrl
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)
        executeCall(api.postFormEncrypted(fullUrl, params), callback)
    }

    // ==================== 列表请求 ====================

    /**
     * 列表类型GET请求
     *
     * @param url 请求URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun <T> getList(
        url: String,
        params: Map<String, String> = emptyMap(),
        callback: ListCallback<T>
    ) {
        val api = createApi()
        @Suppress("UNCHECKED_CAST")
        executeListCall(api.getList(url, params) as Call<NetworkListResponse<T>>, callback)
    }

    /**
     * 列表类型GET请求（使用指定baseUrl）
     *
     * @param url 请求URL
     * @param baseUrl 基础URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun <T> getList(
        url: String,
        baseUrl: String,
        params: Map<String, String> = emptyMap(),
        callback: ListCallback<T>
    ) {
        val api = createApi(baseUrl)
        @Suppress("UNCHECKED_CAST")
        executeListCall(api.getList(url, params) as Call<NetworkListResponse<T>>, callback)
    }

    /**
     * 列表类型POST请求
     *
     * @param url 请求URL
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> postList(
        url: String,
        body: Any,
        callback: ListCallback<T>
    ) {
        val api = createApi()
        @Suppress("UNCHECKED_CAST")
        executeListCall(api.postList(url, body) as Call<NetworkListResponse<T>>, callback)
    }

    /**
     * 加密的列表类型GET请求
     *
     * @param url 请求URL
     * @param params 查询参数
     * @param callback 回调接口
     */
    fun <T> getListEncrypted(
        url: String,
        params: Map<String, String> = emptyMap(),
        callback: ListCallback<T>
    ) {
        val api = createApi()
        @Suppress("UNCHECKED_CAST")
        executeListCall(api.getListEncrypted(url, params) as Call<NetworkListResponse<T>>, callback)
    }

    // ==================== 内部方法 ====================

    /**
     * 构建完整URL
     *
     * @param baseUrl 基础URL
     * @param relativePath 相对路径
     * @return 完整URL
     */
    private fun buildFullUrl(baseUrl: String, relativePath: String): String {
        // 如果相对路径已经是完整URL（包含协议），直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }

        // 确保baseUrl以/结尾
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        // 确保相对路径不以/开头（避免双斜杠）
        val normalizedRelativePath =
            if (relativePath.startsWith("/")) relativePath.substring(1) else relativePath

        return normalizedBaseUrl + normalizedRelativePath
    }

    /**
     * 创建API实例
     */
    internal fun createApi(baseUrl: String? = null): BaseApi {
        return if (baseUrl != null) {
            NetworkManager.createApiService(BaseApi::class.java, baseUrl)
        } else {
            NetworkManager.createApiService(BaseApi::class.java)
        }
    }

    /**
     * 执行网络请求
     */
    internal fun <T> executeCall(
        call: Call<NetworkResponse<T>>,
        callback: NetworkCallback<T>
    ) {
        call.enqueue(object : Callback<NetworkResponse<T>> {
            override fun onResponse(
                call: Call<NetworkResponse<T>>,
                response: Response<NetworkResponse<T>>
            ) {
                mainHandler.post {
                    if (response.isSuccessful) {
                        val networkResponse = response.body()
                        networkResponse.let {
//                            eLogD("返回体executeCall", "response-->${response}")
                        }
                        if (networkResponse != null) {
                            if (networkResponse.isRequestSuccess()) {
                                callback.onSuccess(networkResponse.data)
                            } else {
                                callback.onFailure(
                                    networkResponse.code,
                                    networkResponse.getErrorMessage() ?: "请求失败",
                                    networkResponse.data
                                )

                            }
                        } else {
                            callback.onFailure(-1, "响应数据为空", null)
                        }
                    } else {
                        // 这里解析 errorBody
                        val errorBody = response.errorBody()?.string()
                        try {
                            val gson = Gson()
                            val errorResponse: NetworkResponse<*>? =
                                gson.fromJson(
                                    errorBody,
                                    object : TypeToken<NetworkResponse<*>>() {}.type
                                )
                            callback.onFailure(
                                errorResponse?.code ?: response.code(),
                                errorResponse?.msg ?: "API 错误",
                                null
                            )
                        } catch (e: Exception) {
                            Log.e("NetworkUtils", "解析错误响应失败: $errorBody", e)
                            callback.onFailure(response.code(), "API 错误", null)
                        }

                    }
                }
            }

            override fun onFailure(call: Call<NetworkResponse<T>>, t: Throwable) {
                mainHandler.post {
                    callback.onError(t)
                }
            }
        })
    }

    /**
     * 执行列表网络请求
     */
    private fun <T> executeListCall(
        call: Call<NetworkListResponse<T>>,
        callback: ListCallback<T>
    ) {
        call.enqueue(object : Callback<NetworkListResponse<T>> {
            override fun onResponse(
                call: Call<NetworkListResponse<T>>,
                response: Response<NetworkListResponse<T>>
            ) {
                mainHandler.post {
                    if (response.isSuccessful) {
                        val networkResponse = response.body()
                        if (networkResponse != null) {
                            if (networkResponse.isRequestSuccess()) {
                                callback.onSuccess(
                                    networkResponse.getList(),
                                    networkResponse.totalCount,
                                    networkResponse.totalPage
                                )
                            } else {
                                callback.onFailure(
                                    networkResponse.code,
                                    networkResponse.getErrorMessage() ?: "请求失败"
                                )
                            }
                        } else {
                            callback.onFailure(-1, "响应数据为空")
                        }
                    } else {
                        callback.onFailure(response.code(), response.message())
                    }
                }
            }

            override fun onFailure(call: Call<NetworkListResponse<T>>, t: Throwable) {
                mainHandler.post {
                    callback.onError(t)
                }
            }
        })
    }


    /**
     * 执行列表网络请求2
     */
    private fun <T> executeListCall2(
        call: Call<NetworkListResponse2<T>>,
        callback: ListCallback<T>
    ) {
//        eLogD("NetWorkUtilsClass", "url:" + call.request().url.toString())
        call.enqueue(object : Callback<NetworkListResponse2<T>> {
            override fun onResponse(
                call: Call<NetworkListResponse2<T>>,
                response: Response<NetworkListResponse2<T>>
            ) {
//                eLogD("NetWorkUtilsClass", "return:" + call.request().url.toString())
//                eLogD("NetWorkUtilsClass", "response:" + response)
                mainHandler.post {
                    if (response.isSuccessful) {
                        val networkResponse = response.body()
                        if (networkResponse != null) {
                            networkResponse.let {
//                                eLogD(
//                                    "NetWorkUtilsClass",
//                                    "url:${call.request().url} ,返回ListResponse-->${
//                                        response.body().toString()
//                                    }"
//                                )
                            }
                            if (networkResponse.isRequestSuccess()) {
                                if (networkResponse.getMyDataList() != null) {
                                    callback.onSuccess(
                                        networkResponse.getMyDataList(),
                                        networkResponse.totalCount,
                                        networkResponse.totalPage
                                    )
                                } else {
                                    callback.onSuccess(
                                        networkResponse.getList(),
                                        networkResponse.totalCount,
                                        networkResponse.totalPage
                                    )
                                }

                            } else {
                                callback.onFailure(
                                    networkResponse.code,
                                    networkResponse.getErrorMessage() ?: "请求失败"
                                )
                            }
                        } else {
                            callback.onFailure(-1, "响应数据为空")
                        }
                    } else {
                        callback.onFailure(response.code(), response.message())
                    }
                }
            }

            override fun onFailure(call: Call<NetworkListResponse2<T>>, t: Throwable) {
                mainHandler.post {
                    callback.onError(t)
                }
            }
        })
    }


    // ==================== 文件上传 ====================

    /**
     * 单文件上传（泛型版本）
     * 使用 multipart/form-data 格式
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param file 要上传的文件
     * @param fileKey 文件字段名，默认为 "file"
     * @param params 其他表单参数
     * @param mediaType 文件MIME类型，默认为 "application/octet-stream"
     * @param callback 回调接口
     */
    fun <T> uploadFile(
        url: String,
        filePath: String,
        fileKey: String = "file",
        params: Map<String, String> = emptyMap(),
//        mediaType: String = getMediaTypeFromFileName(filePath),// "application/octet-stream",
        callback: NetworkCallback<T>
    ) {
        val file = File(filePath)
        val api = createApi()
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)

        // 创建文件 RequestBody
        val fileRequestBody =
            file.asRequestBody(getMediaTypeFromFileName(file.name).toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(fileKey, file.name, fileRequestBody)

        // 创建其他参数的 RequestBody
        val requestBodyMap = params.mapValues { (_, value) ->
            value.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        @Suppress("UNCHECKED_CAST")
        executeCall(
            api.uploadFile(fullUrl, filePart, requestBodyMap) as Call<NetworkResponse<T>>,
            callback
        )
    }

    /**
     * 单文件上传（兼容旧版本）
     * 使用 multipart/form-data 格式
     *
     * @param url 请求URL
     * @param file 要上传的文件
     * @param fileKey 文件字段名，默认为 "file"
     * @param params 其他表单参数
     * @param mediaType 文件MIME类型，默认为 "application/octet-stream"
     * @param callback 回调接口
     */
    fun uploadFileAny(
        url: String,
        filePath: String,
        fileKey: String = "file",
        params: Map<String, String> = emptyMap(),
        callback: NetworkCallback<Any>
    ) {
        uploadFile(url, filePath, fileKey, params, callback)
    }

    /**
     * 多文件上传（泛型版本）
     * 使用 multipart/form-data 格式
     *
     * @param T 期望的响应数据类型
     * @param url 请求URL
     * @param files 要上传的文件列表
     * @param fileKey 文件字段名，默认为 "files"
     * @param params 其他表单参数
     * @param mediaType 文件MIME类型，默认为 "application/octet-stream"
     * @param callback 回调接口
     */
    fun <T> uploadFiles(
        url: String,
        files: List<File>,
        fileKey: String = "files",
        params: Map<String, String> = emptyMap(),
        mediaType: String = "application/octet-stream",
        callback: NetworkCallback<T>
    ) {
        if (files.isEmpty()) {
            mainHandler.post {
                callback.onFailure(-1, "文件列表为空", null)
            }
            return
        }

        // 检查所有文件是否存在
        val notExistFiles = files.filter { !it.exists() }
        if (notExistFiles.isNotEmpty()) {
            mainHandler.post {
                callback.onFailure(
                    -1,
                    "部分文件不存在: ${notExistFiles.joinToString { it.name }}",
                    null
                )
            }
            return
        }

        val api = createApi()
        val fullUrl = buildFullUrl(NetworkConfig.defaultBaseUrl, url)

        // 创建文件 Parts
        val fileParts = files.map { file ->
            val fileRequestBody = file.asRequestBody(mediaType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(fileKey, file.name, fileRequestBody)
        }

        // 创建其他参数的 RequestBody
        val requestBodyMap = params.mapValues { (_, value) ->
            value.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        @Suppress("UNCHECKED_CAST")
        executeCall(
            api.uploadFiles(fullUrl, fileParts, requestBodyMap) as Call<NetworkResponse<T>>,
            callback
        )
    }

    /**
     * 多文件上传（兼容旧版本）
     * 使用 multipart/form-data 格式
     *
     * @param url 请求URL
     * @param files 要上传的文件列表
     * @param fileKey 文件字段名，默认为 "files"
     * @param params 其他表单参数
     * @param mediaType 文件MIME类型，默认为 "application/octet-stream"
     * @param callback 回调接口
     */
    fun uploadFilesAny(
        url: String,
        files: List<File>,
        fileKey: String = "files",
        params: Map<String, String> = emptyMap(),
        mediaType: String = "application/octet-stream",
        callback: NetworkCallback<Any>
    ) {
        uploadFiles(url, files, fileKey, params, mediaType, callback)
    }

    /**
     * 根据文件扩展名获取 MIME 类型
     * 辅助方法，用于自动识别文件类型
     *
     * @param fileName 文件名
     * @return MIME 类型字符串
     */
    fun getMediaTypeFromFileName(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "3gp" -> "video/3gpp"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "txt" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            else -> "application/octet-stream"
        }
    }


}