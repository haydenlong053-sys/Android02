# CommonLibrary 网络框架文档 (Kotlin版本)

## 概述

CommonLibrary 提供了一个基于 Retrofit 的完整网络请求框架，采用 Kotlin 编写，支持动态 BaseURL、请求加密、响应解密等功能。该框架设计简洁，易于使用，同时支持 Java 和 Kotlin 调用。

## 🎉 新功能：类型安全的泛型支持

从现在开始，`NetworkUtils` 支持泛型，你可以直接获得期望的数据类型，无需手动类型转换！

### 主要优势
- ✅ **类型安全**：编译时类型检查，避免 `ClassCastException`
- ✅ **无需类型转换**：直接得到期望的数据类型
- ✅ **IDE 智能提示**：完整的代码补全和类型检查
- ✅ **向后兼容**：现有代码无需修改，继续正常工作

### 快速示例

```kotlin
// 🎉 新方式：直接得到 MeetingConfig 类型
NetworkUtils.get<MeetingConfig>("api/meeting/config") { config ->
    // config 已经是 MeetingConfig 类型，无需转换！
    config?.let {
        println("续费规则: ${it.renewalRules}")
        println("支持免密支付: ${it.isNonPayPwd}")
    }
}

// ✅ 旧方式仍然支持（向后兼容）
NetworkUtils.get("api/meeting/config", object : NetworkUtils.NetworkCallback<Any> {
    override fun onSuccess(data: Any?) {
        val config = data as? MeetingConfig // 仍需手动转换
        // ...
    }
})
```

## 主要特性

- ✅ **动态BaseURL支持** - 可在运行时切换不同的API服务器
- ✅ **参数加密/解密** - 使用AES加密保护敏感数据
- ✅ **多种请求方式** - 支持GET、POST（JSON/表单）
- ✅ **智能响应处理** - 自动处理code、msg、success、data字段
- ✅ **列表数据支持** - 内置totalCount、totalPage分页信息
- ✅ **Java/Kotlin兼容** - 可在Java和Kotlin项目中使用
- ✅ **错误处理** - 统一的错误处理机制
- ✅ **日志记录** - 可配置的网络请求日志

## 快速开始

### 1. 初始化配置

```java
// 在Application中初始化
NetworkConfig.setDefaultBaseUrl("https://api.example.com/");
NetworkConfig.setConnectTimeout(30);
NetworkConfig.setReadTimeout(30);
NetworkConfig.setEnableLogging(true);

// 设置App Token
NetworkConfig.setAppToken("your_app_token_here");

// 添加通用请求头
NetworkConfig.addCommonHeader("User-Agent", "MyApp/1.0.0");
NetworkConfig.addCommonHeader("Platform", "Android");
NetworkConfig.addCommonHeader("Version", "1.0.0");

// 或者批量设置请求头
Map<String, String> headers = new HashMap<>();
headers.put("Device-ID", "device_12345");
headers.put("App-Version", "1.0.0");
headers.put("Channel", "official");
NetworkConfig.setCommonHeaders(headers);
```

### 2. 简单请求

```java
// GET请求
Map<String, Object> params = new HashMap<>();
params.put("page", 1);
params.put("size", 10);

NetworkUtils.get("user/list", params, new NetworkUtils.NetworkCallback<BaseResponse<Object>>() {
    @Override
    public void onSuccess(BaseResponse<Object> data) {
        // 请求成功
        Object result = data.getData();
    }
    
    @Override
    public void onFailure(int code, String message) {
        // 请求失败
        Log.e("Network", "Error: " + code + " - " + message);
    }
});

// POST请求
Map<String, Object> body = new HashMap<>();
body.put("username", "test");
body.put("password", "123456");

NetworkUtils.post("user/login", body, callback);
```

### 3. 加密请求

```java
// 加密GET请求
NetworkUtils.get("secure/data", params, true, callback);

// 加密POST请求
NetworkUtils.post("payment/create", body, true, callback);
```

### 4. 列表请求

```java
NetworkUtils.getList("product/search", params, new NetworkUtils.ListCallback<Object>() {
    @Override
    public void onSuccess(ListResponse<Object> data) {
        List<Object> list = data.getList();
        int totalCount = data.getTotalCount();
        int totalPage = data.getTotalPage();
    }
    
    @Override
    public void onFailure(int code, String message) {
        // 处理错误
    }
});
```

### 5. 动态BaseURL

```java
String customBaseUrl = "https://custom-api.example.com/";
NetworkUtils.get(customBaseUrl, "custom/endpoint", params, false, callback);
```

### 6. Header 管理

#### 设置 App Token
```java
// 设置App Token（会自动添加到所有请求的header中）
NetworkConfig.setAppToken("your_app_token_here");

// 清除App Token
NetworkConfig.setAppToken(null);
```

#### 管理通用请求头
```java
// 添加单个请求头
NetworkConfig.addCommonHeader("User-Agent", "MyApp/1.0.0");
NetworkConfig.addCommonHeader("Platform", "Android");

// 批量设置请求头
Map<String, String> headers = new HashMap<>();
headers.put("Device-ID", "android_device_001");
headers.put("Device-Model", "Samsung Galaxy S21");
headers.put("OS-Version", "Android 12");
headers.put("App-Version", "1.0.0");
NetworkConfig.setCommonHeaders(headers);

// 移除特定请求头
NetworkConfig.removeCommonHeader("Device-Model");

// 清空所有通用请求头
NetworkConfig.clearCommonHeaders();
```

