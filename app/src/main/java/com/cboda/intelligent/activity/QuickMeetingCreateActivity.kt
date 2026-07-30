package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.baselibrary.utils.openH5InImToken
import com.cboda.baselibrary.utils.openH5InMetaMask
import com.cboda.baselibrary.utils.openH5InTokenPocket
import com.cboda.baselibrary.utils.showCommonStrTip
import com.cboda.baselibrary.utils.toJson2
import com.cboda.chatui.quickmeet.CallEventCallback
import com.cboda.chatui.quickmeet.MeetCallManager
import com.cboda.chatui.quickmeet.bean.CallDirect
import com.cboda.chatui.quickmeet.bean.GroupMemberRole
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.goPermissionAudioMeet
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.compose.*
import com.cboda.intelligent.bean.PaymentMethodBroadBean
import com.cboda.intelligent.bean.PaymentMethodDefaults.IM_TOKEN
import com.cboda.intelligent.bean.PaymentMethodDefaults.META_MASK
import com.cboda.intelligent.bean.PaymentMethodDefaults.TP
import com.cboda.intelligent.dialog.pop.PasswordPricedPop
import com.cboda.intelligent.dialog.pop.PaymentMethodPopup
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.QuickMeetingCreateViewModel
import com.freddy.im.group.MeetGroupMuteHelper
import kotlinx.coroutines.launch
import java.math.BigDecimal

class QuickMeetingCreateActivity : BaseSimpleBroadActivity(), CallEventCallback {

    private val viewModel: QuickMeetingCreateViewModel by viewModels()
    private var payLsOrderBean: PayLsOrderBean? = null

    override fun getLayoutId(): Int = com.cboda.intelligent.R.layout.activity_quick_meet_create

    override fun showOrHideTitleLayout(): Boolean = false

    override fun transparentStatus(): Boolean = true

    override fun useStatusBarDarkFont(): Boolean = false
    override fun isEnableKeyboard(): Boolean {
        return true
    }

    private val groupName by lazy {
        intent.getStringExtra("group_name") ?: ""
    }
    private val groupId by lazy {
        intent.getStringExtra("group_id") ?: ""
    }
    private val groupMemberRole by lazy {
        intent.getIntExtra(EXTRA_GROUP_MEMBER_ROLE, GroupMemberRole.UNKNOWN)
    }

    companion object {
        private const val EXTRA_GROUP_MEMBER_ROLE = "group_member_role"

        @JvmOverloads
        fun launch(context: Context?, groupId: String, groupName: String, groupMemberRole: Int = GroupMemberRole.UNKNOWN) {
            context?.let {
                val intent = Intent(it, QuickMeetingCreateActivity::class.java)
                intent.putExtra("group_name", groupName)
                intent.putExtra("group_id", groupId)
                intent.putExtra(EXTRA_GROUP_MEMBER_ROLE, groupMemberRole)
                it.startActivity(intent)
            }
        }
    }

