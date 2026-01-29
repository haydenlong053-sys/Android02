package com.cboda.intelligent.compose.wallet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.R
import com.cboda.intelligent.model.AddTokenRequest
import com.cboda.intelligent.model.TokenPreviewRequest
import com.cboda.intelligent.repository.WTRepository
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.viewmodel.WTViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTAddTokenScreen(
    onBack: () -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Reset state on entry to ensure fresh start
    LaunchedEffect(Unit) {
        viewModel.resetActionState()
    }
    
    var contractAddress by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var decimals by remember { mutableStateOf("") }

    // Update state when preview returns
    LaunchedEffect(uiState.tokenPreview) {
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
            // Reset success state logic should be in VM or handled by navigating away
        }
        if (uiState.actionError != null) {
             Toast.makeText(context, uiState.actionError, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.wt_add_token_title),  fontSize = 16.sp,
                    fontWeight = FontWeight.W500) },
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
                    containerColor = Color(0xFFF7F8FA)
                )
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
                    // Trigger preview if address length is sufficient (e.g. 42 for EVM)
                    if (it.length >= 42) {
                        viewModel.previewToken(TokenPreviewRequest(WTRepository.wtDefaultChanId, it))
                    }
                },
                placeholder = stringResource(R.string.wt_input_contract_address)
            )

            // Symbol Input
            WTInput(
                label = stringResource(R.string.wt_token_symbol),
                value = symbol,
                onValueChange = { symbol = it },
                placeholder = stringResource(R.string.wt_input_token_symbol)
            )

            // Decimals Input
            WTInput(
                label = stringResource(R.string.wt_token_decimals),
                value = decimals,
                onValueChange = { decimals = it },
                placeholder = stringResource(R.string.wt_input_token_decimals)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    if (contractAddress.isNotBlank()) {
                         viewModel.addToken(AddTokenRequest(WTRepository.wtDefaultChanId, token_address = contractAddress))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B47DD))
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
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black.copy(0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
//                .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            color = Color.Black.copy(0.35f),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            },
            cursorBrush = SolidColor(Color(0xFF25A6FF))
        )
    }
}

@Preview
@Composable
fun PreviewWTAddTokenScreen() {
    WTAddTokenScreen(onBack = {})
}