#### 用户登录/登出场景
```java
// 用户登录后添加认证头
public void onUserLogin(String accessToken, String refreshToken) {
    NetworkConfig.addCommonHeader("Authorization", "Bearer " + accessToken);
    NetworkConfig.addCommonHeader("Refresh-Token", refreshToken);
    // 重置网络管理器以应用新配置
    NetworkManager.reset();
}

// 用户登出后清理认证头
public void onUserLogout() {
    NetworkConfig.removeCommonHeader("Authorization");
    NetworkConfig.removeCommonHeader("Refresh-Token");
    NetworkConfig.setAppToken(null);
    NetworkManager.reset();
}
```

## 高级用法

### 自定义API接口

```java
public interface UserApi {
    @GET("user/{id}")
    Call<BaseResponse<User>> getUserById(@Path("id") int userId);
    
    @Headers("X-Encrypt-Request: true")
    @POST("user/update")
    Call<BaseResponse<Object>> updateUserEncrypted(@Body User user);
    
    @Headers({"X-Encrypt-Request: true", "X-Decrypt-Response: true"})
    @POST("secure/operation")
    Call<BaseResponse<Object>> secureOperation(@Body Map<String, Object> params);
}

// 使用自定义接口
UserApi userApi = NetworkManager.getInstance().createService(UserApi.class);
userApi.getUserById(123).enqueue(callback);
```

### 直接使用Retrofit

```java
BaseApi api = NetworkManager.getInstance().createService(BaseApi.class);
api.get("endpoint", params).enqueue(callback);
```

## 数据模型

### BaseResponse

```java
public class BaseResponse<T> {
    private int code;        // 响应状态码
    private String msg;      // 响应消息
    private boolean success; // 请求是否成功
    private T data;          // 响应数据
    
    // 判断请求是否成功
    public boolean isRequestSuccess() {
        return code == 200 && success;
    }
}
```

### ListResponse

```java
public class ListResponse<T> extends BaseResponse<List<T>> {
    private int totalCount;  // 总记录数
    private int totalPage;   // 总页数
    
    public List<T> getList() {
        return getData();
    }
}
```

## 加密机制

### 请求加密

1. 添加请求头：`X-Encrypt-Request: true`
2. 将所有参数转换为JSON字符串
3. 使用AES加密JSON字符串
4. 将加密结果放入`encrData`字段
5. 最终请求参数：`{"encrData": "加密后的数据"}`

### 响应解密

1. 添加请求头：`X-Decrypt-Response: true`
2. 接收到响应后，使用AES解密响应体
3. 如果解密结果是JSON，自动解析为对象

### 同时加密请求和解密响应

```java
@Headers({"X-Encrypt-Request: true", "X-Decrypt-Response: true"})
@POST("secure/endpoint")
Call<BaseResponse<Object>> secureRequest(@Body Map<String, Object> params);
```

## 错误处理

框架会自动处理以下情况：

1. **网络错误** - 连接超时、DNS解析失败等
2. **HTTP错误** - 4xx、5xx状态码
3. **业务错误** - code=200但success=false的情况
4. **数据为空** - data字段为空的情况

```java
NetworkUtils.get("endpoint", params, new NetworkUtils.NetworkCallback<BaseResponse<Object>>() {
    @Override
    public void onSuccess(BaseResponse<Object> data) {
        // 只有在code=200且success=true时才会调用
        if (data.getData() != null) {
            // 处理数据
        } else {
            // 数据为空的情况
        }
    }
    
    @Override
    public void onFailure(int code, String message) {
        // 统一的错误处理
        // code: 错误码（-1表示网络错误，其他为HTTP状态码或业务错误码）
        // message: 错误信息
    }
});
```

## 配置选项

```java
// 网络配置
NetworkConfig.setDefaultBaseUrl("https://api.example.com/");
NetworkConfig.setConnectTimeout(30);  // 连接超时（秒）
NetworkConfig.setReadTimeout(30);     // 读取超时（秒）
NetworkConfig.setWriteTimeout(30);    // 写入超时（秒）
NetworkConfig.setEnableLogging(true); // 启用日志

// 重新初始化（配置变更后）
NetworkManager.getInstance().reinitialize();
```

### NetworkConfig 配置项
- `defaultBaseUrl`: 默认基础URL
- `connectTimeout`: 连接超时时间（秒）
- `readTimeout`: 读取超时时间（秒）
- `writeTimeout`: 写入超时时间（秒）
- `enableLogging`: 是否启用日志
- `appToken`: App Token，会自动添加到所有请求的header中
- `commonHeaders`: 通用请求头Map，会自动添加到所有请求中

## Kotlin使用示例

```kotlin
// 初始化
NetworkConfig.setDefaultBaseUrl("https://api.example.com/")

// GET请求
val params = mapOf("page" to 1, "size" to 10)
NetworkUtils.get("user/list", params, object : NetworkUtils.NetworkCallback<BaseResponse<Any>> {
    override fun onSuccess(data: BaseResponse<Any>) {
        // 请求成功
    }
    
    override fun onFailure(code: Int, message: String) {
        // 请求失败
    }
})

// POST请求
val body = mapOf("username" to "test", "password" to "123456")
NetworkUtils.post("user/login", body, callback)

// 加密请求
NetworkUtils.post("secure/endpoint", body, true, callback)
```

## 注意事项

1. **AES密钥** - 当前使用固定密钥，生产环境建议使用动态密钥
2. **BaseURL格式** - 必须以`/`结尾，框架会自动添加
3. **线程安全** - 所有回调都在主线程执行
4. **内存管理** - 请求完成后会自动释放资源
5. **错误重试** - 框架不提供自动重试，需要业务层实现

## 依赖要求

确保在`build.gradle`中添加以下依赖：

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.10.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
```

## 更新日志

### v1.0.0
- 初始版本
- 支持基本的GET/POST请求
- 支持参数加密/解密
- 支持动态BaseURL
- 支持列表数据处理
- Java/Kotlin兼容

