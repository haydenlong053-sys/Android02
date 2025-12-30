package com.lxkj.aiim.lj

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lxkj.aiim.compose.*
import kotlinx.coroutines.launch

class PaymentPasswordTestActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PaymentPasswordTestScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPasswordTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val paymentManager = remember { PaymentPasswordManager() }
    
    var showDialog by remember { mutableStateOf(false) }
    var dialogState by remember { 
        mutableStateOf(
            PaymentPasswordState(
                mode = PaymentPasswordMode.PAYMENT,
                title = "输入支付密码",
                amount = "100.00",
                currency = "￥"
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "支付密码弹窗测试",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // 测试按钮组
        TestButton(
            text = "支付验证模式",
            onClick = {
                dialogState = PaymentPasswordState(
                    mode = PaymentPasswordMode.PAYMENT,
                    title = "输入支付密码",
                    amount = "100.00",
                    currency = "￥"
                )
                showDialog = true
            }
        )
        
        TestButton(
            text = "设置支付密码",
            onClick = {
                dialogState = PaymentPasswordState(
                    mode = PaymentPasswordMode.SET_PASSWORD
                )
                showDialog = true
            }
        )
        
        TestButton(
            text = "修改支付密码",
            onClick = {
                dialogState = PaymentPasswordState(
                    mode = PaymentPasswordMode.MODIFY_PASSWORD,
                    showOldPasswordInput = true
                )
                showDialog = true
            }
        )
        
        TestButton(
            text = "支付验证（无金额）",
            onClick = {
                dialogState = PaymentPasswordState(
                    mode = PaymentPasswordMode.PAYMENT,
                    title = "验证支付密码"
                )
                showDialog = true
            }
        )
        
        // 说明文字
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "测试说明：",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 正确密码：123456\n" +
                          "• 支持设置、修改、验证三种模式\n" +
                          "• 包含错误抖动动画\n" +
                          "• 自定义数字键盘\n" +
                          "• 模拟网络请求和错误处理",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp
                )
            }
        }
    }
    
    // 支付密码弹窗
    if (showDialog) {
        UnifiedPaymentPasswordDialog(
            state = dialogState,
            onPasswordEntered = { password, oldPassword ->
                scope.launch {
                    dialogState = dialogState.copy(isLoading = true, errorMessage = null)
                    
                    try {
                        when (dialogState.mode) {
                            PaymentPasswordMode.PAYMENT -> {
                                val result = paymentManager.verifyPaymentPassword(password)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "支付密码验证成功", Toast.LENGTH_SHORT).show()
                                    showDialog = false
                                } else {
                                    dialogState = dialogState.copy(
                                        isLoading = false,
                                        errorMessage = "密码错误，请重新输入"
                                    )
                                }
                            }
                            
                            PaymentPasswordMode.SET_PASSWORD -> {
                                if (dialogState.currentStep == 1) {
                                    // 第一次输入
                                    dialogState = dialogState.copy(
                                        currentStep = 2,
                                        firstPassword = password,
                                        isLoading = false
                                    )
                                } else {
                                    // 第二次输入，验证是否一致
                                    if (password == oldPassword) {
                                        val result = paymentManager.setPaymentPassword(password)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "支付密码设置成功", Toast.LENGTH_SHORT).show()
                                            showDialog = false
                                        } else {
                                            dialogState = dialogState.copy(
                                                isLoading = false,
                                                errorMessage = "设置失败，请重试"
                                            )
                                        }
                                    } else {
                                        dialogState = dialogState.copy(
                                            isLoading = false,
                                            errorMessage = "两次输入密码不一致",
                                            currentStep = 1,
                                            firstPassword = ""
                                        )
                                    }
                                }
                            }
                            
                            PaymentPasswordMode.MODIFY_PASSWORD -> {
                                if (dialogState.showOldPasswordInput) {
                                    // 验证旧密码
                                    val result = paymentManager.verifyPaymentPassword(oldPassword ?: "")
                                    if (result.isSuccess) {
                                        dialogState = dialogState.copy(
                                            showOldPasswordInput = false,
                                            isLoading = false
                                        )
                                    } else {
                                        dialogState = dialogState.copy(
                                            isLoading = false,
                                            errorMessage = "原密码错误"
                                        )
                                    }
                                } else {
                                    // 设置新密码
                                    val result = paymentManager.modifyPaymentPassword(oldPassword ?: "", password)
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "支付密码修改成功", Toast.LENGTH_SHORT).show()
                                        showDialog = false
                                    } else {
                                        dialogState = dialogState.copy(
                                            isLoading = false,
                                            errorMessage = "修改失败，请重试"
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        dialogState = dialogState.copy(
                            isLoading = false,
                            errorMessage = "网络错误，请重试"
                        )
                    }
                }
            },
            onDismiss = {
                showDialog = false
                // 重置状态
                dialogState = dialogState.copy(
                    currentStep = 1,
                    firstPassword = "",
                    showOldPasswordInput = dialogState.mode == PaymentPasswordMode.MODIFY_PASSWORD,
                    isLoading = false,
                    errorMessage = null
                )
            },
            onForgotPassword = {
                Toast.makeText(context, "跳转到忘记密码页面", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun TestButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF25A6FF)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}