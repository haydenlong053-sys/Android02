package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import com.cboda.chatui.R
import com.cboda.chatui.bridge.YeHuoBroadEvent
import cn.wildfirechat.message.MeetingInviteMessageContent
import com.google.gson.Gson
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.compose.*
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.model.CreateMeetingResponse
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.MeetingCreateViewBroadModel
import org.greenrobot.eventbus.EventBus

class MeetingCreateBroadActivity : BaseSimpleBroadActivity() {

    private val viewModel: MeetingCreateViewBroadModel by viewModels()

    override fun getLayoutId(): Int = com.cboda.intelligent.R.layout.activity_base

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

    companion object {

        fun launch(context: Context?, groupId: String, userId: String, groupName: String) {
            context?.let {
                val intent = Intent(it, MeetingCreateBroadActivity::class.java)
                intent.putExtra("group_name", groupName)
                intent.putExtra("group_id", groupId)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取传入的群聊名称
//        val groupName = intent.getStringExtra("group_name") ?: ""
        val roomName = if (groupName.isNotEmpty()) "$groupName 会议室" else ""

        if (roomName.isNotEmpty()) {
            viewModel.updateRoomName(roomName)
        }
        if (groupId.isNotEmpty()) {
            viewModel.updateGroupId(groupId)
        }

        setContent {
            MeetingCreateScreen(
                viewModel = viewModel,
                groupName,
                onBack = { finish() },
                onCreateSuccess = {
                    it?.let {
                        when (it.createout ?: -1) {
                            200 -> {
                                ToastBroadUtil.show("会议创建成功")
                                val meetingMessage =
                                    MeetingInviteMessageContent()
                                        .apply {
                                            meetingId = it.meeting?.roomId
                                            idno = it.meeting?.idNo
                                            title = it.meeting?.roomName
                                            desc = "我在会议室等你，快进来吧"
                                            isAllMic = it.meeting?.allMic ?: true
                                            isAllMute = it.meeting?.allMute ?: false

                                        }
                                EventBus.getDefault().post(
                                    YeHuoBroadEvent(
                                        "huiyishi",
                                        Gson().toJson(meetingMessage),
                                        ""
                                    )
                                )
                                finish()
                                JumpBroadUtils.startQunMeetingActivity(
                                    this,
                                    it.meeting?.roomId ?: "",
                                    it.meeting?.idNo ?: "",
                                    it.meeting?.roomName ?: "",
                                    true,
                                    it.meeting?.allMute ?: false,
                                    it.meeting?.allMic ?: true
                                )

                            }


                        }
                    }

                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingCreateScreen(
    viewModel: MeetingCreateViewBroadModel,
    groupName: String,
    onBack: () -> Unit,
    onCreateSuccess: (CreateMeetingResponse?) -> Unit
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
            .background(colorResource(id = R.color.bg_main_space))
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
                        timeOptions = uiState.config?.timeOline ?: emptyList(),
                        renewalRules = uiState.config?.renewalRules ?: ""
                    )
                }

                // 续时免密支付开关
                item {
                    SwitchItem(
                        title = "续时免密支付",
                        isChecked = uiState.isNonPayPwd,
                        onCheckedChange = { viewModel.toggleNonPayPwd() }
                    )
                }

                // 会议人数
                item {
                    OptionItem(
                        title = "会议人数",
                        selectedText = uiState.selectedUserTier?.name ?: "请选择",
                        onClick = {
                            showUserTierDialog = true
                        }
                    )
                }

                // 全员开麦和全员禁言
                item {
                    DoubleSwitchItem(
                        firstTitle = "全员开麦",
                        firstIsChecked = uiState.allMic,
                        onFirstCheckedChange = { viewModel.toggleAllMic() },
                        secondTitle = "全员禁言",
                        secondIsChecked = uiState.allMute,
                        onSecondCheckedChange = { viewModel.toggleAllMute() }
                    )
                }

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
                onCreateClick = {
                    viewModel.startCreateMeetingFlow(
//                        onSuccess = onCreateSuccess,
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
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
            userTierOptions = uiState.config?.userTierOptions ?: emptyList(),
            currentSelection = uiState.selectedUserTier,
            onDismiss = { showUserTierDialog = false },
            onOptionSelected = { userTier ->
                viewModel.updateSelectedUserTier(userTier)
                showUserTierDialog = false
            }
        )
    }

    // 支付密码弹窗
    if (uiState.showPaymentPasswordDialog) {
        PaymentPasswordDialog(
            isLoading = uiState.paymentPasswordLoading,
            errorMessage = uiState.paymentPasswordError,
            onPasswordEntered = { password ->
                viewModel.verifyPaymentPassword(
                    password = password,
                    onSuccess = onCreateSuccess,
                    onError = { error, createout ->
                        when (createout) {
                            1 -> {
                                viewModel.showInsufficientTokenDialog()

                            }

                            2 -> {
                                viewModel.showCreateFailedDialog()
                            }

                            else -> {
                                viewModel.showValidationFailedDialog(error)
                            }
                        }
//                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = { viewModel.dismissPaymentPasswordDialog() }
        )
    }

    // 设置支付密码弹窗
    if (uiState.showSetPasswordDialog) {
        SetPaymentPasswordDialog(
            onDismiss = { viewModel.dismissSetPasswordDialog() },
            onSetPassword = {
                viewModel.dismissSetPasswordDialog()
                val intent = Intent(context, SetPasswordBroadActivity::class.java)
                intent.putExtra("editIndex", 1)
                context.startActivity(intent)
            }
//            onSetPassword = { viewModel.navigateToSetPassword() }
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




