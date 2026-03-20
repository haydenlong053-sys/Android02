package com.cboda.intelligent.compose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cboda.intelligent.R
import com.cboda.intelligent.model.TimeOption
import com.cboda.intelligent.model.UserTierBroadOption
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@Composable
fun TitleBar(
    statusBarHeight: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp + statusBarHeight)
            .background(
                Brush.verticalGradient(
                colors = listOf(Color(0xFF9EB4F5),Color(0x0097CAF7))
            )
    ) ){
        // 背景图片
//        Image(
//            painter = painterResource(id = R.mipmap.ic_bg_top_broad_main),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.FillWidth,
//            alignment = Alignment.TopCenter
//        )
        
        // 标题栏内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = statusBarHeight)
        ) {
            // 返回按钮
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_broad_back),
                    contentDescription = "返回",
                    tint = Color.Black
                )
            }
            
            // 标题
            Text(
                text = stringResource(com.cboda.chatui.R.string.创建会议主题),
                color = colorResource(id = R.color.black),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * 代币不足弹窗
 */
@Composable
fun InsufficientTokenDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "ODIC不足",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 提示内容
                Text(
                    text = "请前往资金ODIC中心充值",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 确定按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_price_blue)
                    )
                ) {
                    Text(
                        text = "好的",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 验证失败提示弹窗
 */
@Composable
fun ValidationFailedDialog(
    message: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "创建失败",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 提示信息
                Text(
                    text = message ?: "请完善会议信息",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 确认按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B47DD)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "我知道了",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 创建失败弹窗
 */
@Composable
fun CreateFailedDialog(
    message: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "创建失败",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 错误信息
                Text(
                    text = message ?: "已有会议正在进行，请先处理当前会议或更换时间",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 确定按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_price_blue)
                    )
                ) {
                    Text(
                        text = "好的",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RoomNameInputField(
    roomName: String,
    onRoomNameChange: (String) -> Unit,
    groupChatName: String = "" // 从前面页面传入的群聊名称
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.color_content_bg),
                    shape = RoundedCornerShape(10.dp)
                )
                .height(56.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Text(
//                    text = "会议室名称",
//                    color = colorResource(id = R.color.color_text0),
//                    fontSize = 16.sp,
//                    modifier = Modifier.width(100.dp)
//                )
//
//                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    BasicTextField(
                        value = roomName,
                        onValueChange = onRoomNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = colorResource(id = R.color.black),
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (roomName.isEmpty()) {
                                        // 显示占位符，包含群聊名称
                                        val placeholder = if (groupChatName.isNotEmpty()) {
                                            "$groupChatName 会议室"
                                        } else {
                                            "请输入会议室名称"
                                        }
                                        Text(
                                            text = placeholder,
                                            color = colorResource(id = R.color.black).copy(alpha = 0.6f),
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                                
                                // 清除按钮
                                if (roomName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(id = R.mipmap.ic_meet_edit_broad_clear),
                                        contentDescription = "清除",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onRoomNameChange("") },
                                        tint = colorResource(id = R.color.black_45)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
        }
    }


@Composable
fun StartTimeSection(
    selectedDate: String,
    selectedTime: String,
    onDateTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.color_content_bg),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onDateTimeClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开始时间",
                    color = colorResource(id = R.color.color_text0),
                    fontSize = 16.sp,
                    modifier = Modifier.width(80.dp) // 减少标签宽度，给时间文本更多空间
                )
                
                Spacer(modifier = Modifier.width(8.dp)) // 减少间距
                
                Text(
                    text = "$selectedDate $selectedTime",
                    color = colorResource(id = R.color.color_text_content),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                    // 移除 maxLines 和 overflow 限制，让时间完整显示
                )
                
                Spacer(modifier = Modifier.width(8.dp)) // 减少间距
                
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_broad_right),
                    contentDescription = "选择时间",
                    tint = colorResource(id = R.color.color_text_content)
                )
            }
        }
    }
}

