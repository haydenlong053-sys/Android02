package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cboda.intelligent.R

import androidx.compose.ui.draw.blur
import com.cboda.intelligent.compose.noRippleClickable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.viewmodel.WTViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTExportPrivateKeyScreen(
    privateKey: String,
    onBack: () -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showSecurityTipDialog by remember { mutableStateOf(false) }
    var isRevealed by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var verificationAction by remember { mutableStateOf(0) } // 1: Reveal, 2: Copy

    WTSecurityVerification(
        showDialog = showPasswordDialog,
        onDismiss = { showPasswordDialog = false },
        onPassWordVerifySuccess = {
            when (verificationAction) {
                1 -> isRevealed = true
                2 -> showSecurityTipDialog = true
            }
        },
        viewModel = viewModel
    )

    if (showSecurityTipDialog) {
        SecurityTipDialog(
            onDismiss = { showSecurityTipDialog = false },
            onConfirm = {
                showSecurityTipDialog = false
                clipboardManager.setText(AnnotatedString(privateKey))
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.wt_export_private_key_title), 
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, 
                            contentDescription = stringResource(R.string.wt_back), 
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF9F9F9)
                )
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Text(
                text = stringResource(R.string.wt_export_private_key_warning),
                fontSize = 14.sp,
                color = Color(0xFFFF5252),
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 44.dp)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = privateKey,
                        color = Color(0xFF3B47DD),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400,
                        lineHeight = 16.41.sp,
                        modifier = Modifier
                            .padding(12.dp)
                            .blur(if (isRevealed) 0.dp else 12.dp)
                    )

                    if (!isRevealed) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .noRippleClickable {
                                    verificationAction = 1
                                    showPasswordDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Color(0xFF1D2129).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.mipmap.ic_wt_eye_open),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.wt_click_to_show_private_key),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    verificationAction = 2
                    showPasswordDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B47DD))
            ) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_wt_backup_copy),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.wt_copy_private_key),
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B47DD)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(
                    text = stringResource(R.string.wt_backed_up),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = Color(0xFF3B47DD)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWTExportPrivateKeyScreen() {
    WTExportPrivateKeyScreen(
        privateKey = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
        onBack = {}
    )
}
