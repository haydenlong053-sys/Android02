package com.lxkj.aiim.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// Removed Icons imports - using string characters instead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PaymentPasswordMode {
    PAYMENT,        // 支付验证
    SET_PASSWORD,   // 设置密码
    MODIFY_PASSWORD // 修改密码
}

data class PaymentPasswordState(
    val mode: PaymentPasswordMode = PaymentPasswordMode.PAYMENT,
    val title: String = "",
    val amount: String = "",
    val currency: String = "￥",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentStep: Int = 1, // 用于设置密码时的步骤（1：输入新密码，2：确认密码）
    val firstPassword: String = "", // 第一次输入的密码
    val showOldPasswordInput: Boolean = false // 修改密码时是否显示旧密码输入
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedPaymentPasswordDialog(
    state: PaymentPasswordState,
    onPasswordEntered: (password: String, oldPassword: String?) -> Unit,
    onDismiss: () -> Unit,
    onForgotPassword: (() -> Unit)? = null
) {
    var currentPassword by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val shakeAnimation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            showError = true
            scope.launch {
                repeat(3) {
                    shakeAnimation.animateTo(10f, animationSpec = tween(50))
                    shakeAnimation.animateTo(-10f, animationSpec = tween(50))
                }
                shakeAnimation.animateTo(0f, animationSpec = tween(50))
                delay(3000)
                showError = false
            }
        }
    }

    LaunchedEffect(state.currentStep) {
        currentPassword = ""
    }

    fun handlePasswordComplete(password: String) {
        when (state.mode) {
            PaymentPasswordMode.PAYMENT -> {
                onPasswordEntered(password, null)
            }
            PaymentPasswordMode.SET_PASSWORD -> {
                if (state.currentStep == 1) {
                    onPasswordEntered(password, null)
                } else {
                    onPasswordEntered(password, state.firstPassword)
                }
            }
            PaymentPasswordMode.MODIFY_PASSWORD -> {
                if (state.showOldPasswordInput) {
                    onPasswordEntered(oldPassword, null)
                } else {
                    onPasswordEntered(password, oldPassword)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false // 让弹窗延伸到状态栏
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .clickable(enabled = false) { } // 阻止点击事件传播
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (state.currentStep > 1 || state.showOldPasswordInput) {
                                when (state.mode) {
                                    PaymentPasswordMode.SET_PASSWORD -> {
                                        if (state.currentStep > 1) {
                                            onPasswordEntered("BACK_STEP", null)
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                    PaymentPasswordMode.MODIFY_PASSWORD -> {
                                        if (!state.showOldPasswordInput) {
                                            onPasswordEntered("BACK_STEP", null)
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                    else -> onDismiss()
                                }
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = if ((state.currentStep > 1 && state.mode == PaymentPasswordMode.SET_PASSWORD) || 
                                    (!state.showOldPasswordInput && state.mode == PaymentPasswordMode.MODIFY_PASSWORD)) 
                                "←" else "×",
                            fontSize = 20.sp,
                            color = Color(0xFF007AFF)
                        )
                    }

                    Text(
                        text = getDialogTitle(state),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF000000)
                    )

                    Spacer(modifier = Modifier.size(24.dp))
                }

                if (state.mode == PaymentPasswordMode.PAYMENT && state.amount.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "支付金额",
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.currency}${state.amount}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                    }
                }

                // 密码输入区域（包含输入框和错误提示）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 16.dp)
                        .graphicsLayer(translationX = shakeAnimation.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PasswordInputField(
                        password = if (state.showOldPasswordInput) oldPassword else currentPassword
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 预留错误提示位置，避免弹窗高度变化
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp), // 固定高度预留位置
                        contentAlignment = Alignment.Center
                    ) {
                        if (showError && state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = Color(0xFFE53E3E),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                NumberKeyboard(
                    onNumberClick = { number: String ->
                        val targetPassword = if (state.showOldPasswordInput) oldPassword else currentPassword
                        if (targetPassword.length < 6) {
                            val newPassword = targetPassword + number
                            if (state.showOldPasswordInput) {
                                oldPassword = newPassword
                            } else {
                                currentPassword = newPassword
                            }
                            
                            if (newPassword.length == 6) {
                                handlePasswordComplete(newPassword)
                            }
                        }
                    },
                    onDeleteClick = {
                        if (state.showOldPasswordInput) {
                            if (oldPassword.isNotEmpty()) {
                                oldPassword = oldPassword.dropLast(1)
                            }
                        } else {
                            if (currentPassword.isNotEmpty()) {
                                currentPassword = currentPassword.dropLast(1)
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )

                if ((state.mode == PaymentPasswordMode.PAYMENT || state.mode == PaymentPasswordMode.MODIFY_PASSWORD) 
                    && onForgotPassword != null) {
                    Text(
                        text = "忘记支付密码？",
                        fontSize = 16.sp,
                        color = Color(0xFF007AFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onForgotPassword() }
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PasswordInputField(
    password: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp), // 左右边距36dp
        horizontalArrangement = Arrangement.SpaceBetween // 使用SpaceBetween确保6个格子都显示
    ) {
        repeat(6) { index ->
            Box(
                modifier = Modifier
                    .size(40.dp) // 减小格子尺寸以适应布局
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F9FA))
                    .border(
                        width = 1.dp,
                        color = if (index < password.length) Color(0xFF007AFF) else Color(0xFFE5E5EA),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (index < password.length) {
                    Text(
                        text = "*",
                        color = Color(0xFF000000),
                        fontSize = 20.sp, // 相应调整字体大小
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KeyboardButton(
    text: String,
    onClick: () -> Unit,
    isDelete: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                if (isDelete) Color(0xFFF2F2F7) else Color(0xFFFFFFFF)
            )
            .clickable { onClick() }
            .border(
                width = if (isDelete) 0.dp else 0.5.dp,
                color = Color(0xFFE5E5EA),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isDelete) 18.sp else 28.sp,
            fontWeight = FontWeight.Normal,
            color = if (isDelete) Color(0xFF8E8E93) else Color(0xFF000000)
        )
    }
}

@Composable
fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 数字键盘布局 1-9
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 1..3) {
                    val number = (row * 3 + col).toString()
                    KeyboardButton(
                        text = number,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 最后一行：空白、0、删除
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 空白按钮
            Spacer(modifier = Modifier.size(80.dp))
            
            // 0按钮
            KeyboardButton(
                text = "0",
                onClick = { onNumberClick("0") }
            )
            
            // 删除按钮
            KeyboardButton(
                text = "⌫",
                onClick = onDeleteClick,
                isDelete = true
            )
        }
    }
}

fun getDialogTitle(state: PaymentPasswordState): String {
    return when (state.mode) {
        PaymentPasswordMode.PAYMENT -> "请输入支付密码"
        PaymentPasswordMode.SET_PASSWORD -> {
            if (state.currentStep == 1) "设置支付密码" else "确认支付密码"
        }
        PaymentPasswordMode.MODIFY_PASSWORD -> {
            if (state.showOldPasswordInput) "请输入原支付密码" else "请输入新支付密码"
        }
    }
}

class PaymentPasswordManager {
    
    suspend fun verifyPaymentPassword(password: String): Result<Boolean> {
        return try {
            // 模拟网络请求
            kotlinx.coroutines.delay(1000)
            // 这里应该调用实际的API验证密码
            if (password == "123456") {
                Result.success(true)
            } else {
                Result.failure(Exception("密码错误"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setPaymentPassword(password: String): Result<Boolean> {
        return try {
            // 模拟网络请求
            kotlinx.coroutines.delay(1000)
            // 这里应该调用实际的API设置密码
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun modifyPaymentPassword(oldPassword: String, newPassword: String): Result<Boolean> {
        return try {
            // 模拟网络请求
            kotlinx.coroutines.delay(1000)
            // 这里应该调用实际的API修改密码
            if (oldPassword == "123456") {
                Result.success(true)
            } else {
                Result.failure(Exception("原密码错误"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun hasPaymentPassword(): Result<Boolean> {
        return try {
            // 模拟网络请求
            kotlinx.coroutines.delay(500)
            // 这里应该调用实际的API检查是否已设置密码
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}