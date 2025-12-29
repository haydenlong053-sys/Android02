# NetworkResult 网络请求结果处理框架

## 概述

`NetworkResult` 是一个增强的网络请求结果处理框架，用于替代原有的简单 `Result` 类。它能够准确区分和处理网络请求的三种不同状态：

- **Success**: 请求成功，业务逻辑正常
- **Failure**: 请求成功但业务逻辑失败（如用户名密码错误、权限不足等）
- **Error**: 网络层面的错误（如网络超时、连接失败等）

## 问题背景

原有的 `Result` 类只有 `onSuccess` 和 `onFailure` 两种状态，无法准确区分业务失败和网络错误，导致：

1. 无法为用户提供准确的错误提示
2. 无法实现针对性的错误处理策略
3. 难以区分是否需要重试请求

而 `NetworkCallback` 提供了三种状态：
- `onSuccess`: 业务成功
- `onFailure`: 业务失败（HTTP 200 但业务逻辑失败）
- `onError`: 网络错误（HTTP 非 200 或网络异常）

## 解决方案

### 1. NetworkResult 密封类

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T?) : NetworkResult<T>()
    data class Failure(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Error(val throwable: Throwable) : NetworkResult<Nothing>()
}
```

### 2. 扩展方法

提供丰富的扩展方法来处理不同场景：

```kotlin
// 基础处理方法
fun onSuccess(action: (T?) -> Unit): NetworkResult<T>
fun onFailure(action: (code: Int, message: String) -> Unit): NetworkResult<T>
fun onError(action: (throwable: Throwable) -> Unit): NetworkResult<T>
fun onAnyError(action: (message: String) -> Unit): NetworkResult<T>

// 数据转换方法
fun <R> map(transform: (T?) -> R?): NetworkResult<R>
fun <R> mapSuccess(transform: (T?) -> R?): NetworkResult<R>
fun <R> flatMap(transform: (T?) -> NetworkResult<R>): NetworkResult<R>

// 状态检查方法
val isSuccess: Boolean
val isFailure: Boolean
val isError: Boolean
val isNetworkError: Boolean
fun getOrNull(): T?
fun getErrorCode(): Int?
fun getErrorMessage(): String
```

### 3. Repository 扩展

提供协程支持的 Repository 扩展方法：

```kotlin
// GET 请求
suspend fun <T> NetworkRepository.get(url: String, params: Map<String, String>): NetworkResult<T>
suspend fun <T> NetworkRepository.getEncrypted(url: String, params: Map<String, String>): NetworkResult<T>
suspend fun <T> NetworkRepository.getList(url: String, params: Map<String, String>): NetworkListResult<T>

// POST 请求
suspend fun <T> NetworkRepository.post(url: String, body: Any): NetworkResult<T>
suspend fun <T> NetworkRepository.postForm(url: String, params: Map<String, String>): NetworkResult<T>
suspend fun <T> NetworkRepository.postEncrypted(url: String, body: Any): NetworkResult<T>
```

## 使用方法

### 1. 基础用法

```kotlin
class UserViewModel : ViewModel() {
    fun getUserInfo(userId: Int) {
        viewModelScope.launch {
            val result = NetworkRepository.get<User>("api/user/$userId", emptyMap())
            
            when (result) {
                is NetworkResult.Success -> {
                    // 处理成功情况
                    updateUI(result.data)
                }
                is NetworkResult.Failure -> {
                    // 处理业务失败
                    showError("获取用户信息失败: ${result.message}")
                }
                is NetworkResult.Error -> {
                    // 处理网络错误
                    showError("网络连接失败，请检查网络设置")
                }
            }
        }
    }
}
```

### 2. 链式调用

```kotlin
fun login(username: String, password: String) {
    viewModelScope.launch {
        NetworkRepository.post<LoginResponse>("api/login", LoginRequest(username, password))
            .onSuccess { loginResponse ->
                // 登录成功
                saveUserInfo(loginResponse?.userInfo)
                navigateToHome()
            }
            .onFailure { code, message ->
                // 业务失败：用户名密码错误等
                when (code) {
                    401 -> showError("用户名或密码错误")
                    403 -> showError("账户被禁用")
                    else -> showError("登录失败: $message")
                }
            }
            .onError { throwable ->
                // 网络错误
                showError("网络连接失败，请检查网络设置")
                showRetryButton()
            }
    }
}
```

### 3. 简化错误处理

```kotlin
fun getUserList() {
    viewModelScope.launch {
        NetworkListRepository.getList<User>("api/users", emptyMap())
            .onSuccess { listResult ->
                updateUserList(listResult?.list ?: emptyList())
            }
            .onAnyError { errorMessage ->
                // 统一处理所有错误
                showError(errorMessage)
            }
    }
}
```

### 4. 数据转换

```kotlin
fun createMeeting(request: CreateMeetingRequest) {
    viewModelScope.launch {
        NetworkRepository.post<String>("api/meeting/create", request)
            .map { meetingId ->
                "会议创建成功，会议ID: $meetingId"
            }
            .onSuccess { message ->
                showSuccessMessage(message)
            }
            .onAnyError { errorMessage ->
                showError(errorMessage)
            }
    }
}
```

## 错误处理策略

### 1. 区分错误类型

```kotlin
result
    .onFailure { code, message ->
        // 业务错误 - 根据错误码进行不同处理
        when (code) {
            401 -> handleUnauthorized()
            403 -> handleForbidden()
            404 -> handleNotFound()
            500 -> handleServerError()
            else -> handleGenericError(message)
        }
    }
    .onError { throwable ->
        // 网络错误 - 根据异常类型进行处理
        when (throwable) {
            is SocketTimeoutException -> handleTimeout()
            is UnknownHostException -> handleNoNetwork()
            else -> handleNetworkError(throwable.message)
        }
    }