@Composable
fun OptionItem(
    title: String,
    selectedText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.color_content_bg),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(indication = null,interactionSource = remember { MutableInteractionSource() }) { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colorResource(id = com.cboda.baselibrary.R.color.black),
                    fontSize = 16.sp,
                    modifier = Modifier.width(100.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = selectedText,
                    color = colorResource(id = R.color.black_35),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_broad_right),
                    contentDescription = "选择",
                    tint = colorResource(id = com.cboda.baselibrary.R.color.black)
                )
            }
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.color_content_bg),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colorResource(id = com.cboda.baselibrary.R.color.black),
                    fontSize = 16.sp,
                    modifier = Modifier.width(100.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End
                ) {
                    // 使用自定义开关图标
                    Icon(
                        painter = painterResource(
                            id = if (isChecked) {
                                com.cboda.chatui.R.mipmap.ic_switch_broad_kai
                            } else {
                                com.cboda.chatui.R.mipmap.ic_switch_broad_guan
                            }
                        ),
                        contentDescription = if (isChecked) "开启" else "关闭",
                        modifier = Modifier
                            .size(48.dp, 28.dp)
//                            .clickable { onCheckedChange(!isChecked) }
                            .alpha(0.6f), // 增加 40% 透明度，并移除点击事件使其不可点击
                        tint = Color.Unspecified // 保持原始颜色
                    )
                }
            }
        }
    }
}

