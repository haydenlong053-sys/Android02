package com.cboda.baselibrary.network.api

import com.cboda.baselibrary.network.model.NetworkListResponse
import com.cboda.baselibrary.network.model.NetworkListResponse2
import com.cboda.baselibrary.network.model.NetworkResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/**
 * 基础API接口
 * 定义通用的网络请求方法
 *
 * @author CommonLibrary
 */
interface BaseApi {

    // ==================== 普通GET请求 ====================

    /**
     * 通用GET请求
     *
     * @param url 动态URL
     * @param params 查询参数
     * @return 响应数据
     */
    @GET
    fun get(
        @Url url: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Call<NetworkResponse<Any>>


    // ==================== 普通POST请求 ====================

    /**
     * 通用POST请求（JSON格式）
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 响应数据
     */
    @POST
    @Headers("Content-Type: application/json")
    fun post(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkResponse<Any>>


    /**
     * 通用POST请求（表单格式）
     *
     * @param url 动态URL
     * @param params 表单参数
     * @return 响应数据
     */
    @POST
    @FormUrlEncoded
    fun postForm(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Call<NetworkResponse<Any>>


    // ==================== 加密请求 ====================

    /**
     * 加密GET请求
     *
     * @param url 动态URL
     * @param params 查询参数
     * @return 响应数据
     */
    @GET
    @Headers("X-Encrypt-Request: true", "X-Decrypt-Response: true")
    fun getEncrypted(
        @Url url: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Call<NetworkResponse<Any>>


    /**
     * 加密POST请求（JSON格式）
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 响应数据
     */
    @POST
    @Headers(
        "Content-Type: application/json",
        "X-Encrypt-Request: true",
        "X-Decrypt-Response: true"
    )
    fun postEncrypted(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkResponse<Any>>


    /**
     * 加密POST请求（表单格式）
     *
     * @param url 动态URL
     * @param params 表单参数
     * @return 响应数据
     */
    @POST
    @FormUrlEncoded
    @Headers("X-Encrypt-Request: true", "X-Decrypt-Response: true")
    fun postFormEncrypted(
        @Url url: String,
        @FieldMap params: Map<String, String>
    ): Call<NetworkResponse<Any>>


    // ==================== 解密响应 ====================

    /**
     * 解密GET请求
     *
     * @param url 动态URL
     * @param params 查询参数
     * @return 响应数据
     */
    @GET
    @Headers("X-Decrypt-Response: true")
    fun getDecrypted(
        @Url url: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Call<NetworkResponse<Any>>

    /**
     * 解密POST请求
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 响应数据
     */
    @POST
    @Headers("Content-Type: application/json", "X-Decrypt-Response: true")
    fun postDecrypted(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkResponse<Any>>

    // ==================== 加密请求+解密响应 ====================

    /**
     * 加密请求并解密响应的GET请求
     *
     * @param url 动态URL
     * @param params 查询参数
     * @return 响应数据
     */
    @GET
    @Headers("X-Encrypt-Request: true", "X-Decrypt-Response: true")
    fun getEncryptedDecrypted(
        @Url url: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Call<NetworkResponse<Any>>

    /**
     * 加密请求并解密响应的POST请求
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 响应数据
     */
    @POST
    @Headers(
        "Content-Type: application/json",
        "X-Encrypt-Request: true",
        "X-Decrypt-Response: true"
    )
    fun postEncryptedDecrypted(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkResponse<Any>>

    // ==================== 列表类型请求 ====================

    /**
     * 列表类型GET请求
     *
     * @param url 动态URL
     * @param params 查询参数
     * @return 列表响应数据
     */
    @GET
    fun getList(
        @Url url: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Call<NetworkListResponse<Any>>

    /**
     * 列表类型POST请求
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 列表响应数据
     */
    @POST
    @Headers("Content-Type: application/json")
    fun postList(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkListResponse<Any>>

    /**
     * 加密的列表类型GET请求
     *
     * @param url 动态URL
     * @param params 查询参数
     * @return 列表响应数据
     */
    @GET
    @Headers("X-Encrypt-Request: true", "X-Decrypt-Response: true")
    fun getListEncrypted(
        @Url url: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Call<NetworkListResponse<Any>>

    /**
     * 加密的列表类型POST请求
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 列表响应数据
     */
    @POST
    @Headers(
        "Content-Type: application/json",
        "X-Encrypt-Request: true",
        "X-Decrypt-Response: true"
    )
    fun postListEncrypted(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkListResponse<Any>>

    /**
     * 加密的列表类型POST请求
     *
     * @param url 动态URL
     * @param body 请求体
     * @return 列表响应数据
     */
    @POST
    @Headers(
        "Content-Type: application/json",
        "X-Encrypt-Request: true",
        "X-Decrypt-Response: true"
    )
    fun postListEncrypted2(
        @Url url: String,
        @Body body: Any
    ): Call<NetworkListResponse2<Any>>


    /**
     * 单文件上传
     *
     * @param url 动态URL
     * @param filePart 文件部分
     * @param params 其他表单参数
     * @return 响应数据
     */
    @Multipart
    @POST
    @Headers("X-Decrypt-Response: true")
    fun uploadFile(
        @Url url: String,
        @Part filePart: MultipartBody.Part,
        @PartMap params: Map<String, @JvmSuppressWildcards okhttp3.RequestBody> = emptyMap()
    ): Call<NetworkResponse<Any>>

    /**
     * 多文件上传
     *
     * @param url 动态URL
     * @param fileParts 文件部分列表
     * @param params 其他表单参数
     * @return 响应数据
     */
    @Multipart
    @POST
    @Headers("X-Decrypt-Response: true")
    fun uploadFiles(
        @Url url: String,
        @Part fileParts: List<MultipartBody.Part>,
        @PartMap params: Map<String, @JvmSuppressWildcards okhttp3.RequestBody> = emptyMap()
    ): Call<NetworkResponse<Any>>
}