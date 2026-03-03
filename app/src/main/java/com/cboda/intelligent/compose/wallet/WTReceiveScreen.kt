package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.viewmodel.WTViewModel
import com.cboda.intelligent.R
import coil.compose.SubcomposeAsyncImage
import android.net.Uri
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.toLowerCase
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.compose.widget.ComposeNormalScreenToolBar
import com.cboda.intelligent.utils.AEBroadS
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.ToastBroadUtil
import java.util.Locale
import java.util.Locale.getDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTReceiveScreen(
    address: String,
    symbol: String?,
    logoUrl: String?,
    chainId: Int?,
    chain: String?,
    tokenAddress:String?="",
    onBack: () -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf<String?>(null) }
    var showSetAmountDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            ComposeNormalScreenToolBar(
                stringResource(R.string.wt_receive_title),
                onBack = onBack,
                titleColor = Color.White,
                navigationColor = Color.White,
                backgroundColor = colorResource(R.color.color_3B47DD)
            )
            
        },
        containerColor = Color(0xFF3B47DD)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Warning Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.wt_receive_warning_bsc),
                    color = Color(0xFFEB534B),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Asset Info
            Text(
                text = stringResource(R.string.wt_receive_asset_label),
                color = Color.Black.copy(alpha = 0.65f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubcomposeAsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
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
                                    symbol?:"BNB".take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
//                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${symbol ?: "BNB"})",
                    color = Color.Black.copy(0.9f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                // Generate QR Code
                val qrContent = remember(amount, address, symbol, logoUrl, chainId, chain) {
                    val encryptedAddress = AEBroadS.encryptETH(address) ?: ""
                    val finalAmount = if (amount.isNullOrEmpty()) "0" else amount
                    val finalSymbol = symbol ?: "BNB"
                    val finalLogo = logoUrl ?: ""
                    val finalChainId = chainId ?: 97
                    val finalChain = chain ?: "BSC"
                    // Format: seekWeb4://transfer?amount=0.123&toAddress=...&symbol=USDT&logo_url=...&chan_id=...&chan=...&version=1.0
                    "sw4bc://transfer?amount=$finalAmount&toAddress=$encryptedAddress&symbol=$finalSymbol&tokenAddress=$tokenAddress&version=1.0"
                }
                
                val qrBitmap = remember(qrContent) {
                    AppBroadUtils.createQRImage(qrContent, 600, 600)
                }
                
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(240.dp)
                    )
                } else {
                    Box(modifier = Modifier.size(240.dp).background(Color.Gray))
                }
                SubcomposeAsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.White, CircleShape)
                        .padding(5.dp)
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
                                    symbol?:"BNB".take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
//                        }
                    }
                )

                // Center Icon
//                SubcomposeAsyncImage(
//                    model = logoUrl,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(50.dp)
//                        .background(Color.White, CircleShape)
//                        .padding(5.dp)
//                        .clip(CircleShape),
//                    loading = {
//                         Image(
//                            painter = painterResource(id = R.mipmap.ic_wt_bsc),
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop
//                        )
//                    },
//                    error = {
//                        Image(
//                            painter = painterResource(id = R.mipmap.ic_wt_bsc),
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Amount Display
            if (!amount.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$amount ${symbol?:"BNB"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.mipmap.ic_wt_receive_close),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                            .noRippleClickable{
                                amount = null
                            }
                    )

                }
                Spacer(modifier = Modifier.height(4.dp))
                // Calculate USD value based on current asset price
                val targetSymbol = symbol ?: "BNB"
                val asset = uiState.assetList.find { it.symbol.equals(targetSymbol, ignoreCase = true) }
                val usdPrice = asset?.price_usd?.toDoubleOrNull() ?: 0.0
                val totalUsd = (amount!!.toDoubleOrNull() ?: 0.0) * usdPrice
                Text(
                    text = "≈ $${String.format("%.2f", totalUsd)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Address Box
            Surface(
                color = Color(0xFFF7F8FA),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "钱包地址",
                        fontSize = 14.sp,
                        color = Color.Black.copy(0.65f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = address,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.W400,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Copy Button
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(address))
                        ToastBroadUtil.show(context.getString(R.string.wt_address_copied))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3FAC0A)
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_wt_get_copy),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.wt_copy_address),
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Set Amount Button
                Button(
                    onClick = { showSetAmountDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B47DD))
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_wt_get_edit),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.wt_set_amount),
                        color = Color(0xFF3B47DD),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showSetAmountDialog) {
        SetAmountDialog(
            onDismiss = { showSetAmountDialog = false },
            onConfirm = { inputAmount ->
                amount = inputAmount
                showSetAmountDialog = false
            }
        )
    }
}

@Preview
@Composable
fun PreviewWTReceiveScreen() {
    WTReceiveScreen(
        address = "0x6375hdfhdkfnghdfhdfh74857dhfdhhgsggsgdsgeg64",
        symbol = "BNB",
        logoUrl = "",
        chainId = 97,
        chain = "BSC",
        onBack = {}
    )
}

@Composable
fun SetAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

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
                Text(
                    text = stringResource(R.string.wt_set_amount_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Input Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.wt_enter_amount_hint),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B47DD))
                    ) {
                        Text(stringResource(R.string.wt_cancel), color = Color(0xFF3B47DD))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onConfirm(text) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B47DD))
                    ) {
                        Text(stringResource(R.string.wt_confirm), color = Color.White)
                    }
                }
            }
        }
    }
}
