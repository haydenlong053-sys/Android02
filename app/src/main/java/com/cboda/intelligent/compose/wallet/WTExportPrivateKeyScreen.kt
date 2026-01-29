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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTExportPrivateKeyScreen(
    privateKey: String,
    onBack: () -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showSecurityTipDialog by remember { mutableStateOf(false) }

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
                Text(
                    text = privateKey,
                    color = Color(0xFF3B47DD),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 16.41.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    showSecurityTipDialog = true
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
