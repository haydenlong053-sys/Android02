package com.cboda.baselibrary.network.example

import com.cboda.baselibrary.network.NetworkBroadManager
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.api.BaseBroadApi
import com.cboda.baselibrary.network.config.NetworkBroadConfig
import com.cboda.baselibrary.network.model.NetworkListBroadResponse
import com.cboda.baselibrary.network.model.NetworkBroadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body

/**
 * 网络框架使用示例
 * 演示如何使用Kotlin版本的网络框架
 * 
 * @author CommonLibrary
 */
class NetworkBroadExample {
    
    /**
     * 示例数据模型
     */
    data class BroadUser(
        val id: Int,
        val name: String,
        val email: String,
        val age: Int
    )
    
    /**
     * 示例登录请求参数
     */
    data class LoginBroadRequest(
        val username: String,
        val password: String
    )
    
    /**
     * 示例登录响应数据
     */
    data class LoginBroadResponse(
        val token: String,
        val userId: Int,
        val username: String
    )
    
    /**
     * 自定义API接口示例
     */
    interface UserBroadApi {
        @GET("users")
        fun getUsers(@Query("page") page: Int): Call<NetworkListBroadResponse<BroadUser>>
        
        @GET("users/{id}")
        fun getUserById(@retrofit2.http.Path("id") id: Int): Call<NetworkBroadResponse<BroadUser>>
        
        @POST("login")
        fun login(@Body request: LoginBroadRequest): Call<NetworkBroadResponse<LoginBroadResponse>>
    }
    
    /**
     * 初始化网络配置示例
     */
//    fun initNetworkConfig() {
//        // 基础配置
//        NetworkBroadConfig.init(
//            baseUrl = "https://api.example.com/",
//            connectTimeout = 30,
//            readTimeout = 30,
//            writeTimeout = 30,
//            enableLogging = true
//        )
//
//        // 设置App Token
//        NetworkBroadConfig.updateAppToken("your_app_token_here")
//
//        // 添加通用请求头
//        NetworkBroadConfig.addCommonHeader("BroadUser-Agent", "MyApp/1.0.0")
//        NetworkBroadConfig.addCommonHeader("Platform", "Android")
//        NetworkBroadConfig.addCommonHeader("Version", "1.0.0")
//
//        // 或者批量设置
//        val headers = mapOf(
//            "Device-ID" to "device_12345",
//            "App-Version" to "1.0.0",
//            "Channel" to "official"
//        )
//        NetworkBroadConfig.updateCommonHeaders(headers)
//
//        println("网络配置初始化完成")
//        println("BaseURL: ${NetworkBroadConfig.defaultBaseUrl}")
//        println("超时设置: ${NetworkBroadConfig.connectTimeout}s")
//        println("日志开关: ${NetworkBroadConfig.enableLogging}")
//        println("App Token: ${NetworkBroadConfig.appToken}")
//        println("通用请求头: ${NetworkBroadConfig.commonHeaders}")
//    }
    
    /**
     * 简单GET请求示例
     */
    fun simpleGetExample() {
        val params = mapOf("page" to "1", "size" to "10")
        
        NetworkBroadUtils.get("api/users", params, object : NetworkBroadUtils.NetworkCallback<Any> {
            override fun onSuccess(data: Any?) {
                // 处理成功响应
                println("GET请求成功: $data")
            }
            
            override fun onFailure(code: Int, message: String, data: Any?) {
                // 处理业务失败
                println("GET请求失败: code=$code, message=$message, data=$data")
            }
            
            override fun onError(throwable: Throwable) {
                // 处理网络错误
                println("GET请求错误: ${throwable.message}")
            }
        })
    }
    
    /**
     * 简单POST请求示例
     */
    fun simplePostExample() {
        val requestBody = LoginBroadRequest("admin", "123456")
        
        NetworkBroadUtils.postAny("api/login", requestBody, object : NetworkBroadUtils.NetworkCallback<Any> {
            override fun onSuccess(data: Any?) {
                // 处理成功响应
                println("POST请求成功: $data")
            }
            
            override fun onFailure(code: Int, message: String, data: Any?) {
                // 处理业务失败
                println("POST请求失败: code=$code, message=$message, data=$data")
            }
            
            override fun onError(throwable: Throwable) {
                // 处理网络错误
                println("POST请求错误: ${throwable.message}")
            }
        })
    }
    
    /**
     * 表单POST请求示例
     */
    fun formPostExample() {
        val params = mapOf(
            "username" to "admin",
            "password" to "123456"
        )
        
        NetworkBroadUtils.postForm("api/login", params, object : NetworkBroadUtils.NetworkCallback<Any> {
            override fun onSuccess(data: Any?) {
                println("表单POST请求成功: $data")
            }
            
            override fun onFailure(code: Int, message: String, data: Any?) {
                println("表单POST请求失败: code=$code, message=$message, data=$data")
            }
            
            override fun onError(throwable: Throwable) {
                println("表单POST请求错误: ${throwable.message}")
            }
        })
    }
    