    private fun joinCreatedMeet(meetBean: QuickDetailMeetBean) {
        // 会议开启：默认全员禁言，并记录开会前禁言态以便结束后恢复
        // 快照必须以 IM 群会话 ID 为主 key；channelId 作别名，避免自动结束 ID 不一致漏恢复
        val muteGroupId = groupId.takeIf { it.isNotEmpty() }
            ?: meetBean.groupIdNo?.takeIf { it.isNotEmpty() }
            ?: meetBean.channelId?.takeIf { it.isNotEmpty() }
            ?: ""
        MeetGroupMuteHelper.onMeetStarted(
            muteGroupId,
            meetBean.groupIdNo,
            meetBean.channelId,
        )
        MeetCallManager.getInstance().callMeetAction(
            CallDirect.CALLER,
            meetBean.channelId ?: groupId,
            meetBean.token ?: "",
            meetBean,
            groupMemberRole,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MeetCallManager.getInstance().registerEngineEvent(this)
        // 获取传入的群聊名称
        val roomName = if (groupName.isNotEmpty()) {
            "$groupName-${getString(com.cboda.intelligent.R.string.meeting_room_suffix)}"
        } else {
            ""
        }

        if (roomName.isNotEmpty()) {
            viewModel.updateRoomName(roomName)
        }
        if (groupId.isNotEmpty()) {
            viewModel.updateGroupId(groupId)
        }

        setContent {
            QuickMeetingCreateScreen(
                viewModel = viewModel,
                groupName,
                onBack = { finish() },
                onCreateClick = {
                    if (HttpsBroadConfig.openTPay) {
                        startCreatePayOrderFlow()
                    }
                },
                onPayAuthSuccess = {
                    if (!HttpsBroadConfig.openTPay) {
                        lifecycleScope.launch {
                            val meetingCreateUiState = viewModel.uiState.value
                            viewModel.goCreateQuickMeet(groupId, meetingCreateUiState.roomName, meetingCreateUiState.selectedDuration?.id ?: 0, meetingCreateUiState.selectedUserTier?.id ?: 0, "")
                                .observe(this@QuickMeetingCreateActivity) {
                                    joinCreatedMeet(it)
                                }
                        }
                    }
                }
            )
        }
    }

    override fun onEngineSetup() {
        showLoading()
    }

    override fun onRtcJoinChannelSuccess() {
        dismissLoading()
        finish()
    }

    override fun onDestroy() {
        MeetCallManager.getInstance().unregisterEngineEvent(this)
        super.onDestroy()
    }

    private var canPay = false
    private fun startCreatePayOrderFlow() {
        goPermissionAudioMeet {
            val validationError = viewModel.getCreateMeetingValidationError()
            if (!validationError.isNullOrBlank()) {
                ToastBroadUtils.show(validationError)
                return@goPermissionAudioMeet
            }
            viewModel.checkPayMeet(viewModel.buildCreateGroupData(false).groupIdNo).observe(this@QuickMeetingCreateActivity) { data ->
                val paidUnused = data.paidUnused
                paidUnused?.let { checkMeetBean ->
                    if (checkMeetBean.remainSeconds > 0 && checkMeetBean.has) {
                        showCommonStrTip("是否使用这笔订单创建会议？", "检测到你在本群有一笔已支付未使用的会议订单，该订单将保留", "新建会议", "使用该订单", getResumeActivity()) { toCheck ->
                            if (toCheck) {
                                lifecycleScope.launch {
                                    viewModel.goCreateQuickMeet(checkMeetBean.groupIdNo, checkMeetBean.channelName, checkMeetBean.durationConfigId, checkMeetBean.userTierConfigId, checkMeetBean.orderId)
                                        .observe(this@QuickMeetingCreateActivity) {
                                            joinCreatedMeet(it)
                                        }
                                }
                            } else {
                                createOrder(true)
                            }
                        }
                    } else {
                        createOrder()
                    }
                } ?: createOrder()
            }

        }
    }

    private fun createOrder(forceNewPay: Boolean = false) {
        viewModel.createPayOrder(viewModel.buildCreateGroupData(forceNewPay), true, true)
            .observe(this@QuickMeetingCreateActivity) { order ->
                payLsOrderBean = order
                showSelectPayMethod()
            }
    }

    private fun showSelectPayMethod() {
        val payPopup = PaymentMethodPopup(this)
        payPopup.setOnSelected { paymentMethod ->
            canPay = true
            openWalletPay(payLsOrderBean, paymentMethod)
            payPopup.dismiss()
        }
        payPopup.showPopupWindow()
    }

    override fun onResume() {
        super.onResume()
        checkOrder()
    }

    private fun checkOrder() {
        if (canPay && payLsOrderBean != null && !TextUtils.isEmpty(payLsOrderBean?.orderId)) {
            viewModel.checkOrder(payLsOrderBean?.orderId, true, true).observe(this) { data ->
                if (data?.success == true) {
                    lifecycleScope.launch {
                        val meetingCreateUiState = viewModel.uiState.value
                        viewModel.goCreateQuickMeet(groupId, meetingCreateUiState.roomName, meetingCreateUiState.selectedDuration?.id ?: 0, meetingCreateUiState.selectedUserTier?.id ?: 0, payLsOrderBean?.orderId)
                            .observe(this@QuickMeetingCreateActivity) {
                                canPay = false
                                payLsOrderBean = null
                                joinCreatedMeet(it)
                            }
                    }
                } else {
                    showCommonStrTip("订单支付", "该订单是否支付？", "取消支付", "已支付", this, isOutSideTouchable = false) { toCheck ->
                        if (toCheck) {
                            viewModel.checkOrder(payLsOrderBean?.orderId, true, true).observe(this) { data ->
                                if (data?.success == true) {
                                    lifecycleScope.launch {
                                        val meetingCreateUiState = viewModel.uiState.value
                                        viewModel.goCreateQuickMeet(
                                            groupId,
                                            meetingCreateUiState.roomName,
                                            meetingCreateUiState.selectedDuration?.id ?: 0,
                                            meetingCreateUiState.selectedUserTier?.id ?: 0,
                                            payLsOrderBean?.orderId
                                        )
                                            .observe(this@QuickMeetingCreateActivity) {
                                                canPay = false
                                                payLsOrderBean = null
                                                joinCreatedMeet(it)
                                            }
                                    }
                                } else {
                                    showCommonStrTip("订单支付", "查询到该订单未成功支付，是否继续？", "取消支付", "重新支付", this, isOutSideTouchable = false) { toCheck2 ->
                                        if (toCheck2) {
                                            showSelectPayMethod()
                                        } else {
                                            payLsOrderBean = null
                                            canPay = false
                                        }
                                    }
                                }
                            }
                        } else {
                            payLsOrderBean = null
                            canPay = false
                        }
                    }
                }
            }

        }
    }

    private fun openWalletPay(data: PayLsOrderBean?, paymentMethod: PaymentMethodBroadBean) {
        if (data == null) {
            ToastBroadUtils.show("订单数据为空")
            return
        }
        val chainId = anyToInt(data.chainId)
        eLogD("openWalletPay_data2", toJson2(data))
        val priceOdic = BigDecimal(data.priceOdic)
        val h5Url = "https://pay.web4x.io/pay?amountDisplay=$priceOdic" +
                "&chainId=$chainId" +
                "&orderId=${data.orderId}" +
                "&tokenSymbol=DUALX&paymentType=${data.paymentType}"
        eLogD("openWalletPay_h5Url2", h5Url)
        when (paymentMethod.id) {
            IM_TOKEN -> openH5InImToken(h5Url, this)
            TP -> openH5InTokenPocket(h5Url, this, chainId)
            META_MASK -> openH5InMetaMask(h5Url, this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMeetingCreateScreen(
    viewModel: QuickMeetingCreateViewModel,
    groupName: String,
    onBack: () -> Unit,
    onCreateClick: () -> Unit,
    onPayAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiState by viewModel.uiState.collectAsState()

    // 获取状态栏高度
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }

    // 显示选择对话框的状态
    var showUserTierDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = com.cboda.intelligent.R.color.bg_main2))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题栏
            TitleBar(
                statusBarHeight = statusBarHeight,
                onBack = onBack
            )

            // 内容区域
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(bottom = 62.dp) // 为底部按钮留出空间
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 会议室名称输入框
                item {
                    RoomNameInputField(
                        roomName = uiState.roomName,
                        onRoomNameChange = viewModel::updateRoomName,
                        groupChatName = groupName
                    )
                }

//                // 开始时间
//                item {
//                    StartTimeSection(
//                        selectedDate = uiState.selectedDate,
//                        selectedTime = uiState.selectedTime,
//                        onDateTimeClick = {
//                            showDatePicker = true
//                        }
//                    )
//                }

                // 会议时长
                item {
                    MeetingDurationSection(
                        selectedDuration = uiState.selectedDuration,
                        onDurationSelected = { duration ->
                            viewModel.updateSelectedDuration(duration)
                        },
                        timeOptions = uiState.config?.durationConfigList ?: emptyList(),
                        renewalRules = uiState.config?.billingRule?.roundingRule ?: ""
                    )
                }

                // 续时免密支付开关
                item {
                    SwitchItem(
                        title = stringResource(id = com.cboda.intelligent.R.string.meeting_non_password_renewal),
                        isChecked = uiState.isNonPayPwd,
                        onCheckedChange = { viewModel.toggleNonPayPwd() }
                    )
                }

                // 会议人数
                item {
                    OptionItem(
                        title = stringResource(id = com.cboda.intelligent.R.string.meeting_participants),
                        selectedText = uiState.selectedUserTier?.tierName
                            ?: stringResource(id = com.cboda.intelligent.R.string.meeting_please_select),
                        onClick = {
                            showUserTierDialog = true
                        }
                    )
                }

                // 全员开麦和全员禁言
//                item {
//                    DoubleSwitchItem(
//                        firstTitle = stringResource(id = com.cboda.intelligent.R.string.meeting_all_mic_on),
//                        firstIsChecked = uiState.allMic,
//                        onFirstCheckedChange = { viewModel.toggleAllMic() },
//                        secondTitle = stringResource(id = com.cboda.intelligent.R.string.meeting_all_mute),
//                        secondIsChecked = uiState.allMute,
//                        onSecondCheckedChange = { viewModel.toggleAllMute() }
//                    )
//                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // 底部价格和创建按钮 - 固定在屏幕底部
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomSection(
                totalPrice = uiState.calculateTotalPrice(),
                originalPrice = uiState.calculateOriginalPrice(), // 如果有原价的话
                canCreate = uiState.canCreateMeeting(),
                isLoading = uiState.isLoading,
                isPriceLoading = uiState.isPriceLoading,
                showPriceLoadingIndicator = true, // 可以设置为false来隐藏价格加载指示器
                onCreateClick = if (HttpsBroadConfig.openTPay) onCreateClick else {
                    {
                        goPermissionAudioMeet() {
                            viewModel.startCreateMeetingFlow(
//                        onSuccess = onCreateSuccess,
                                onError = { error ->
                                    ToastBroadUtils.show(error)
                                },
                                onSuccessInvoke = { hasPassword ->
                                    if (hasPassword) {
                                        eLogD(APP_TAG, "展示支付密码弹窗----》")
                                        PasswordPricedPop(getAppContext(), uiState.calculateTotalPrice().toString(), {
                                            onPayAuthSuccess.invoke()
                                        }).showPopupWindow()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    // 日期选择器
    if (showDatePicker) {
        LaunchedEffect(showDatePicker) {
            showSystemDatePicker(context) { date ->
                viewModel.updateSelectedDate(date)
                // 选择日期后自动显示时间选择器
                showTimePicker(context) { time ->
                    viewModel.updateSelectedTime(time)
                }
            }
            showDatePicker = false
        }
    }

    // 用户等级选择对话框
    if (showUserTierDialog) {
        UserTierSelectionDialog(
            userTierOptions = uiState.config?.userTierConfigList ?: emptyList(),
            currentSelection = uiState.selectedUserTier,
            onDismiss = { showUserTierDialog = false },
            onOptionSelected = { userTier ->
                viewModel.updateSelectedUserTier(userTier)
                showUserTierDialog = false
            }
        )
    }

    // 支付密码弹窗
//    if (uiState.showPaymentPasswordDialog) {
//        PaymentPasswordDialog(
//            isLoading = uiState.paymentPasswordLoading,
//            errorMessage = uiState.paymentPasswordError,
//            onPasswordEntered = { password ->
//                viewModel.verifyPaymentPassword(
//                    password = password,
//                    onSuccess = onCreateSuccess,
//                    onError = { error, createout ->
//                        when (createout) {
//                            1 -> {
//                                viewModel.showInsufficientTokenDialog()
//
//                            }
//
//                            2 -> {
//                                viewModel.showCreateFailedDialog()
//                            }
//
//                            else -> {
//                                viewModel.showValidationFailedDialog(error)
//                            }
//                        }
////                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
//                    }
//                )
//            },
//            onDismiss = { viewModel.dismissPaymentPasswordDialog() }
//        )
//    }

    // 设置支付密码弹窗
    if (uiState.showSetPasswordDialog) {
        SetPaymentPasswordDialog(
            onDismiss = { viewModel.dismissSetPasswordDialog() },
            onSetPassword = {
                viewModel.dismissSetPasswordDialog()
                val intent = Intent(context, SetPayPasswordBroadActivity::class.java)
                intent.putExtra("editIndex", "1")
                context.startActivity(intent)
            }
        )
    }

    // 代币不足弹窗
    if (uiState.showInsufficientTokenDialog) {
        InsufficientTokenDialog(
            onDismiss = {
                viewModel.dismissInsufficientTokenDialog()
                context.startActivity(Intent(context, QianbaoBroadActivity::class.java))
            }
        )
    }

    // 创建失败弹窗
    if (uiState.showCreateFailedDialog) {
        CreateFailedDialog(
            message = uiState.createFailedMessage,
            onDismiss = { viewModel.dismissCreateFailedDialog() }
        )
    }

    if (uiState.showValidationFailedDialog) {
        ValidationFailedDialog(
            message = uiState.validationFailedMessage,
            onDismiss = { viewModel.dismissValidationFailedDialog() }
        )
    }


    // 错误提示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
}