// 双开关项组件，用于全开麦和全禁言
@Composable
fun DoubleSwitchItem(
    firstTitle: String,
    firstIsChecked: Boolean,
    onFirstCheckedChange: (Boolean) -> Unit,
    secondTitle: String,
    secondIsChecked: Boolean,
    onSecondCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.color_content_bg),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Column {
                // 第一个开关项
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = firstTitle,
                        color = colorResource(id = R.color.color_text0),
                        fontSize = 16.sp,
                        modifier = Modifier.width(100.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (firstIsChecked) {
                                    com.cboda.chatui.R.mipmap.ic_switch_broad_kai
                                } else {
                                    com.cboda.chatui.R.mipmap.ic_switch_broad_guan
                                }
                            ),
                            contentDescription = if (firstIsChecked) "开启" else "关闭",
                            modifier = Modifier
                                .size(48.dp, 24.dp)
                                .clickable { onFirstCheckedChange(!firstIsChecked) },
                            tint = Color.Unspecified
                        )
                    }
                }
                
                // 分割线
                Divider(
                    color = colorResource(id = com.cboda.chatui.R.color.color_line_conversation_list),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // 第二个开关项
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = secondTitle,
                        color = colorResource(id = R.color.color_text0),
                        fontSize = 16.sp,
                        modifier = Modifier.width(100.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (secondIsChecked) {
                                    com.cboda.chatui.R.mipmap.ic_switch_broad_kai
                                } else {
                                    com.cboda.chatui.R.mipmap.ic_switch_broad_guan
                                }
                            ),
                            contentDescription = if (secondIsChecked) "开启" else "关闭",
                            modifier = Modifier
                                .size(48.dp, 24.dp)
                                .clickable { onSecondCheckedChange(!secondIsChecked) },
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingDurationSection(
    selectedDuration: TimeOption?,
    onDurationSelected: (TimeOption) -> Unit,
    timeOptions: List<TimeOption> = emptyList(),
    renewalRules: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // 标题
        Text(
            text = "会议时长",
            color = colorResource(id = R.color.black_65),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 去掉白色背景的时长选项网格
        DurationOptionsGrid(
            selectedDuration = selectedDuration,
            onDurationSelected = onDurationSelected,
            timeOptions = timeOptions,
            renewalRules = renewalRules
        )
    }
}

@Composable
fun DurationOptionsGrid(
    selectedDuration: TimeOption?,
    onDurationSelected: (TimeOption) -> Unit,
    timeOptions: List<TimeOption> = emptyList(),
    renewalRules: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (timeOptions.isNotEmpty()) {
            // 动态生成行数，每行最多3个选项
            val rows = timeOptions.chunked(2)
            
            rows.forEachIndexed { rowIndex, rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowOptions.forEach { option ->
                        DurationOptionItem(
                            option = option,
                            // 使用data class的equals方法进行完整对象比较
                            isSelected = selectedDuration == option,
                            onSelected = { onDurationSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 如果这一行不足3个选项，添加空白占位符
                    repeat(2 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                
                // 如果不是最后一行，添加间距
                if (rowIndex < rows.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 提示文字
        if (renewalRules.isNotEmpty()) {
            Text(
                text = renewalRules,
                color = colorResource(id = com.cboda.baselibrary.R.color.black_45),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DurationOptionItem(
    option: TimeOption,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .background(
                color = colorResource(R.color.color_content_bg),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF3B47DD) else colorResource(R.color.color_content_bg),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelected() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.durationName,
            color = if (isSelected) Color(0xFF3B47DD) else colorResource(id = com.cboda.baselibrary.R.color.black),
            fontSize = 14.sp
        )
    }
}

@Composable
fun BottomSection(
    totalPrice: Int,
    originalPrice: Int? = null,
    canCreate: Boolean,
    isLoading: Boolean,
    isPriceLoading: Boolean = false,
    showPriceLoadingIndicator: Boolean = true, // 新增参数：控制是否显示价格加载指示器
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(color = colorResource(R.color.color_content_bg))
            .fillMaxWidth()
            .height(62.dp) // 总高度62dp
            .padding(horizontal = 16.dp, vertical = 9.dp) // 上下各9dp，剩余44dp为边框高度
    ) {
        // 两个拼接的区域，有边框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp) // 边框高度44dp
                .border(
                    width = 1.dp,
                    color = Color(0xFF3B47DD),
                    shape = RoundedCornerShape(10.dp)
                )
                .background(
                    color = Color(0x143B47DD),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // 左侧价格显示区域
            Box(
                modifier = Modifier
                    .weight(190f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                // 根据是否有原价动态调整对齐方式
                contentAlignment = if (originalPrice != null && originalPrice > totalPrice) {
                    Alignment.CenterStart // 有原价时左对齐
                } else {
                    Alignment.Center // 没有原价时居中
                }
            ) {
                if (isPriceLoading && showPriceLoadingIndicator) {
                    // 价格加载中的显示（仅在showPriceLoadingIndicator为true时显示）
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF3B47DD),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "计算价格中...",
                            color = Color(0xFF3B47DD),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } else {
                    // 正常价格显示：第二个 Text(ODIK) 相对第一个 Text(价格) 底部对齐
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "${totalPrice}",
                            color = Color(0xFF3B47DD),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "ODIC",
                            color = Color(0xFF3B47DD),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )

                        // 原价显示
                        originalPrice?.let { original ->
                            if (original > totalPrice) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "原价${original}ODIC",
                                    color = Color(0xFF9CA1A5),
                                    fontSize = 12.sp,
                                    style = androidx.compose.ui.text.TextStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // 分割线
//            Box(
//                modifier = Modifier
//                    .width(1.dp)
//                    .fillMaxHeight()
//                    .background(Color(0xFF3B47DD))
//            )
            
            // 右侧创建按钮区域
            Box(
                modifier = Modifier
                    .weight(153f)
                    .fillMaxHeight()
                    .background(
                        color = if (!isLoading) Color(0xFF3B47DD) else Color(0xFF3B47DD).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 10.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 10.dp
                        )
                    )
                    .clickable(enabled = !isLoading) { onCreateClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "创建",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<TimeOption>,
    onOptionSelected: (TimeOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.color_text_content),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(options) { option ->
                        Text(
                            text = option.durationName,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.color_text_content),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option) }
                                .padding(vertical = 12.dp)
                        )

                        if (option != options.last()) {
                            Divider(
                                color = colorResource(id = com.cboda.chatui.R.color.color_line_conversation_list),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 会议人数选择弹窗
@Composable
fun MeetingUserCountDialog(
    onOptionSelected: (UserTierBroadOption) -> Unit,
    onDismiss: () -> Unit,
    userTierOptions: List<UserTierBroadOption> = emptyList()
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 标题和关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "请选择会议人数",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.color_text_content)
                    )

                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "✕",
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.color_text_content)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 选项列表
                Column {
                    userTierOptions.forEachIndexed { index, option ->
                        Text(
                            text = option.tierName,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.color_text_content),
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionSelected(option)
                                    onDismiss()
                                }
                                .padding(vertical = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (index < userTierOptions.size - 1) {
                            Divider(
                                color = colorResource(id = com.cboda.chatui.R.color.color_line_conversation_list),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 确定按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B47DD)
                    ),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "确定",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTierSelectionDialog(
    userTierOptions: List<UserTierBroadOption>,
    currentSelection: UserTierBroadOption? = null,
    onOptionSelected: (UserTierBroadOption) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(currentSelection) }
    val bottomSate  = rememberModalBottomSheetState( skipPartiallyExpanded = true)

//    Dialog(onDismissRequest = onDismiss) {
//
//    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = bottomSate, dragHandle = {}, shape =RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        contentWindowInsets = {WindowInsets(0)}
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.color_content_bg))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 关闭按钮
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_close_broad_topic),
                        contentDescription = "关闭",
                        tint = Color(0xFFA7A9AC),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onDismiss() }
                    )

                    // 标题
                    Text(
                        text = "请选择会议人数",
                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
                        color = colorResource(com.cboda.baselibrary.R.color.black),
                        textAlign = TextAlign.Center
                    )

                    // 占位符保持对称
                    Spacer(modifier = Modifier.size(24.dp))
                }

                // 选项列表
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                ) {
                    userTierOptions.forEach { option ->
                        val isSelected = selectedOption == option
                        Text(
                            text = option.tierName, // 使用name字段显示人数范围
                            fontSize = 16.sp,
                            color = if (isSelected)  colorResource(R.color.color_price_blue) else colorResource(com.cboda.baselibrary.R.color.black),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) Color(0xFFF7F7F7) else Color.Transparent,
//                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedOption = option
                                }
                                .padding(vertical = 16.dp, horizontal = 12.dp)
                        )
                    }
                }

                // 确定按钮
                Button(
                    onClick = {
                        selectedOption?.let { option ->
                            onOptionSelected(option)
                        }
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 36.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.color_3B47DD),
                        disabledContainerColor = Color(0xFFF6F6f6)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.确定),
                        fontSize = 16.sp,
                        color = if (selectedOption!=null) Color.White else colorResource(R.color.black)
                    )
                }
            }
        }
    }
}

