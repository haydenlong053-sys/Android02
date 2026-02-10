package com.cboda.intelligent.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 弹窗预览组件
 */
@Composable
fun DialogPreviewScreen() {
    var showInsufficientTokenDialog by remember { mutableStateOf(false) }
    var showCreateFailedDialog by remember { mutableStateOf(false) }
    var showValidationFailedDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { showInsufficientTokenDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("显示ODIC不足弹窗")
        }

        Button(
            onClick = { showCreateFailedDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("显示创建失败弹窗")
        }

        // 验证失败弹窗测试按钮
        Button(
            onClick = { 
                validationMessage = "请选择会议室名称"
                showValidationFailedDialog = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("测试：缺少会议室名称")
        }

        Button(
            onClick = { 
                validationMessage = "请选择会议时长和会议人数"
                showValidationFailedDialog = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("测试：缺少多个字段")
        }

        Button(
            onClick = { 
                validationMessage = "请完善以下信息：会议室名称、开始日期、会议时长、会议人数"
                showValidationFailedDialog = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("测试：缺少多个字段（长文本）")
        }
    }

    // 代币不足弹窗
    if (showInsufficientTokenDialog) {
        InsufficientTokenDialog(
            onDismiss = { showInsufficientTokenDialog = false }
        )
    }

    // 创建失败弹窗
    if (showCreateFailedDialog) {
        CreateFailedDialog(
            message = "已有会议正在进行，请先处理当前会议或更换时间",
            onDismiss = { showCreateFailedDialog = false }
        )
    }

    // 验证失败弹窗
    if (showValidationFailedDialog) {
        ValidationFailedDialog(
            message = validationMessage,
            onDismiss = { showValidationFailedDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DialogPreviewScreenPreview() {
    DialogPreviewScreen()
}

@Preview(showBackground = true)
@Composable
fun InsufficientTokenDialogPreview() {
    InsufficientTokenDialog(
        onDismiss = { }
    )
}

@Preview(showBackground = true)
@Composable
fun CreateFailedDialogPreview() {
    CreateFailedDialog(
        message = "已有会议正在进行，请先处理当前会议或更换时间",
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ValidationFailedDialogPreview() {
    ValidationFailedDialog(
        message = "请选择会议室名称",
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ValidationFailedDialogLongMessagePreview() {
    ValidationFailedDialog(
        message = "请完善以下信息：会议室名称、开始日期、会议时长、会议人数",
        onDismiss = {}
    )
}
@Preview(showBackground = true)
@Composable
fun SetPaymentPasswordDialogPreview() {
    SetPaymentPasswordDialog(
        onSetPassword = {},
        onDismiss = {}
    )
}