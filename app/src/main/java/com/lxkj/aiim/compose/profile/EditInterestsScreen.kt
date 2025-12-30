package com.lxkj.aiim.compose.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lxkj.aiim.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInterestsScreen(
    navController: NavController,
    initialList: List<String>,
    onConfirm: (list: List<String>) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var currentInterests by remember { mutableStateOf(initialList) }

    // 限制最大数量
    val maxInterests = 6

    // 逻辑：是否显示底部的“新增”输入框
    // 如果当前数量 < 6，则允许新增
    val canAddMore = currentInterests.size < maxInterests

    // 本地临时状态，用于“新增”那个输入框的内容
    var newInterestInput by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
                expandedHeight = 46.dp,
                navigationIcon = {
                    if (onBack != null) {
                        Icon(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onBack.invoke()
                            },
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "返回",
                            tint = Color.Black,
                        )
                    }
                },
                title = {
                    Text(
                        text = "兴趣标签",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
//                                navController.popBackStack()
                                if (newInterestInput.trim().isNotEmpty()){
                                    val newList = currentInterests.toMutableList()
                                    newList.add(newInterestInput.trim())
                                    currentInterests = newList
                                    newInterestInput = "" // 重置输入框
                                }
                                if (currentInterests.isNotEmpty()) {
                                    onConfirm.invoke(currentInterests)
                                }


                            }) {
                        Text(
                            text = "保存",
                            fontSize = 14.sp,
                            color = Color(0xff3B47DD)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "我的爱好",
                fontSize = 14.sp,
                color = Color.Black.copy(0.65f)
            )

            // 现有列表 (可编辑)
            currentInterests.forEachIndexed { index, interest ->
                Spacer(Modifier.height(8.dp))
                ListInputField(
                    value = interest,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            val newList = currentInterests.toMutableList()
                            newList.removeAt(index)
                            currentInterests = newList
//                            viewModel.updateInterests(newList)
                        } else {
                            // 修改现有列表中的某一项
                            val newList = currentInterests.toMutableList()
                            if (newValue.length <= 16) {
                                newList[index] = newValue
                            } else {
                                newList[index] = newValue.take(16)
                            }
                            currentInterests = newList
//                        viewModel.updateInterests(newList)
                        }

                    },
                    onDoneListener = {

                    },
                    hint = "请输入",
                )
//                OutlinedTextField(
//                    value = interest,
//                    onValueChange = { newValue ->
//                        // 修改现有列表中的某一项
//                        val newList = currentInterests.toMutableList()
//                        newList[index] = newValue
////                        viewModel.updateInterests(newList)
//                    },
//                    label = { Text("Interest ${index + 1}") },
//                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
//                    trailingIcon = {
//                        // 可选：添加删除按钮
//                        IconButton(onClick = {
//                            val newList = currentInterests.toMutableList()
//                            newList.removeAt(index)
////                            viewModel.updateInterests(newList)
//                        }) { Text("X") }
//                    }
//                )
            }

            // 特殊逻辑：新增输入框
            if (canAddMore) {
                Spacer(Modifier.height(12.dp))
                ListInputField(
                    value = newInterestInput,
                    onValueChange = { newValue ->
                        newInterestInput = newValue
//                        if (newValue.isEmpty()){
////                            val newList = currentInterests.toMutableList()
////                            newList.removeAt(index)
////                            viewModel.updateInterests(newList)
//                        }else{
//                            if (newInterestInput.isNotEmpty()) {
//                                val newList = currentInterests.toMutableList()
//                                newList.add(newInterestInput)
//                                currentInterests=newList
//                                newInterestInput = "" // 重置输入框
//                            }
//
//                        }

                    },
                    onDoneListener = {
                        // 点击键盘回车键时，将输入内容添加到列表并清空输入框
                        if (newInterestInput.isNotEmpty() && newInterestInput.isNotBlank()) {
                            val newList = currentInterests.toMutableList()
                            newList.add(newInterestInput.trim())
                            currentInterests = newList
                            newInterestInput = "" // 重置输入框
                        }
                    },
                    hint = "请输入",
                )
//                OutlinedTextField(
//                    value = newInterestInput,
//                    onValueChange = {
//                        newInterestInput = it
//
//                        // 逻辑：一旦用户在“新增框”输入了内容，我们可以选择：
//                        // 1. 只有点击 Done 才加入列表
//                        // 2. 或者输入时实时加入？
//                        // 通常 UX 是：输入完成后，失去焦点或点击确认加入。
//                        // 这里演示：如果不为空，自动将其作为新条目加入列表，并清空输入框让用户继续输（类似自动聚焦下一行）
//                        // 但为了稳妥，这里建议用 Icon Button 确认添加，或者监听 Keyboard Action。
//                    },
//                    label = { Text("Add new interest...") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = MaterialTheme.colorScheme.primary,
//                        unfocusedBorderColor = Color.LightGray
//                    ),
//                    trailingIcon = {
//                        if (newInterestInput.isNotEmpty()) {
//                            IconButton(onClick = {
//                                val newList = currentInterests.toMutableList()
//                                newList.add(newInterestInput)
//                                currentInterests=newList
////                                viewModel.updateInterests(newList)
//                                newInterestInput = "" // 重置输入框
//                            }) {
//                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
//                            }
//                        }
//                    }
//                )
                Text(
                    text = "您还能添加 ${maxInterests - currentInterests.size} 个爱好。",
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.65f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "最多设置6个爱好",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ListInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onDoneListener: () -> Unit,
    hint: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        val fieldTextStyle = TextStyle(
            color = Color.Black.copy(0.9f),
            fontSize = 14.sp
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = fieldTextStyle,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            minLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,           // 键盘右下角显示“完成”
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDoneListener.invoke()

                }
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint.ifEmpty { "请输入" },
                            style = fieldTextStyle.copy(color = Color.Black.copy(0.35f))
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview
@Composable
fun EditInterestsScreenPreview() {
    EditInterestsScreen(rememberNavController(), emptyList(), {}, {})

}