// 自定义日期选择器
@Composable
fun CustomDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "选择日期",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.color_text_content),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 年月日选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 年份选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "年",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.color_text_content),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        NumberPicker(
                            value = selectedYear,
                            range = 2024..2030,
                            onValueChange = { selectedYear = it }
                        )
                    }

                    // 月份选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "月",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.color_text_content),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        NumberPicker(
                            value = selectedMonth,
                            range = 1..12,
                            onValueChange = { selectedMonth = it }
                        )
                    }

                    // 日期选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "日",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.color_text_content),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        NumberPicker(
                            value = selectedDay,
                            range = 1..31,
                            onValueChange = { selectedDay = it }
                        )
                    }
                }

                // 按钮区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "取消",
                            color = colorResource(id = R.color.color_text_content)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(selectedYear, selectedMonth - 1, selectedDay)
                            }
                            onDateSelected(dateFormat.format(selectedCalendar.time))
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B47DD)
                        )
                    ) {
                        Text(
                            text = "确定",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// 数字选择器组件
@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .height(120.dp)
            .width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(range.toList()) { number ->
            Text(
                text = number.toString(),
                fontSize = if (number == value) 18.sp else 14.sp,
                fontWeight = if (number == value) FontWeight.Bold else FontWeight.Normal,
                color = if (number == value) Color(0xFF3B47DD) else colorResource(id = R.color.color_text_content),
                modifier = Modifier
                    .clickable { onValueChange(number) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

// 时间选择器辅助函数
fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            onTimeSelected(timeFormat.format(selectedCalendar.time))
        },
        hour,
        minute,
        true
    ).show()
}

// 支付密码弹窗
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPasswordDialog(
    onPasswordEntered: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
//    val focusManager = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // sheetState 保留用于监听展开（可选）
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 标志：输入框已被放置（placed）
    var inputPlaced by remember { mutableStateOf(false) }
    
    // 标志：跟踪键盘显示状态
    var keyboardRequested by remember { mutableStateOf(false) }
    
    // 错误状态和抖动动画
    var showError by remember { mutableStateOf(false) }
    var currentErrorMessage by remember { mutableStateOf<String?>(null) }
    val shakeAnimation = remember { Animatable(0f) }

    // 当输入框被放置后，触发一次焦点+键盘
    LaunchedEffect(inputPlaced) {
        if (inputPlaced) {
            // 给一点时间让底部弹窗动画稳定（经验值 50~250ms 可调整）
            // 不要用过短的 delay；也可以去掉 delay 看效果
            delay(200)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 监听外部错误消息变化，触发抖动动画
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            currentErrorMessage = errorMessage
            showError = true
            // 触发抖动动画
            repeat(3) {
                shakeAnimation.animateTo(10f, animationSpec = tween(50))
                shakeAnimation.animateTo(-10f, animationSpec = tween(50))
            }
            shakeAnimation.animateTo(0f, animationSpec = tween(50))
        }
    }

    // 点击格子区域弹起键盘的函数
    val requestFocusAndShowKeyboard = {
        scope.launch {
//            // 清理掉旧焦点，强制刷新
//            focusManager.clearFocus(force = true)
            delay(200)
            // 先请求焦点
            focusRequester.requestFocus()
            // 稍微延迟一下再显示键盘，确保焦点已经设置
            delay(50)
            keyboardController?.show()
            keyboardRequested = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 标题栏略...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_close_broad_topic),
                    contentDescription = "关闭",
                    tint = Color(0xFFA7A9AC),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismiss() }
                )

                Text(
                    text = "请输入支付密码",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 密码格子区域 - 使用Box来叠加隐藏的输入框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = shakeAnimation.value
                    }
            ) {
                // 显示的六个密码格子
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (showError) {
                                        Color.Red
                                    } else if (index < password.length) {
                                        Color(0xFF3B47DD)
                                    } else {
                                        Color(0xFFE5E5E5)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(Color.White, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index < password.length) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.Black, shape = CircleShape)
                                )
                            }
                        }
                    }
                }
                
                // 隐藏的输入框覆盖整个格子区域
                BasicTextField(
                    value = password,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            // 用户重新输入时隐藏错误提示
                            if (showError) {
                                showError = false
                                currentErrorMessage = null
                            }
                            
                            password = newValue
                            if (newValue.length == 6) {
                                // 不自动隐藏键盘和关闭弹窗，让验证逻辑处理
                                onPasswordEntered(newValue)
                            }
                        }
                    },
                    modifier = Modifier
                        // 覆盖整个Row区域
                        .fillMaxWidth()
                        .height(48.dp)
                        .alpha(0f)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            // 标记已放置一次（只需要一次）
                            if (!inputPlaced) inputPlaced = true
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (password.length == 6) {
                                // 不自动隐藏键盘，让验证逻辑处理
                                onPasswordEntered(password)
                            }
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 错误信息区域 - 预留固定空间避免高度变化
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp), // 固定高度，包含文本和间距
                contentAlignment = Alignment.Center
            ) {
                if (showError && currentErrorMessage != null) {
                    Text(
                        text = currentErrorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }



            Text(
                text = "为了您的账户安全，请输入6位数字支付密码",
                color = Color(0xFF999999),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}



// 设置支付密码提示弹窗
@Composable
fun SetPaymentPasswordDialog(
    onSetPassword: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标
//                Icon(
//                    painter = painterResource(id = R.mipmap.ic_close_broad_topic), // 这里应该用一个锁的图标
//                    contentDescription = "支付密码",
//                    tint = Color(0xFF3B47DD),
//                    modifier = Modifier.size(48.dp)
//                )

                Spacer(modifier = Modifier.height(16.dp))

                // 标题
                Text(
                    text = "设置支付密码",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 描述
                Text(
                    text = "您还未设置支付密码，请前往设置",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(
                            text = "取消",
                            color = Color(0xFF666666),
                            fontSize = 16.sp
                        )
                    }

                    // 去设置按钮
                    Button(
                        onClick = onSetPassword,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B47DD)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(
                            text = "去设置",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


// 显示系统日期选择器
fun showSystemDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    
    // 计算一个月后的日期
    val maxCalendar = Calendar.getInstance().apply {
        add(Calendar.MONTH, 1)
    }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            onDateSelected(dateFormat.format(selectedCalendar.time))
        },
        currentYear,
        currentMonth,
        currentDay
    )
    
    // 设置最小日期为当前日期
    datePickerDialog.datePicker.minDate = calendar.timeInMillis
    
    // 设置最大日期为一个月后
    datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis
    
    datePickerDialog.show()
}