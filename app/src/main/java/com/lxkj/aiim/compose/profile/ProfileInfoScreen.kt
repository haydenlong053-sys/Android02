package com.lxkj.aiim.compose.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lxkj.aiim.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onExternalBack: () -> Unit // <--- 新增参数：外部返回回调
) {
    val state by viewModel.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }

    val isModified = viewModel.isModified

    // 状态 C: 有数据修改，拦截物理返回键
    BackHandler(enabled = isModified) {
        showDiscardDialog = true
    }

    // 放弃修改确认弹窗
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Do you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.discardChanges() // 还原数据
                    showDiscardDialog = false
                    onExternalBack() // <--- 关键修改：退出 Compose 界面
                }) { Text("Discard", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Info") },
                navigationIcon = {
                    IconButton(onClick = {
                        // 如果有修改，显示 Dialog；否则，执行外部返回操作
                        if (isModified) showDiscardDialog = true else onExternalBack() // <--- 关键修改：退出 Compose 界面
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // 状态 B: 仅当有修改时显示保存按钮
                    if (isModified) {
                        TextButton(onClick = {
                            viewModel.saveChanges()
                            // 可选：保存成功后，如果需要直接退出，可在此调用 onExternalBack()
                        }) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // ... (其他静态信息，如头像、昵称等)
            ProfileItem(label = "Nickname", value = state.nickname)
            HorizontalDivider()

            // 跳转修改职业 (内部导航)
            ProfileItem(
                label = "Job",
                value = state.job,
                onClick = { navController.navigate("edit_job") }
            )
            HorizontalDivider()

            // 跳转修改兴趣 (内部导航)
            ProfileItem(
                label = "Interests",
                value = state.interests.joinToString(", "),
                onClick = { navController.navigate("edit_interests") }
            )
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}