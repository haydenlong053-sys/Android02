package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.R
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.viewmodel.WTViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTWalletDetailScreen(
    name: String,
    address: String,
    isWatch: Boolean,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onExportMnemonic: () -> Unit,
    onExportPrivateKey: () -> Unit,
    onConvert: () -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConvertDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf(name) }

    LaunchedEffect(Unit) {
        viewModel.getSafeCenterInfo()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wt_wallet_detail_title),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.size(48.dp).noRippleClickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.wt_back),
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F8FA)
                )
            )
        },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wallet Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_wt_bsc),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400,
                                color = Color.Black
                            )
                            if (isWatch) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFE8EAF6),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.wt_watch_wallet_tag),
                                        color = Color(0xFF5B6CFF),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.mipmap.ic_wt_edit),
                                contentDescription = stringResource(R.string.wt_edit),
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(16.dp)
                                    .noRippleClickable { showRenameDialog = true }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isWatch) {
                // Actions for Identity Wallet
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        DetailActionItem(text = stringResource(R.string.wt_export_mnemonic), onClick = onExportMnemonic)
                        Divider(color = Color.Black.copy(0.16f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 0.dp))
                        DetailActionItem(text = stringResource(R.string.wt_export_private_key), onClick = onExportPrivateKey)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isWatch) {
                // Convert Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B47DD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .noRippleClickable { showConvertDialog = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.wt_convert_to_transaction_wallet),
                            color = Color(0xFF3B47DD),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Delete Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFEBEE), // Pink/Red background
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .noRippleClickable { showDeleteDialog = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.wt_delete_wallet),
                        color = Color(0xFFEB534B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500
                    )
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        WalletInputDialog(
            title = stringResource(R.string.wt_input_remark_name),
            initialValue = currentName,
            placeholder = stringResource(R.string.wt_max_20_chars),
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                currentName = newName
                onRename(newName)
                showRenameDialog = false
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        WalletConfirmDialog(
            title = stringResource(R.string.wt_delete_wallet),
            message = if (isWatch) stringResource(R.string.wt_delete_watch_wallet_confirm) else stringResource(R.string.wt_delete_wallet_confirm),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                showPasswordDialog = true
            }
        )
    }

    WTSecurityVerification(
        showDialog = showPasswordDialog,
        onDismiss = { showPasswordDialog = false },
        onPassWordVerifySuccess = { _ ->
            onDelete()
        },
        viewModel = viewModel
    )

    // Convert Dialog
    if (showConvertDialog) {
        WalletConfirmDialog(
            title = stringResource(R.string.wt_tip),
            message = stringResource(R.string.wt_convert_need_import_hint),
            onDismiss = { showConvertDialog = false },
            onConfirm = {
                showConvertDialog = false
                onConvert()
            }
        )
    }
}

@Composable
fun DetailActionItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 16.sp, color = Color.Black.copy(0.9f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun WalletInputDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                // Input Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(text = placeholder, color = Color.Gray, fontSize = 14.sp)
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = text,
                        onValueChange = { if (it.length <= 20) text = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5B6CFF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .noRippleClickable(onClick = onDismiss)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.wt_cancel), color = Color(0xFF5B6CFF))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF5B6CFF),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .noRippleClickable { onConfirm(text) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.wt_confirm), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5B6CFF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .noRippleClickable(onClick = onDismiss)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.wt_cancel), color = Color(0xFF5B6CFF))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF5B6CFF),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .noRippleClickable(onClick = onConfirm)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.wt_confirm), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWTWalletDetailScreen() {
    WTWalletDetailScreen(
        name = "Wallet 8081",
        address = "0x6cb5742b...8081",
        isWatch = false,
        onBack = {},
        onRename = {},
        onDelete = {},
        onExportMnemonic = {},
        onExportPrivateKey = {},
        onConvert = {}
    )
}

@Preview
@Composable
fun PreviewWTWalletDetailScreenWatch() {
    WTWalletDetailScreen(
        name = "Watch Wallet",
        address = "0x12345678...90AB",
        isWatch = true,
        onBack = {},
        onRename = {},
        onDelete = {},
        onExportMnemonic = {},
        onExportPrivateKey = {},
        onConvert = {}
    )
}