```

### 2. 统一错误处理

```kotlin
result.onAnyError { errorMessage ->
    // 简化处理，统一显示错误信息
    showErrorMessage(errorMessage)
}
```

### 3. 条件处理

```kotlin
if (result.isSuccess) {
    val data = result.getOrNull()
    updateUI(data)
} else if (result.isNetworkError) {
    showRetryButton()
} else if (result.isFailure) {
    val errorCode = result.getErrorCode()
    val errorMessage = result.getErrorMessage()
    handleBusinessError(errorCode, errorMessage)
}
```

## 最佳实践

### 1. ViewModel 中的标准用法

```kotlin
fun loadData() {
    viewModelScope.launch {
        // 1. 设置加载状态
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        // 2. 发起网络请求
        val result = NetworkRepository.get<DataType>(url, params)
        
        // 3. 处理结果
        result
            .onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = data
                )
            }
            .onFailure { code, message ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
            .onError { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "网络连接失败",
                    isNetworkError = true
                )
            }
    }
}
```

### 2. 错误状态管理

```kotlin
data class UiState(
    val isLoading: Boolean = false,
    val data: DataType? = null,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false  // 区分网络错误和业务错误
)
```

### 3. 重试机制

```kotlin
fun retryRequest() {
    if (uiState.value.isNetworkError) {
        // 只有网络错误时才允许重试
        loadData()
    }
}
```

## 迁移指南

### 从原有 Result 迁移

**原有代码：**
```kotlin
networkCall { result ->
    result.onSuccess { data ->
        // 处理成功
    }.onFailure { message ->
        // 处理失败（无法区分错误类型）
    }
}
```

**新代码：**
```kotlin
viewModelScope.launch {
    val result = NetworkRepository.get<DataType>(url, params)
    result
        .onSuccess { data ->
            // 处理成功
        }
        .onFailure { code, message ->
            // 处理业务失败
        }
        .onError { throwable ->
            // 处理网络错误
        }
}
```

## 文件结构

```
network/
├── result/
│   ├── NetworkResult.kt          # 核心结果类
│   └── NetworkResultExtensions.kt # 扩展方法
├── repository/
│   ├── NetworkRepository.kt      # Repository 扩展
│   └── NetworkListRepository.kt  # 列表请求扩展
└── example/
    ├── NetworkResultExample.kt   # 使用示例
    └── README_NetworkResult.md   # 文档说明
```

## 总结

`NetworkResult` 框架解决了原有 `Result` 类无法区分业务失败和网络错误的问题，提供了：

1. **清晰的状态区分**：Success、Failure、Error 三种状态
2. **丰富的扩展方法**：链式调用、数据转换、条件处理
3. **协程支持**：与 ViewModel 和 Repository 模式完美集成
4. **灵活的错误处理**：支持统一处理和分类处理
5. **易于迁移**：保持与现有代码的兼容性

通过使用 `NetworkResult`，开发者可以更准确地处理网络请求结果，为用户提供更好的错误提示和用户体验。