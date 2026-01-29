package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.viewmodel.WTViewModel
import com.cboda.intelligent.viewmodel.WalletCreationState
import com.cboda.intelligent.R

import androidx.compose.ui.tooling.preview.Preview
import com.cboda.intelligent.utils.ToastBroadUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTImportScreen(
    onBack: () -> Unit,
    onImportSuccess: () -> Unit = onBack,
    expectedAddress: String? = null,
    viewModel: WTViewModel = viewModel()
) {
    val importWalletState by viewModel.importWalletState.collectAsState()
    var showLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(importWalletState) {
        when (val state = importWalletState) {
            is WalletCreationState.Loading -> {
                showLoading = true
                errorMessage = null
            }
            is WalletCreationState.Success -> {
                showLoading = false
                viewModel.resetLocalState()
                onImportSuccess()
            }
            is WalletCreationState.Error -> {
                showLoading = false
                errorMessage = state.message
            }
            else -> {
                showLoading = false
                errorMessage = null
            }
        }
    }

    if (showLoading) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF25A6FF))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.wt_importing_wallet), fontSize = 16.sp)
                }
            }
        }
    }

    if (errorMessage != null) {

        ToastBroadUtil.show(stringResource(R.string.wt_import_failed))
    }

    WTImportContent(
        onBack = onBack,
        isConvert = expectedAddress != null,
        onConfirm = { mnemonic, name ->
            viewModel.importLocalWallet(mnemonic, name, expectedAddress)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTImportContent(
    onBack: () -> Unit,
    isConvert: Boolean = false,
    onConfirm: (String, String) -> Unit
) {
    var mnemonic by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.wt_import_wallet_title), 
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
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Mnemonic Input
            Text(
                text = stringResource(R.string.wt_import_mnemonic_or_private_key),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.wt_import_hint),
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Custom Input Area with Clear/Paste buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                if (mnemonic.isEmpty()) {
                    Text(
                        text = stringResource(R.string.wt_input_mnemonic_or_private_key),
                        color = Color(0xFFCCCCCC),
                        fontSize = 14.sp
                    )
                }
                
                BasicTextField(
                    value = mnemonic,
                    onValueChange = { mnemonic = it },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.fillMaxSize(),
                    cursorBrush = SolidColor(Color(0xFF25A6FF))
                )

                // Clear / Paste Buttons
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = stringResource(R.string.wt_clear),
                        color = Color(0xFF25A6FF),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { mnemonic = "" }
                            .padding(8.dp)
                    )
                    Text(
                        text = "|",
                        color = Color(0xFFDDDDDD),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.wt_paste),
                        color = Color(0xFF25A6FF),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                clipboardManager.getText()?.text?.let {
                                    mnemonic = it
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Network
            Text(
                text = stringResource(R.string.wt_network),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BSC Icon
                Image(
                    painter = painterResource(id = R.mipmap.ic_wt_bsc_small),
                    contentDescription = "BSC",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "BSC",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Wallet Remark
            if (!isConvert) {
                Text(
                    text = stringResource(R.string.wt_wallet_remark),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (walletName.isEmpty()) {
                                Text(
                                    stringResource(R.string.wt_input_placeholder),
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    },
                    cursorBrush = SolidColor(Color(0xFF25A6FF))
                )

                Spacer(modifier = Modifier.height(40.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Confirm Button
            Button(
                onClick = { 
                    onConfirm(mnemonic, walletName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF56BAFF), // Using lighter blue from theme
                    disabledContainerColor = Color(0xFF56BAFF).copy(alpha = 0.5f)
                ),
                enabled = mnemonic.isNotEmpty() && (isConvert || walletName.isNotEmpty())
            ) {
                Text(
                    text = stringResource(R.string.wt_confirm_import),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview
@Composable
fun PreviewWTImportScreen() {
    WTImportContent(
        onBack = {},
        onConfirm = { _, _ -> }
    )
}
