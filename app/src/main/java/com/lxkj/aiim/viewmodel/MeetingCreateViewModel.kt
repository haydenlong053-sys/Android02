package com.lxkj.aiim.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.wildfirechat.remote.ChatManager
import com.base.commonlibrary.network.result.onError
import com.base.commonlibrary.network.result.onFailure
import com.base.commonlibrary.network.result.onSuccess
import com.lxkj.aiim.model.CreateMeetingRequest
import com.lxkj.aiim.model.CreateMeetingResponse
import com.lxkj.aiim.model.MeetingCreateUiState
import com.lxkj.aiim.model.TimeOption
import com.lxkj.aiim.model.UserTierOption
import com.lxkj.aiim.model.PaymentPasswordVerifyRequest
import com.lxkj.aiim.model.PriceQueryRequest
import com.lxkj.aiim.repository.MeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MeetingCreateViewModel(
    private val repository: MeetingRepository = MeetingRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MeetingCreateUiState())
    val uiState: StateFlow<MeetingCreateUiState> = _uiState.asStateFlow()
    
    init {
        loadMeetingConfig()
        initializeDefaultValues()
    }
    
    /**
     * 加载会议配置
     */
    private fun loadMeetingConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getMeetingConfig()
                .onSuccess { config ->
                    config?.let {
                        // 为TimeOption添加基于索引的id（如果服务器没有返回id或id重复）
                        val processedTimeOptions = config.timeOline.mapIndexed { index, timeOption ->
                            // 如果id为0或者与其他选项重复，则使用index+1作为id
                            val needsNewId = timeOption.id == 0 || 
                                           config.timeOline.count { it.id == timeOption.id } > 1
                            
                            if (needsNewId) {
                                timeOption.copy(id = index + 1)
                            } else {
                                timeOption
                            }
                        }
                        
                        val updatedConfig = config.copy(timeOline = processedTimeOptions)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            config = updatedConfig,
                            allMic = config.allMic,
                            allMute = config.allMute,
                            isNonPayPwd = config.isNonPayPwd
                        )
                        
                        Log.d("MeetingCreateViewModel", "Processed TimeOptions: ${processedTimeOptions.map { "${it.name}(id=${it.id})" }}")
                    }
                }
                .onFailure { code, message, data ->
                    Log.e("MeetingCreateViewModel", "加载会议配置失败onFailure: $message, data: $data")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = message ?: "加载配置失败"
                    )
                }
                .onError { error ->
                    Log.e("MeetingCreateViewModel", "加载会议配置失败onError: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "加载配置失败"
                    )
                }
        }
    }
    
    /**
     * 初始化默认值
     */
    private fun initializeDefaultValues() {
        val currentDate = SimpleDateFormat("yyyy年M月d日", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        _uiState.value = _uiState.value.copy(
            selectedDate = currentDate,
            selectedTime = currentTime
        )
    }
    
    /**
     * 更新会议室名称
     */
    fun updateRoomName(name: String) {
        _uiState.value = _uiState.value.copy(roomName = name)
    }

    /**
     * 更新会议室ID
     */
    fun updateGroupId(groupId: String) {
        _uiState.value = _uiState.value.copy(groupId = groupId)
    }
    
    /**
     * 更新选中的日期
     */
    fun updateSelectedDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
    
    /**
     * 更新选中的时间
     */
    fun updateSelectedTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }
    
    /**
     * 更新选中的时长
     */
    fun updateSelectedDuration(duration: TimeOption) {
        _uiState.value = _uiState.value.copy(selectedDuration = duration)
        // 选择时长后实时获取价格
        queryPriceIfReady()
    }
    
    /**
     * 更新选中的用户人数
     */
    fun updateSelectedUserTier(userTier: UserTierOption) {
        _uiState.value = _uiState.value.copy(selectedUserTier = userTier)
        // 选择人数后实时获取价格
        queryPriceIfReady()
    }
    
    
    
    /**
     * 切换全禁麦状态
     */
    fun toggleAllMic() {
        _uiState.value = _uiState.value.copy(allMic = !_uiState.value.allMic)
    }
    
    /**
     * 切换全禁言状态
     */
    fun toggleAllMute() {
        _uiState.value = _uiState.value.copy(allMute = !_uiState.value.allMute)
    }
    
    /**
     * 切换续时免密支付状态
     */
    fun toggleNonPayPwd() {
        _uiState.value = _uiState.value.copy(isNonPayPwd = !_uiState.value.isNonPayPwd)
    }
    
    /**
     * 检查是否可以查询价格，如果时长和人数都已选择则查询价格
     */
    private fun queryPriceIfReady() {
        val currentState = _uiState.value
        val duration = currentState.selectedDuration?.value
        val userCount = currentState.selectedUserTier?.value
        
        if (duration != null && userCount != null) {
            queryMeetingPrice(duration, userCount)
        }
    }
    
    /**
     * 查询会议价格
     */
    private fun queryMeetingPrice(duration: Int, userCount: Int) {
        viewModelScope.launch {
            // 设置价格加载状态
            _uiState.value = _uiState.value.copy(isPriceLoading = true)
            
            val request = PriceQueryRequest(
                ownerId = ChatManager.Instance().userId, // 这里应该从用户信息中获取
                meetingTime = duration.toString(),
                meetingMaxUsers = userCount.toString()
            )
            
            repository.queryMeetingPrice(request)
                .onSuccess { response ->
                    response?.let {
                        _uiState.value = _uiState.value.copy(
                            currentPrice = response.discountMeetingTokens.toDouble(),
                            originalPrice = response.meetingTokens.toDouble(),
                            availableTokens = response.availableTokens.toDouble(),
                            isPriceLoading = false
                        )
                    }
                }
                .onFailure { code, msg, data ->
                    Log.e("MeetingCreateViewModel", "价格查询失败onFailure: code=$code, msg=$msg, data=$data")
                    // 价格查询失败，保持原有价格，只取消加载状态
                    _uiState.value = _uiState.value.copy(isPriceLoading = false)
                    // 可以选择是否显示错误提示，这里暂时不显示
                }
                .onError {
                    _uiState.value = _uiState.value.copy(isPriceLoading = false)
                    // 可以选择是否显示错误提示，这里暂时不显示
                }
        }
    }
    
    /**
     * 检查支付密码状态并开始创建会议流程
     */
    fun startCreateMeetingFlow( onError: (String) -> Unit) {
        // 先进行详细的表单验证
        val validationResult = validateMeetingForm()
        if (!validationResult.isValid) {
            // 显示iOS风格的Toast提示
            onError(validationResult.errorMessage ?: "请完善会议信息")
            return
        }
        
        // 验证通过，继续检查支付密码状态
        checkPaymentPasswordStatus( onError)
    }
    
    /**
     * 详细的表单验证
     */
    private fun validateMeetingForm(): ValidationResult {
        val currentState = _uiState.value
        val missingFields = mutableListOf<String>()
        
        // 检查会议室名称
        if (currentState.roomName.isBlank()) {
            missingFields.add("会议室名称")
        }
        
//        // 检查开始日期
//        if (currentState.selectedDate.isBlank()) {
//            missingFields.add("开始日期")
//        }
//
//        // 检查开始时间
//        if (currentState.selectedTime.isBlank()) {
//            missingFields.add("开始时间")
//        }
        
        // 检查会议时长
        if (currentState.selectedDuration == null) {
            missingFields.add("会议时长")
        }
        
        // 检查会议人数
        if (currentState.selectedUserTier == null) {
            missingFields.add("会议人数")
        }
        
        // 检查是否正在加载
        if (currentState.isLoading) {
            return ValidationResult(false, "正在加载中，请稍后再试")
        }
        
        return if (missingFields.isEmpty()) {
            ValidationResult(true, null)
        } else {
            val message = when (missingFields.size) {
                1 -> "请选择${missingFields[0]}"
                2 -> "请选择${missingFields[0]}和${missingFields[1]}"
                else -> "请完善以下信息：${missingFields.joinToString("、")}"
            }
            ValidationResult(false, message)
        }
    }
    
    /**
     * 验证结果数据类
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
    
    /**
     * 检查支付密码状态
     */
    private fun checkPaymentPasswordStatus( onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.checkPaymentPasswordStatus()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasPaymentPassword = response.hasPassword
                    )
                    
                    if (response.hasPassword) {
                        // 已设置支付密码，显示密码输入弹窗
                        _uiState.value = _uiState.value.copy(showPaymentPasswordDialog = true)
                    } else {
                        // 未设置支付密码，显示设置密码提示弹窗
                        _uiState.value = _uiState.value.copy(showSetPasswordDialog = true)
                    }
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "检查支付密码状态失败"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
        }
    }
    
    /**
     * 验证支付密码
     */
    fun verifyPaymentPassword(password: String, onSuccess: (CreateMeetingResponse?) -> Unit, onError: (String,Int) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                paymentPasswordLoading = true,
                paymentPasswordError = null
            )
            
            val request = PaymentPasswordVerifyRequest(password)
            repository.verifyPaymentPassword(request)
                .onSuccess { response ->
                    if (response.success) {
                        // 密码验证成功，关闭弹窗并创建会议
                        _uiState.value = _uiState.value.copy(
                            paymentPasswordLoading = false,
                            showPaymentPasswordDialog = false
                        )
                        createMeeting(onSuccess = {
                            onSuccess.invoke(it)
                        }, onError)
                    } else {
                        // 密码验证失败，保持弹窗打开，显示错误信息
                        _uiState.value = _uiState.value.copy(
                            paymentPasswordLoading = false,
                            paymentPasswordError = response.message ?: "支付密码错误"
                        )
                    }
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "验证支付密码失败"
                    _uiState.value = _uiState.value.copy(
                        paymentPasswordLoading = false,
                        paymentPasswordError = errorMessage
                    )
                }
        }
    }
    
    /**
     * 关闭支付密码弹窗
     */
    fun dismissPaymentPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showPaymentPasswordDialog = false,
            paymentPasswordError = null
        )
    }
    
    /**
     * 关闭设置密码提示弹窗
     */
    fun dismissSetPasswordDialog() {
        _uiState.value = _uiState.value.copy(showSetPasswordDialog = false)
    }
    
    /**
     * 跳转到设置支付密码页面
     */
    fun navigateToSetPassword() {
        _uiState.value = _uiState.value.copy(showSetPasswordDialog = false)
        // 这里应该跳转到设置支付密码的页面
        // 实际实现中可能需要通过回调或导航组件来处理
    }

    /**
     * 创建会议（内部方法，在支付密码验证成功后调用）
     */
    private fun createMeeting(onSuccess: (CreateMeetingResponse?) -> Unit, onError: (String,Int) -> Unit) {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // 使用选中的用户等级的值作为最大参与人数
            val maxParticipants = currentState.selectedUserTier?.value ?: 100
            
            // 将日期时间转换为毫秒级时间戳
            val startTimeTimestamp = try {
                val dateTimeString = "${currentState.selectedDate} ${currentState.selectedTime}"
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
                val date = dateFormat.parse(dateTimeString)
                date?.time ?: 0L // 直接使用毫秒级时间戳
            } catch (e: Exception) {
                Log.e("MeetingCreateViewModel", "Failed to parse date time: ${currentState.selectedDate} ${currentState.selectedTime}", e)
                0L
            }
            
            val request = CreateMeetingRequest(
                groupId = currentState.groupId,
                ownerId = ChatManager.Instance().userId,
                roomName = currentState.roomName,
                startTime = startTimeTimestamp.toString(), // 使用毫秒级时间戳字符串
                meetingTime = currentState.selectedDuration?.value ?: 0,
                meetingMaxUsers = maxParticipants,
                allMic = currentState.allMic,
                allMute = currentState.allMute,
                isNonPayPwd = currentState.isNonPayPwd
            )
            
            Log.d("MeetingCreateViewModel", "Creating meeting with timestamp: $startTimeTimestamp ms (${currentState.selectedDate} ${currentState.selectedTime})")
            
            repository.createMeeting(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(it)
                }
                .onError {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(it.message ?: "创建会议失败",-1)
                }
                .onFailure { code, message, data ->
                    Log.e("MeetingCreateViewModel", "创建会议失败onFailure: code=$code, message=$message, data=$data")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (data!=null){
                        onError(message?:"创建失败",code)
                    }else{

                        onError(message ?: "创建会议失败",-1)
                    }
                }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }


    fun showInsufficientTokenDialog(){
        _uiState.value = _uiState.value.copy(showInsufficientTokenDialog = true)
    }
    /**
     * 关闭代币不足弹窗
     */
    fun dismissInsufficientTokenDialog() {
        _uiState.value = _uiState.value.copy(showInsufficientTokenDialog = false)
    }


    fun showCreateFailedDialog(){
        _uiState.value = _uiState.value.copy(showCreateFailedDialog = true)
    }

    fun showValidationFailedDialog(errorMessage: String?){
        _uiState.value = _uiState.value.copy(showValidationFailedDialog = true, validationFailedMessage = errorMessage)
    }
    
    /**
     * 关闭创建失败弹窗
     */
    fun dismissCreateFailedDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateFailedDialog = false,
            createFailedMessage = null
        )
    }
    
    /**
     * 关闭验证失败弹窗
     */
    fun dismissValidationFailedDialog() {
        _uiState.value = _uiState.value.copy(
            showValidationFailedDialog = false,
            validationFailedMessage = null
        )
    }
}