    /**
     * 加密请求示例
     */
    fun encryptedRequestExample() {
        val requestBody = LoginBroadRequest("admin", "123456")
        
        // 加密POST请求
        NetworkBroadUtils.postEncryptedAny("api/login", requestBody, object : NetworkBroadUtils.NetworkCallback<Any> {
            override fun onSuccess(data: Any?) {
                println("加密POST请求成功: $data")
            }
            
            override fun onFailure(code: Int, message: String, data: Any?) {
                println("加密POST请求失败: code=$code, message=$message, data=$data")
            }
            
            override fun onError(throwable: Throwable) {
                println("加密POST请求错误: ${throwable.message}")
            }
        })
        
        // 加密GET请求
        val params = mapOf("userId" to "123")
        NetworkBroadUtils.getEncryptedAny("api/user/profile", params, object : NetworkBroadUtils.NetworkCallback<Any> {
            override fun onSuccess(data: Any?) {
                println("加密GET请求成功: $data")
            }
            
            override fun onFailure(code: Int, message: String, data: Any?) {
                println("加密GET请求失败: code=$code, message=$message, data=$data")
            }
            
            override fun onError(throwable: Throwable) {
                println("加密GET请求错误: ${throwable.message}")
            }
        })
    }
    
    /**
     * 列表类型请求示例
     */
    fun listRequestExample() {
        val params = mapOf("page" to "1", "size" to "10")
        
        NetworkBroadUtils.getList<BroadUser>("api/users", params, object : NetworkBroadUtils.ListCallback<BroadUser> {
            override fun onSuccess(list: List<BroadUser>?, totalCount: Int, totalPage: Int) {
                println("列表请求成功: 共${totalCount}条数据，${totalPage}页")
                list?.forEach { user ->
                    println("用户: ${user.name}, 邮箱: ${user.email}")
                }
            }
            
            override fun onFailure(code: Int, message: String) {
                println("列表请求失败: code=$code, message=$message")
            }
            
            override fun onError(throwable: Throwable) {
                println("列表请求错误: ${throwable.message}")
            }
        })
    }
    
    /**
     * 动态BaseURL示例
     */
    fun dynamicBaseUrlExample() {
        val customBaseUrl = "https://api.custom.com/"
        val params = mapOf("id" to "123")
        
        NetworkBroadUtils.getAnyWithBaseUrl("api/data", customBaseUrl, params, object : NetworkBroadUtils.NetworkCallback<Any> {
            override fun onSuccess(data: Any?) {
                println("动态BaseURL请求成功: $data")
            }
            
            override fun onFailure(code: Int, message: String, data: Any?) {
                println("动态BaseURL请求失败: code=$code, message=$message, data=$data")
            }
            
            override fun onError(throwable: Throwable) {
                println("动态BaseURL请求错误: ${throwable.message}")
            }
        })
    }
    
    /**
     * 直接使用Retrofit接口示例
     */
    fun directRetrofitExample() {
        // 使用默认baseUrl创建API服务
        val baseApi = NetworkBroadManager.createApiService(BaseBroadApi::class.java)
        
        val params = mapOf("page" to "1")
        val call = baseApi.get("api/users", params)
        
        call.enqueue(object : Callback<NetworkBroadResponse<Any>> {
            override fun onResponse(call: Call<NetworkBroadResponse<Any>>, response: Response<NetworkBroadResponse<Any>>) {
                if (response.isSuccessful) {
                    val networkResponse = response.body()
                    if (networkResponse?.isRequestSuccess() == true) {
                        println("直接Retrofit请求成功: ${networkResponse.data}")
                    } else {
                        println("直接Retrofit请求失败: ${networkResponse?.getErrorMessage()}")
                    }
                }
            }
            
            override fun onFailure(call: Call<NetworkBroadResponse<Any>>, t: Throwable) {
                println("直接Retrofit请求错误: ${t.message}")
            }
        })
    }
    
