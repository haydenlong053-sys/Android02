package com.cboda.baselibrary.network.example
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cboda.baselibrary.network.repository.NetworkBroadRepository
//import com.cboda.baselibrary.network.repository.NetworkListBroadRepository
//import com.cboda.baselibrary.network.result.NetworkBroadResult
//import com.cboda.baselibrary.network.result.onSuccess
//import com.cboda.baselibrary.network.result.onFailure
//import com.cboda.baselibrary.network.result.onError
//import com.cboda.baselibrary.network.result.onAnyError
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
///**
// * NetworkBroadResult 使用示例
// * 展示如何在 ViewModel 中使用新的网络请求结果处理方式
// *
// * @author CommonLibrary
// */
//
//// 示例数据类
//data class BroadUser(
//    val id: Int,
//    val name: String,
//    val email: String,
//    val role: String
//)
//
//data class LoginBroadRequest(
//    val username: String,
//    val password: String
//)
//
//data class LoginBroadResponse(
//    val token: String,
//    val userInfo: BroadUser
//)
//
//data class CreateMeetingRequest(
//    val roomName: String,
//    val startTime: String,
//    val duration: Int,
//    val maxParticipants: Int,
//    val allMic: Boolean,
//    val allMute: Boolean,
//    val isNonPayPwd: Boolean
//)
//
//// UI 状态
//data class UserUiState(
//    val isLoading: Boolean = false,
//    val user: BroadUser? = null,
//    val users: List<BroadUser> = emptyList(),
//    val errorMessage: String? = null,
//    val isNetworkError: Boolean = false
//)
//
///**
// * 示例 ViewModel：展示如何使用 NetworkBroadResult
// */
//class NetworkResultExampleViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(UserUiState())
//    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
//
//    /**
//     * 示例1：基础用法 - 获取用户信息
//     */
//    fun getUserInfo(userId: Int) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            val result = NetworkBroadRepository.get<BroadUser>(
//                url = "api/user/$userId",
//                params = emptyMap()
//            )
//
//            // 方式1：使用 when 表达式处理所有状态
//            when (result) {
//                is NetworkBroadResult.BroadSuccess -> {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        user = result.data,
//                        isNetworkError = false
//                    )
//                }
//                is NetworkBroadResult.BroadFailure -> {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = "获取用户信息失败: ${result.message} (错误码: ${result.code})",
//                        isNetworkError = false
//                    )
//                }
//                is NetworkBroadResult.BroadError -> {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = "网络错误: ${result.throwable.message}",
//                        isNetworkError = true
//                    )
//                }
//            }
//        }
//    }
//
//    /**
//     * 示例2：链式调用 - 用户登录
//     */
//    fun login(username: String, password: String) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            val request = LoginBroadRequest(username, password)
//
//            NetworkBroadRepository.post<LoginBroadResponse>(
//                url = "api/login",
//                body = request
//            )
//                .onSuccess { loginResponse ->
//                    // 登录成功，保存用户信息
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        user = loginResponse?.userInfo,
//                        isNetworkError = false
//                    )
//                    // 可以在这里保存 token
//                    loginResponse?.token?.let { token ->
//                        // saveToken(token)
//                    }
//                }
//                .onFailure { code, message ->
//                    // 业务失败：用户名密码错误、账户被禁用等
//                    val errorMsg = when (code) {
//                        401 -> "用户名或密码错误"
//                        403 -> "账户被禁用"
//                        else -> "登录失败: $message"
//                    }
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = errorMsg,
//                        isNetworkError = false
//                    )
//                }
//                .onError { throwable ->
//                    // 网络错误：网络超时、连接失败等
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = "网络连接失败，请检查网络设置",
//                        isNetworkError = true
//                    )
//                }
//        }
//    }
//
//    /**
//     * 示例3：简化错误处理 - 获取用户列表
//     */
//    fun getUserList(page: Int = 1, size: Int = 10) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            val params = mapOf(
//                "page" to page.toString(),
//                "size" to size.toString()
//            )
//
//            NetworkListBroadRepository.getList<BroadUser>(
//                url = "api/users",
//                params = params
//            )
//                .onSuccess { listResult ->
//                    // 成功获取用户列表
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        users = listResult?.list ?: emptyList(),
//                        isNetworkError = false
//                    )
//                }
//                .onAnyError { errorMessage ->
//                    // 统一处理所有错误（业务失败 + 网络错误）
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = errorMessage,
//                        isNetworkError = errorMessage.contains("网络")
//                    )
//                }
//        }
//    }
//
//    /**
//     * 示例4：数据转换 - 创建会议
//     */
//    fun createMeeting(request: CreateMeetingRequest) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            NetworkBroadRepository.post<String>(
//                url = "api/meeting/create",
//                body = request
//            )
//                .map { meetingId ->
//                    // 将会议ID转换为完整的会议信息
//                    "会议创建成功，会议ID: $meetingId"
//                }
//                .onSuccess { message ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = null
//                    )
//                    // 显示成功消息或导航到会议页面
//                    // showSuccessMessage(message)
//                }
//                .onFailure { code, message ->
//                    val errorMsg = when (code) {
//                        400 -> "会议参数错误: $message"
//                        403 -> "没有权限创建会议"
//                        409 -> "会议时间冲突"
//                        else -> "创建会议失败: $message"
//                    }
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = errorMsg
//                    )
//                }
//                .onError { throwable ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = "网络错误，请稍后重试",
//                        isNetworkError = true
//                    )
//                }
//        }
//    }
//
//    /**
//     * 示例5：条件处理 - 检查网络状态
//     */
//    fun checkNetworkAndLoadData() {
//        viewModelScope.launch {
//            val result = NetworkBroadRepository.get<BroadUser>(
//                url = "api/user/profile",
//                params = emptyMap()
//            )
//
//            // 根据结果类型进行不同处理
//            if (result.isSuccess) {
//                // 成功时的处理
//                val user = result.getOrNull()
//                _uiState.value = _uiState.value.copy(user = user)
//            } else if (result.isNetworkError) {
//                // 网络错误时显示重试按钮
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = "网络连接失败",
//                    isNetworkError = true
//                )
//            } else if (result.isFailure) {
//                // 业务错误时显示具体错误信息
//                val errorCode = result.getErrorCode()
//                val errorMessage = result.getErrorMessage()
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = "请求失败 (错误码: $errorCode): $errorMessage",
//                    isNetworkError = false
//                )
//            }
//        }
//    }
//
//    /**
//     * 示例6：链式数据处理 - 获取用户详情后获取权限
//     */
//    fun loadUserWithPermissions(userId: Int) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
//
//            // 先获取用户信息
//            val userResult = NetworkBroadRepository.get<BroadUser>(
//                url = "api/user/$userId",
//                params = emptyMap()
//            )
//
//            userResult
//                .onSuccess { user ->
//                    _uiState.value = _uiState.value.copy(user = user)
//
//                    // 成功后继续获取用户权限
//                    user?.let {
//                        loadUserPermissions(it.id)
//                    }
//                }
//                .onAnyError { errorMessage ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = errorMessage
//                    )
//                }
//        }
//    }
//
//    private suspend fun loadUserPermissions(userId: Int) {
//        NetworkBroadRepository.get<List<String>>(
//            url = "api/user/$userId/permissions",
//            params = emptyMap()
//        )
//            .onSuccess { permissions ->
//                _uiState.value = _uiState.value.copy(isLoading = false)
//                // 处理权限数据
//                // handlePermissions(permissions)
//            }
//            .onAnyError { errorMessage ->
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    errorMessage = "获取用户权限失败: $errorMessage"
//                )
//            }
//    }
//
//    /**
//     * 清除错误状态
//     */
//    fun clearError() {
//        _uiState.value = _uiState.value.copy(
//            errorMessage = null,
//            isNetworkError = false
//        )
//    }
//
//    /**
//     * 重试网络请求（当网络错误时）
//     */
//    fun retryLastRequest() {
//        if (_uiState.value.isNetworkError) {
//            // 根据具体情况重试相应的请求
//            // 这里以获取用户列表为例
//            getUserList()
//        }
//    }
//}
//
///**
// * 使用说明和最佳实践
// */
//object NetworkResultUsageGuide {
//
//    /**
//     * 最佳实践1：在 ViewModel 中的标准用法
//     */
//    fun standardUsageInViewModel() {
//        /*
//        viewModelScope.launch {
//            // 1. 设置加载状态
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            // 2. 发起网络请求
//            val result = NetworkBroadRepository.get<DataType>(url, params)
//
//            // 3. 处理结果
//            result
//                .onSuccess { data ->
//                    // 成功：更新UI状态
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        data = data
//                    )
//                }
//                .onFailure { code, message ->
//                    // 业务失败：显示具体错误信息
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = message
//                    )
//                }
//                .onError { throwable ->
//                    // 网络错误：显示网络错误提示
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = "网络连接失败",
//                        isNetworkError = true
//                    )
//                }
//        }
//        */
//    }
//
//    /**
//     * 最佳实践2：错误处理策略
//     */
//    fun errorHandlingStrategy() {
//        /*
//        // 策略1：区分错误类型
//        result
//            .onFailure { code, message ->
//                when (code) {
//                    401 -> handleUnauthorized()
//                    403 -> handleForbidden()
//                    404 -> handleNotFound()
//                    500 -> handleServerError()
//                    else -> handleGenericError(message)
//                }
//            }
//            .onError { throwable ->
//                when (throwable) {
//                    is SocketTimeoutException -> handleTimeout()
//                    is UnknownHostException -> handleNoNetwork()
//                    else -> handleNetworkError(throwable.message)
//                }
//            }
//
//        // 策略2：简化错误处理
//        result.onAnyError { errorMessage ->
//            showErrorMessage(errorMessage)
//        }
//        */
//    }
//
//    /**
//     * 最佳实践3：数据转换
//     */
//    fun dataTransformation() {
//        /*
//        // 转换数据格式
//        NetworkBroadRepository.get<ApiResponse>(url, params)
//            .map { apiResponse ->
//                // 将API响应转换为UI需要的格式
//                apiResponse?.data?.let { UiModel.fromApiData(it) }
//            }
//            .onSuccess { uiModel ->
//                updateUI(uiModel)
//            }
//
//        // 链式处理
//        NetworkBroadRepository.get<BroadUser>(url, params)
//            .flatMap { user ->
//                if (user != null) {
//                    NetworkBroadRepository.get<UserProfile>("api/user/${user.id}/profile")
//                } else {
//                    NetworkBroadResult.BroadFailure(-1, "用户信息为空")
//                }
//            }
//            .onSuccess { profile ->
//                updateUserProfile(profile)
//            }
//        */
//    }
//}