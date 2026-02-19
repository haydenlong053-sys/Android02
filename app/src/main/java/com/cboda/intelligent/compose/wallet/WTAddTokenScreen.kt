package com.cboda.intelligent.compose.wallet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.model.AddTokenRequest
import com.cboda.intelligent.model.TokenPreviewRequest
import com.cboda.intelligent.repository.WTRepository
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.utils.WalletManager
import com.cboda.intelligent.viewmodel.WTViewModel
import kotlinx.coroutines.delay
import com.cboda.intelligent.R
import com.cboda.intelligent.compose.widget.ComposeNormalScreenToolBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTAddTokenScreen(
    onBack: () -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentWallet = uiState.currentWallet
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Reset state on entry to ensure fresh start
    LaunchedEffect(Unit) {
        viewModel.resetActionState()
    }
    
    var contractAddress by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var decimals by remember { mutableStateOf("") }

    // Debounce for contract address input to trigger preview
    LaunchedEffect(contractAddress) {
        if (contractAddress.length >= 40) { // EVM address is 42 chars usually
            delay(1000) // Wait for user to finish typing
            if (WalletManager.isValidAddress(contractAddress.lowercase())) {
                viewModel.previewToken(TokenPreviewRequest(WTRepository.wtDefaultChanId, contractAddress))
            }else{
                ToastBroadUtil.show("地址不合法")
            }
        }
    }

    // Update state when preview returns
    LaunchedEffect(uiState.tokenPreview, uiState.actionError) {
        if (uiState.actionError != null && contractAddress.length >= 40) {
             // Only if we were expecting a preview or add token result
             // But actionError is shared. 
             // If we just typed an address and got an error, it's likely from preview.
             // We should check if we are in 'preview' mode or 'add' mode?
             // The VM sets actionError for both.
             // User requirement: "if error, prompt error"
             // And "if not success, show -"
             symbol = "-"
             decimals = "-"
             // Toast is already shown in the other LaunchedEffect?
             // Let's consolidate error handling
        }
        
        uiState.tokenPreview?.let {
            if (it.token_address.equals(contractAddress, ignoreCase = true)) {
                symbol = it.symbol
                decimals = it.decimals.toString()
            }
        }
    }

    // Handle add token success/error
    LaunchedEffect(uiState.isActionSuccess, uiState.actionError) {
        if (uiState.isActionSuccess) {
            ToastBroadUtil.show(context.getString(R.string.tianjaichengg))
            onBack()
        }
        if (uiState.actionError != null) {
             Toast.makeText(context, uiState.actionError, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            ComposeNormalScreenToolBar(
                stringResource(R.string.wt_add_token_title),
                onBack=onBack,
                backgroundColor = Color(0xFFF7F8FA)
            )
        
        },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contract Address Input
            WTInput(
                label = stringResource(R.string.wt_token_contract),
                value = contractAddress,
                onValueChange = { 
                    contractAddress = it
                },
                placeholder = stringResource(R.string.wt_input_contract_address),
                trailingIcon = {
                    val isAddressEmpty = contractAddress.isEmpty()
                    Text(
                        text = if (isAddressEmpty) stringResource(R.string.wt_paste) else stringResource(R.string.wt_clear),
                        color = Color(0xFF3B47DD),
                        fontSize = 14.sp,
                        modifier = Modifier.noRippleClickable {
                            if (isAddressEmpty) {
                                clipboardManager.getText()?.text?.let { text ->
                                    contractAddress = text
                                }
                            } else {
                                contractAddress = ""
                                symbol = ""
                                decimals = ""
                            }
                        }
                    )
                }
            )

            // Symbol Input
            WTInput(
                label = stringResource(R.string.wt_token_symbol),
                value = symbol,
                onValueChange = { },
                placeholder = "-",
                readOnly = true
            )

            // Decimals Input
            WTInput(
                label = stringResource(R.string.wt_token_decimals),
                value = decimals,
                onValueChange = { },
                placeholder = "-",
                readOnly = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            val isEnabled = contractAddress.isNotBlank() && symbol.isNotBlank() && symbol != "-"
            Button(
                onClick = {
                    viewModel.addToken(AddTokenRequest(WTRepository.wtDefaultChanId, token_address = contractAddress, address = currentWallet?.address?:""))
                },
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B47DD),
                    disabledContainerColor = Color(0xFF3B47DD).copy(alpha = 0.45f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = stringResource(R.string.baocunle), fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WTInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black.copy(0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = Color.Black.copy(0.35f),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly,
                    enabled = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(Color(0xFF25A6FF))
                )
            }
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Preview
@Composable
fun PreviewWTAddTokenScreen() {
    WTAddTokenScreen(onBack = {})
}