    /**
     * 自定义API接口使用示例
     */
    fun customApiExample() {
        // 创建自定义API服务
        val userApi = NetworkBroadManager.createApiService(UserBroadApi::class.java)
        
        // 获取用户列表
        userApi.getUsers(1).enqueue(object : Callback<NetworkListBroadResponse<BroadUser>> {
            override fun onResponse(call: Call<NetworkListBroadResponse<BroadUser>>, response: Response<NetworkListBroadResponse<BroadUser>>) {
                if (response.isSuccessful) {
                    val listResponse = response.body()
                    if (listResponse?.isRequestSuccess() == true) {
                        println("获取用户列表成功，共${listResponse.totalCount}条数据")
                        listResponse.getList()?.forEach { user ->
                            println("用户: ${user.name}")
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<NetworkListBroadResponse<BroadUser>>, t: Throwable) {
                println("获取用户列表失败: ${t.message}")
            }
        })
        
        // 获取单个用户
        userApi.getUserById(123).enqueue(object : Callback<NetworkBroadResponse<BroadUser>> {
            override fun onResponse(call: Call<NetworkBroadResponse<BroadUser>>, response: Response<NetworkBroadResponse<BroadUser>>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.isRequestSuccess() == true) {
                        val user = userResponse.data
                        println("获取用户成功: ${user?.name}")
                    }
                }
            }
            
            override fun onFailure(call: Call<NetworkBroadResponse<BroadUser>>, t: Throwable) {
                println("获取用户失败: ${t.message}")
            }
        })
        
        // 用户登录
        val loginRequest = LoginBroadRequest("admin", "123456")
        userApi.login(loginRequest).enqueue(object : Callback<NetworkBroadResponse<LoginBroadResponse>> {
            override fun onResponse(call: Call<NetworkBroadResponse<LoginBroadResponse>>, response: Response<NetworkBroadResponse<LoginBroadResponse>>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.isRequestSuccess() == true) {
                        val loginData = loginResponse.data
                        println("登录成功: token=${loginData?.token}")
                    }
                }
            }
            
            override fun onFailure(call: Call<NetworkBroadResponse<LoginBroadResponse>>, t: Throwable) {
                println("登录失败: ${t.message}")
            }
        })
    }
    
    /**
     * 更新网络配置示例
     */
//    fun updateConfigExample() {
//        // 更新基础URL
//        NetworkBroadConfig.setBaseUrl("https://new-api.example.com/")
//
//        // 更新超时设置
//        NetworkBroadConfig.setTimeout(connectTimeout = 60, readTimeout = 60, writeTimeout = 60)
//
//        // 更新日志设置
//        NetworkBroadConfig.setLoggingEnabled(false)
//
//        // 更新App Token（比如用户登录后获取新token）
//        NetworkBroadConfig.updateAppToken("new_token_after_login")
//
//        // 动态添加或更新请求头
//        NetworkBroadConfig.addCommonHeader("Authorization", "Bearer new_access_token")
//        NetworkBroadConfig.addCommonHeader("Session-ID", "session_12345")
//
//        // 使用NetworkManager的便捷方法
//        NetworkBroadManager.updateConfig(
//            baseUrl = "https://api.v2.example.com/",
//            connectTimeout = 45,
//            enableLogging = true
//        )
//
//        println("网络配置已更新")
//        println("新的BaseURL: ${NetworkBroadConfig.defaultBaseUrl}")
//        println("新的App Token: ${NetworkBroadConfig.appToken}")
//        println("更新后的请求头: ${NetworkBroadConfig.commonHeaders}")
//    }
    
    /**
     * Header管理示例
     */
//    fun headerManagementExample() {
//        // 设置App Token
//        NetworkBroadConfig.updateAppToken("app_token_12345")
//
//        // 添加单个请求头
//        NetworkBroadConfig.addCommonHeader("BroadUser-Agent", "MyApp/2.0.0")
//        NetworkBroadConfig.addCommonHeader("Platform", "Android")
//
//        // 批量设置请求头
//        val deviceHeaders = mapOf(
//            "Device-ID" to "android_device_001",
//            "Device-Model" to "Samsung Galaxy S21",
//            "OS-Version" to "Android 12",
//            "App-Version" to "2.0.0"
//        )
//        NetworkBroadConfig.updateCommonHeaders(deviceHeaders)
//
//        // 移除特定请求头
//        NetworkBroadConfig.removeCommonHeader("Device-Model")
//
//        // 用户登录后添加认证相关头
//        fun onUserLogin(accessToken: String, refreshToken: String) {
//            NetworkBroadConfig.addCommonHeader("Authorization", "Bearer $accessToken")
//            NetworkBroadConfig.addCommonHeader("Refresh-Token", refreshToken)
//            // 重置网络管理器以应用新的header配置
//            NetworkBroadManager.reset()
//        }
//
//        // 用户登出后清理认证头
//        fun onUserLogout() {
//            NetworkBroadConfig.removeCommonHeader("Authorization")
//            NetworkBroadConfig.removeCommonHeader("Refresh-Token")
//            NetworkBroadConfig.updateAppToken(null) // 清除app token
//            // 重置网络管理器
//            NetworkBroadManager.reset()
//        }
//
//        println("Header管理示例完成")
//        println("当前App Token: ${NetworkBroadConfig.appToken}")
//        println("当前请求头: ${NetworkBroadConfig.commonHeaders}")
//    }
}