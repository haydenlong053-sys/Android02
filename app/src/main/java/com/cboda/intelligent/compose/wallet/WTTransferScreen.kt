package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.R
import com.cboda.intelligent.model.AssetItem
import com.cboda.intelligent.viewmodel.WTViewModel
import java.math.BigDecimal
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import kotlinx.coroutines.launch

import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.TextField
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import coil.compose.SubcomposeAsyncImage
import com.cboda.baselibrary.utils.EncryptUtil
import com.cboda.intelligent.activity.ScanBroadActivity
import com.cboda.intelligent.compose.formatSixDecimalChange
import com.cboda.intelligent.compose.formatSixDecimalValue
import com.cboda.intelligent.compose.getFormattedAmount
import com.cboda.intelligent.compose.getOriginalAmount
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.compose.widget.ComposeNormalScreenToolBar
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.utils.WalletManager
import org.web3j.utils.Convert
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTTransferScreen(
    initialAddress: String?,
    initialAmount: String?,
    initialSymbol: String?,
    initialTokenAddress: String?,
    onBack: () -> Unit,
    onConfirmSend: () -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val assetList = uiState.assetList
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var showTokenSheet by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getSafeCenterInfo()
    }

    val defaultAsset = remember(assetList, initialSymbol) {
        if (!initialSymbol.isNullOrEmpty()) {
            assetList.find { it.symbol.equals(initialSymbol, ignoreCase = true)&&it.token_address.equals(initialTokenAddress,ignoreCase = true)} ?: assetList.firstOrNull()
        } else {
            assetList.firstOrNull()
        }
    }

    var selectedAsset by remember { mutableStateOf<AssetItem?>(defaultAsset) }
    var toAddress by remember { mutableStateOf(initialAddress ?: "") }
    var amount by remember { mutableStateOf(initialAmount ?: "") }
    var amountUsd by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var feeUsd by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = result.data?.getStringExtra("scan_result")
            if (!scanResult.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(scanResult)
                    val encryptedAddress = uri.getQueryParameter("toAddress")
                    val scanAmount = uri.getQueryParameter("amount")
                    val scanSymbol = uri.getQueryParameter("symbol")
                    val scanTokenAddress = uri.getQueryParameter("tokenAddress")

                    val parsedAddress = if (!encryptedAddress.isNullOrEmpty()) {
                        val fixedEncryptedAddress = encryptedAddress.replace(" ", "+")
                        try {
                            EncryptUtil.aesDecrypt(fixedEncryptedAddress, "AES")
                        } catch (e: Exception) {
                            EncryptUtil.aesDecrypt(encryptedAddress, "AES")
                        }
                    } else ""

                    if (!parsedAddress.isNullOrEmpty()) {
                        toAddress = parsedAddress
                    }
                    if (!scanAmount.isNullOrEmpty()) {
                        amount = scanAmount
                    }
                    if (!scanSymbol.isNullOrEmpty()) {
                        val matchedAsset = assetList.find { it.symbol.equals(scanSymbol, ignoreCase = true) && it.token_address.equals(scanTokenAddress,ignoreCase = true) }
                        if (matchedAsset != null) {
                            selectedAsset = matchedAsset
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastBroadUtil.show("二维码解析失败")
                }
            }
        }
    }

    LaunchedEffect(selectedAsset?.symbol, amount) {
        val asset = selectedAsset
        val amt = amount.toDoubleOrNull() ?: 0.0
        val usd = if (asset != null && amt > 0) {
            val balance = asset.balance.toDoubleOrNull() ?: 0.0
            val totalUsd = asset.value_usd?.toDoubleOrNull()
//            val price = if (balance > 0 && totalUsd != null) {
//                totalUsd / balance
//            } else {
//                asset.price_usd?.toDoubleOrNull() ?: 0.0
//            }
            val price = asset.price_usd?.toDoubleOrNull() ?: 0.0
            amt * price
        } else {
            0.0
        }
//        val usd =asset?.price_usd?.toDoubleOrNull()?:0.0
        amountUsd = if (usd > 0) "≈$${String.format("%.2f", usd)}" else ""
    }

    // Trigger Gas Estimation with Debounce
    LaunchedEffect(selectedAsset?.symbol, toAddress, amount) {
        val wallet = uiState.currentWallet
        val asset = selectedAsset
        if (wallet != null && asset != null && toAddress.isNotBlank() && amount.isNotBlank()) {
            delay(500) // Debounce 500ms
            val maxAmount= BigInteger(selectedAsset?.balance?:"0")
            val inputAmount=WalletManager.convertToWei(amount)?: BigInteger("0")
           val requestAmount= if (inputAmount>=maxAmount){
                 ""
            }else{
                inputAmount.toString()
           }
            viewModel.estimateGasFee(toAddress, requestAmount, asset.symbol,asset.token_address)
        }
    }

    // Update Fee Display based on Response
    LaunchedEffect(uiState.gasEstimateResponse) {
        val res = uiState.gasEstimateResponse
        if (res != null) {
            val raw = res.standard.estimate_fee
            val feeBnB = raw.toBigDecimalOrNull()?.let { fee ->
                // Assuming estimate_fee is already in human readable format or small unit? 
                // Based on user input example "0.000105", it looks like human readable (ETH/BNB unit)
                // If it was Wei, it would be much larger integer string.
                // The previous logic checked for decimal point.
                if (raw.contains(".")) {
                    fee
                } else {
                    if (raw.length > 12) {
                        fee.divide(BigDecimal.TEN.pow(18))
                    } else {
                        fee
                    }
                }
            }?.toDouble() ?: 0.0

            feeText = if (feeBnB > 0) String.format("%.6f", feeBnB) else "0"
            val bnbPrice = uiState.assetList.find { it.symbol.equals("BNB", true) }?.price_usd?.toDoubleOrNull() ?: 0.0
            val feeUsdVal = feeBnB * bnbPrice
            feeUsd = if (feeUsdVal > 0) "≈$${String.format("%.2f", feeUsdVal)}" else ""
        } else {
            feeText = ""
            feeUsd = ""
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            ComposeNormalScreenToolBar(
                stringResource(R.string.wt_transfer_title),
                onBack=onBack,
                backgroundColor = Color(0xFFF7F8FA)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Text(
                text = stringResource(R.string.wt_transfer_asset),
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.65f)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .noRippleClickable() { showTokenSheet = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubcomposeAsyncImage(
                        model = selectedAsset?.logo_url?:"",
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        loading = {
//                        Image(
//                            painter = painterResource(id = R.mipmap.ic_wt_bsc),
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop
//                        )
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF26A17B),
                                modifier = Modifier.size(40.dp)
                            ) {
//                            Box(contentAlignment = Alignment.Center) {
//                                Text(
//                                    symbol.take(1),
//                                    color = Color.White,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
                            }
                        },
                        error = {
//                        if (symbol == "BNB") {
//                            Image(
//                                painter = painterResource(id = R.mipmap.ic_wt_bsc_small),
//                                contentDescription = null,
//                                contentScale = ContentScale.Fit
//                            )
//                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF26A17B),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        (selectedAsset?.symbol?:"BNB").take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
//                        }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = selectedAsset?.symbol ?: "", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f,true))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "down",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(text = stringResource(R.string.wt_transfer_to_address), fontSize = 14.sp, color = Color.Black.copy(alpha = 0.65f))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = toAddress,
                        onValueChange = {
                            if (it == (uiState.currentWallet?.address ?: "0xeeeeeeee")){
                                ToastBroadUtil.show(context.getString(R.string.wt_transfer_address_no_same_tip))
                                toAddress=""
                            }else{
                                toAddress = it
                            }

                                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.wt_transfer_to_address_hint), color = Color(0x61000000)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_wt_tran_scan),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                val intent = Intent(context, ScanBroadActivity::class.java)
                                intent.putExtra("jumpSource", ScanJumpBroadEnum.ETH_TRANSFER.sourceType)
                                scanLauncher.launch(intent)
                            }
                    )
                }
            }

            Text(text = stringResource(R.string.wt_transfer_amount), fontSize = 14.sp, color = Color.Black.copy(alpha = 0.65f))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)) {
                    TextField(
                        value = amount,
                        onValueChange = {
                            if ((it.toDoubleOrNull()?:0.0 )>(getFormattedAmount(selectedAsset?.balance ?: "",selectedAsset?.decimals?:18).toDoubleOrNull()?:0.0) ){
                                amount=(getFormattedAmount(selectedAsset?.balance ?: "",selectedAsset?.decimals?:18).toDoubleOrNull()?:0.0).toString()
                            }else{
                                amount = it
                            }

                                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.wt_transfer_amount_hint), color = Color(0x61000000)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        )
                    )
                    if (amountUsd.isNotEmpty()) {
                        Text(text = amountUsd, fontSize = 12.sp, color = Color(0x61000000))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${stringResource(R.string.wt_balance)}：${getFormattedAmount(selectedAsset?.balance ?: "",selectedAsset?.decimals?:18)} ${selectedAsset?.symbol ?: ""}", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.65f))
                        Text(
                            text = stringResource(R.string.wt_max),
                            fontSize = 12.sp,
                            color = Color(0xFF3B47DD),
                            modifier = Modifier.noRippleClickable() {
                                amount = getFormattedAmount(selectedAsset?.balance ?: "",selectedAsset?.decimals?:18)
                            }
                        )
                    }
                }
            }

            Text(text = stringResource(R.string.wt_network_fee), fontSize = 14.sp, color = Color.Black.copy(alpha = 0.65f))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.wt_estimated_fee), fontSize = 14.sp, color = Color(0x61000000))
                    if (uiState.isEstimatingGas) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF3B47DD))
                    } else {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (feeText.isNotEmpty()) feeText + " BNB" else "0",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            if (feeUsd.isNotEmpty()) {
                                Text(text = feeUsd, fontSize = 12.sp, color = Color(0x61000000))
                            }
                        }
                    }
                }
            }

            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFFF7F8FA))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (toAddress.isBlank()) {
                            ToastBroadUtil.show(context.getString(R.string.wt_toast_enter_address))
                            return@Button
                        }
                        if (amount.isBlank()) {
                            ToastBroadUtil.show(context.getString(R.string.wt_toast_enter_amount))
                            return@Button
                        }
                        val symbol = selectedAsset?.symbol
                        if (symbol.isNullOrEmpty()) {
                            ToastBroadUtil.show(context.getString(R.string.wt_toast_select_asset))
                            return@Button
                        }
                        showPasswordDialog = true
                    },
                    enabled = !uiState.isEstimatingGas&&!uiState.isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B47DD),
                        disabledContainerColor = Color(0xFF3B47DD).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isEstimatingGas||uiState.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text(text = stringResource(R.string.wt_confirm_send), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.W600)
                    }
                }
            }
        }
    }

    if (showTokenSheet) {
        TokenSelectBottomSheet(
            assetList = assetList,
            selectedAsset = selectedAsset,
            onDismiss = { showTokenSheet = false },
            onSelect = { asset ->
                selectedAsset = asset
                showTokenSheet = false
            }
        )
    }

    if (showPasswordDialog) {
        WTSecurityVerification(
            showDialog = showPasswordDialog,
            onDismiss = { showPasswordDialog = false },
            onPassWordVerifySuccess = { pwd ->
                val symbol = selectedAsset?.symbol ?: ""
                viewModel.sendTransaction(
                    context,
                    toAddress = toAddress,
                    amount = amount,
                    symbol = symbol,
                    password = pwd,
                    selectedAsset?.token_address?:""
                ) { success, msg ->
                    scope.launch {
                        if (success) {
                            onConfirmSend()
                        } else {
                            ToastBroadUtil.show(msg)
                        }
                    }
                }
            },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenSelectBottomSheet(
    assetList: List<AssetItem>,
    selectedAsset: AssetItem?,
    onDismiss: () -> Unit,
    onSelect: (AssetItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onDismiss() }
                )
                Text(
                    text = stringResource(R.string.wt_select_token_title),
                    fontSize = 16.sp,
                    color = Color.Black.copy(0.9f),
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(assetList) { item ->
                    TokenSelectionItem(
                        item = item,
                        isSelected = item.symbol == selectedAsset?.symbol &&item.token_address == selectedAsset?.token_address,
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun TokenSelectionItem(
    item: AssetItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFFF2F4FF) else Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Surface(
            shape = CircleShape,
            color = if (item.symbol == "BNB") Color(0xFFF0B90B) else Color(0xFF26A17B),
            modifier = Modifier.size(40.dp)
        ) {
            SubcomposeAsyncImage(
                model = item.logo_url?:"",
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                loading = {
//                        Image(
//                            painter = painterResource(id = R.mipmap.ic_wt_bsc),
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop
//                        )
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF26A17B),
                        modifier = Modifier.size(40.dp)
                    ) {
//                            Box(contentAlignment = Alignment.Center) {
//                                Text(
//                                    symbol.take(1),
//                                    color = Color.White,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
                    }
                },
                error = {
//                        if (symbol == "BNB") {
//                            Image(
//                                painter = painterResource(id = R.mipmap.ic_wt_bsc_small),
//                                contentDescription = null,
//                                contentScale = ContentScale.Fit
//                            )
//                        } else {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF26A17B),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                item.symbol.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
//                        }
                }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            // Row 1: Symbol Price Change
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.symbol, fontSize = 16.sp, fontWeight = FontWeight.W400, color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                if ((item.price_usd?:"0").isNotEmpty()) {
                    Text(text = "$${formatSixDecimalValue(item.price_usd)}", fontSize = 12.sp, color = Color.Black.copy(0.45f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                val change = item.change_24h?:""
                if (change.isNotEmpty()) {
                    val changeColor = if (change.startsWith("-")) Color(0xFFEB534B) else Color(0xFF52C41A)
                    Text(text = formatSixDecimalChange(item.change_24h?:""), fontSize = 12.sp, color = changeColor)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Row 2: Quantity | Value
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.wt_total))
                        withStyle(SpanStyle(color = Color.Black)) {
                            append(getFormattedAmount(item.balance ?: "",item.decimals?:18))
                        }
                    },
                    fontSize = 12.sp,
                    color = Color.Black.copy(0.45f)
                )
                if ((item.value_usd?:"0").isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "|", fontSize = 12.sp, color = Color.Black.copy(0.16f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("≈$")
                            withStyle(SpanStyle(color = Color.Black)) {
                                append("${item.value_usd}")
                            }
                        },
                        fontSize = 12.sp,
                        color = Color.Black.copy(0.45f)
                    )
                }
            }
        }

        if (isSelected) {
            Icon(
                painter = painterResource(id = R.mipmap.ic_wt_select_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TransactionPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wt_password_dialog_title)) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.wt_password_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }) {
                Text(stringResource(R.string.wt_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.wt_cancel))
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun PreviewWTTransferScreen() {
    WTTransferScreen(
        initialAddress = "0x1234567890abcdef",
        initialAmount = "0.034",
        initialSymbol = "BTC",
        initialTokenAddress = "0xeeeeee",
        onBack = {},
        onConfirmSend = {}
    )
}
