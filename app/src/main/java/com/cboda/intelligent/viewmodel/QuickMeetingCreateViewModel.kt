package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.CreateGroupData
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.bean.PriceConversion
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.PRICE_ODIC
import com.cboda.baselibrary.utils.eLogD
import com.cboda.chatui.quickmeet.vm.QuickMeetViewBroadModel
import com.cboda.intelligent.model.CreateMeetingRequest
import com.cboda.intelligent.model.CreateMeetingResponse
import com.cboda.intelligent.model.MeetingCreateUiState
import com.cboda.intelligent.model.TimeOption
import com.cboda.intelligent.model.UserTierBroadOption
import com.cboda.intelligent.model.PaymentPasswordVerifyRequest
import com.cboda.intelligent.repository.QuickMeetingRepository
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class QuickMeetingCreateViewModel(
    private val repository: QuickMeetingRepository = QuickMeetingRepository()
) : QuickMeetViewBroadModel() {

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
            repository.getMeetingConfig().onSuccess { config ->
                    config?.let {
                        // 为TimeOption添加基于索引的id（如果服务器没有返回id或id重复）
//                        val processedTimeOptions = config.durationConfigList.mapIndexed { index, timeOption ->
//                            // 如果id为0或者与其他选项重复，则使用index+1作为id
//                            val needsNewId = timeOption.id == 0 ||
//                                    config.durationConfigList.count { it.id == timeOption.id } > 1
//
//                            if (needsNewId) {
//                                timeOption.copy(id = index + 1)
//                            } else {
//                                timeOption
//                            }
//                        }
//
//                        val updatedConfig = config.copy(durationConfigList = processedTimeOptions)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false, config = config, allMic = config.allMembersMicOn, allMute = config.allMembersMuted, isNonPayPwd = config.autoRenewPayWithoutPassword
                        )

//                        eLogD("MeetingCreateViewBroadModel", "Processed TimeOptions: ${processedTimeOptions.map { "${it.durationName}(id=${it.id})" }}")
                    }
                }.onFailure { code, message, data ->
                    eLogD("MeetingCreateViewBroadModel", "加载会议配置失败onFailure: $message, data: $data")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = message ?: "加载配置失败"
                    )
                }.onError { error ->
                    eLogD("MeetingCreateViewBroadModel", "加载会议配置失败onError: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = error.message ?: "加载配置失败"
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
            selectedDate = currentDate, selectedTime = currentTime
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
    fun updateSelectedUserTier(userTier: UserTierBroadOption) {
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
        val duration = currentState.selectedDuration
        val userCount = currentState.selectedUserTier

        if (duration != null && userCount != null) {
            queryMeetingPrice(duration, userCount)
        }
    }

    /**
     * 查询会议价格
     */
    private fun queryMeetingPrice(duration: TimeOption, userCount: UserTierBroadOption) {
        viewModelScope.launch {
            // 设置价格加载状态
            _uiState.value = _uiState.value.copy(isPriceLoading = true)

            repository.queryMeetingPrice(duration.id, userCount.id).onSuccess { response ->
                    response?.let {
                        _uiState.value = _uiState.value.copy(
                            currentPrice = response.priceOdic?.toDouble() ?: 0.0,
                            originalPrice = response.meetingTokens?.toDouble() ?: 0.0,
                            availableTokens = response.currentAmount?.toDouble() ?: 0.0,
                            isPriceLoading = false
                        )
                    }
                }.onFailure { code, msg, data ->
                    eLogD("MeetingCreateViewBroadModel", "价格查询失败onFailure: code=$code, msg=$msg, data=$data")
                    // 价格查询失败，保持原有价格，只取消加载状态
                    _uiState.value = _uiState.value.copy(isPriceLoading = false)
                    // 可以选择是否显示错误提示，这里暂时不显示
                }.onError {
                    _uiState.value = _uiState.value.copy(isPriceLoading = false)
                    // 可以选择是否显示错误提示，这里暂时不显示
                }
        }
    }

    /**
     * 检查支付密码状态并开始创建会议流程
     */
    fun startCreateMeetingFlow(onError: (String) -> Unit, onSuccessInvoke: (Boolean) -> Unit = {}) {
        // 先进行详细的表单验证
        val validationResult = validateMeetingForm()
        if (!validationResult.isValid) {
            // 显示iOS风格的Toast提示
            onError(validationResult.errorMessage ?: "请完善会议信息")
            return
        }

        // 验证通过，继续检查支付密码状态
        checkPaymentPasswordStatus(onError, onSuccessInvoke)
    }


    fun checkOrder(orderId: String?, haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<PriceConversion?> {
        val liveData = SingleLiveBroadData<PriceConversion?>()
        val params = hashMapOf<String, Any?>("orderId" to orderId)
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val priceConversion = PriceConversion()
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.SELECT_ORDER, PriceConversion::class.java, params
                    )
                    result.onSuccess { data ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        liveData.postValue(data)
                    }.onFailure { _, message ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        priceConversion.priceOdic = message
                        liveData.postValue(priceConversion)
//                        showToastMain(message)
                    }.onError { error ->
                        priceConversion.priceOdic = error.message ?: ""
                        if (closeLoading) {
                            dismissLoading()
                        }
//                        showToastMain("${error.message}")
                        liveData.postValue(priceConversion)
                    }
                } catch (_: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                }
            }
        }
        return liveData
    }

    fun getActivatePrice(haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<String?> {
        val liveData = SingleLiveBroadData<String?>()
        val params = hashMapOf<String, Any?>("amount" to "1")
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.GET_ACTIVATE_PRICE, PriceConversion::class.java, params
                    )
                    result.onSuccess { data ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        val bigDecimal = BigDecimal(data?.priceOdic)
                        val bigDecimalStr = bigDecimal.toString()
                        eLogD("getActivatePriceData_bigDecimal", bigDecimal)
                        eLogD("getActivatePriceData_bigDecimalStr", bigDecimalStr)
                        MMKVBroadUtils.saveSqlStrData(PRICE_ODIC, bigDecimalStr)
                        liveData.postValue(bigDecimalStr)
                    }.onFailure { _, _ ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        liveData.postValue(null)
                    }.onError { _ ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    fun getCreateMeetingValidationError(): String? {
        return validateMeetingForm().errorMessage
    }

    fun buildCreateGroupData(getForceNewPay: Boolean = false): CreateGroupData {
        val currentState = _uiState.value
        return CreateGroupData().apply {
            groupIdNo = currentState.groupId
            durationConfigId = (currentState.selectedDuration?.id ?: 0).toString()
            userTierConfigId = (currentState.selectedUserTier?.id ?: 0).toString()
            roomName = currentState.roomName
            if (getForceNewPay) {
                forceNewPay = true
            }
        }
    }

    fun createPayOrder(createGroupData: CreateGroupData, haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<PayLsOrderBean> {
        val liveData = SingleLiveBroadData<PayLsOrderBean>()
        val params = hashMapOf<String, Any?>("scene" to "meeting_create", "bizParams" to createGroupData)
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.CREATE_LS_PAY_ORDER, PayLsOrderBean::class.java, params
                    )
                    result.onSuccess { data ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        data?.let {
                            liveData.postValue(data)
                        } ?: showToastMain("数据为空")
                    }.onFailure { _, message ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        showToastMain(message)
                    }.onError { error ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        showToastMain("${error.message}")
                    }
                } catch (_: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                }
            }
        }
        return liveData
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
        val isValid: Boolean, val errorMessage: String?
    )

    /**
     * 检查支付密码状态
     */
    private fun checkPaymentPasswordStatus(onError: (String) -> Unit, onSuccessInvoke: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.checkPaymentPasswordStatus().onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, hasPaymentPassword = response.hasPassword
                    )
                    if (response.hasPassword) {
                        // 已设置支付密码，显示密码输入弹窗
                        _uiState.value = _uiState.value.copy(showPaymentPasswordDialog = true)
                    } else {
                        // 未设置支付密码，显示设置密码提示弹窗
                        _uiState.value = _uiState.value.copy(showSetPasswordDialog = true)
                    }
                    onSuccessInvoke.invoke(response.hasPassword)
                }.onFailure { error ->
                    val errorMessage = error.message ?: "检查支付密码状态失败"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = errorMessage
                    )
                    onError(errorMessage)
                }
        }
    }

    /**
     * 验证支付密码
     */
    fun verifyPaymentPassword(password: String, onSuccess: (CreateMeetingResponse?) -> Unit, onError: (String, Int) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                paymentPasswordLoading = true, paymentPasswordError = null
            )

            val request = PaymentPasswordVerifyRequest(password)
            repository.verifyPaymentPassword(request).onSuccess { response ->
                    if (response.success) {
                        // 密码验证成功，关闭弹窗并创建会议
                        _uiState.value = _uiState.value.copy(
                            paymentPasswordLoading = false, showPaymentPasswordDialog = false
                        )
                        createMeeting(onSuccess = {
                            onSuccess.invoke(it)
                        }, onError)
                    } else {
                        // 密码验证失败，保持弹窗打开，显示错误信息
                        _uiState.value = _uiState.value.copy(
                            paymentPasswordLoading = false, paymentPasswordError = response.message ?: "支付密码错误"
                        )
                    }
                }.onFailure { error ->
                    val errorMessage = error.message ?: "验证支付密码失败"
                    _uiState.value = _uiState.value.copy(
                        paymentPasswordLoading = false, paymentPasswordError = errorMessage
                    )
                }
        }
    }

    /**
     * 关闭支付密码弹窗
     */
    fun dismissPaymentPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showPaymentPasswordDialog = false, paymentPasswordError = null
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
    private fun createMeeting(onSuccess: (CreateMeetingResponse?) -> Unit, onError: (String, Int) -> Unit) {
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // 使用选中的用户等级的值作为最大参与人数
            val maxParticipants = currentState.selectedUserTier?.tierValue ?: 100

            // 将日期时间转换为毫秒级时间戳
            val startTimeTimestamp = try {
                val dateTimeString = "${currentState.selectedDate} ${currentState.selectedTime}"
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
                val date = dateFormat.parse(dateTimeString)
                date?.time ?: 0L // 直接使用毫秒级时间戳
            } catch (e: Exception) {
                eLogD("MeetingCreateViewBroadModel", "Failed to parse date time: ${currentState.selectedDate} ${currentState.selectedTime}", e)
                0L
            }

            val request = CreateMeetingRequest(
                groupId = currentState.groupId, ownerId = ChatManager.Instance().userId, roomName = currentState.roomName, startTime = startTimeTimestamp.toString(), // 使用毫秒级时间戳字符串
                meetingTime = currentState.selectedDuration?.durationValue ?: 0, meetingMaxUsers = maxParticipants, allMic = currentState.allMic, allMute = currentState.allMute, isNonPayPwd = currentState.isNonPayPwd
            )

            eLogD("MeetingCreateViewBroadModel", "Creating meeting with timestamp: $startTimeTimestamp ms (${currentState.selectedDate} ${currentState.selectedTime})")

            repository.createMeeting(request).onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(it)
                }.onError {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(it.message ?: "创建会议失败", -1)
                }.onFailure { code, message, data ->
                    eLogD("MeetingCreateViewBroadModel", "创建会议失败onFailure: code=$code, message=$message, data=$data")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (data != null) {
                        onError(message ?: "创建失败", code)
                    } else {

                        onError(message ?: "创建会议失败", -1)
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


    fun showInsufficientTokenDialog() {
        _uiState.value = _uiState.value.copy(showInsufficientTokenDialog = true)
    }

    /**
     * 关闭代币不足弹窗
     */
    fun dismissInsufficientTokenDialog() {
        _uiState.value = _uiState.value.copy(showInsufficientTokenDialog = false)
    }


    fun showCreateFailedDialog() {
        _uiState.value = _uiState.value.copy(showCreateFailedDialog = true)
    }

    fun showValidationFailedDialog(errorMessage: String?) {
        _uiState.value = _uiState.value.copy(showValidationFailedDialog = true, validationFailedMessage = errorMessage)
    }

    /**
     * 关闭创建失败弹窗
     */
    fun dismissCreateFailedDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateFailedDialog = false, createFailedMessage = null
        )
    }

    /**
     * 关闭验证失败弹窗
     */
    fun dismissValidationFailedDialog() {
        _uiState.value = _uiState.value.copy(
            showValidationFailedDialog = false, validationFailedMessage = null
        )
    }
}