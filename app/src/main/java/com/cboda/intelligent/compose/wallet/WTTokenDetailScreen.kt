package com.cboda.intelligent.compose.wallet

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.cboda.intelligent.R
import com.cboda.intelligent.model.LedgerItem

import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.viewmodel.WTViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone



import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.cboda.intelligent.compose.formatSixDecimalValue
import com.cboda.intelligent.compose.getFormattedAmount
import com.cboda.intelligent.model.AssetItem
import com.cboda.intelligent.repository.WTRepository
import com.cboda.intelligent.utils.ToastBroadUtil
import android.content.ClipData
import com.cboda.intelligent.compose.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTTokenDetailScreen(
    isWatch: Boolean,
    symbol: String,
    tokenAddress:String,
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onReceiveClick: (AssetItem) -> Unit,
    onTransferClick: () -> Unit,
    viewModel: WTViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val selectedTab = pagerState.currentPage
    
    val uiState by viewModel.uiState.collectAsState()
    val currentWallet = uiState.currentWallet
    
    val asset = uiState.assetList.find { it.symbol.equals(symbol, ignoreCase = true) && it.token_address.equals(tokenAddress,true)}
        ?: uiState.assetList.find { it.symbol.equals("BNB", ignoreCase = true) }

    LaunchedEffect(currentWallet?.address, symbol) {
        currentWallet?.address?.let { address ->
            // If asset is found and is not native, use its contract address
            // We need to find the asset from list to get contract address
            // If assetList is empty (initial load), this might run with asset=null.
            // But we can try to fetch ledger if we know it's not BNB? 
            // Actually, without asset list, we don't know the contract address for a symbol.
            // So we rely on asset being found.
            if (asset != null) {
                val tokenAddress = asset.token_address
                viewModel.fetchLedger(address, tokenAddress)
            } else {
                 // Try to fetch wallet list if empty?
                 viewModel.getWalletList(WTRepository.wtDefaultChanId)
            }
        }
    }

    val allTransactions = uiState.ledgerList

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SubcomposeAsyncImage(
                            model = asset?.logo_url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            loading = {
                                Image(
                                    painter = painterResource(id = R.mipmap.ic_wt_bsc),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            },
                            error = {
                                if (asset?.symbol == "BNB" || asset?.symbol == null) {
                                    Image(
                                        painter = painterResource(id = R.mipmap.ic_wt_bsc),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF26A17B),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = asset?.symbol?.take(1) ?: "",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(asset?.symbol ?: stringResource(R.string.wt_token_bnb), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.wt_back), modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (isWatch) {
                            ToastBroadUtil.show( context.getString(R.string.wt_toast_unavailable))
                        } else {
                            asset?.let { onReceiveClick(it) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CD964)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    // Icon
                    Icon(painter = painterResource(id = R.mipmap.ic_wt_detail_get), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.wt_receive_title), fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        if (isWatch) {
                            ToastBroadUtil.show( context.getString(R.string.wt_toast_unavailable))
                        } else {
                            onTransferClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B68FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    // Icon
                    Icon(painter = painterResource(id = R.mipmap.ic_wt_tran), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) // Using import icon as placeholder for transfer
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.wt_transfer_title), fontSize = 16.sp)
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header Info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .background(Color(0xffECEDEF), shape = RoundedCornerShape(8.dp))
                    .noRippleClickable() {
                        val fullAddress = currentWallet?.address ?: ""
                        if (fullAddress.isNotEmpty()) {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("walletAddress", fullAddress))
                            ToastBroadUtil.show("已复制")
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 2.dp)) {
                    val displayAddress = currentWallet?.address?.let { 
                        if (it.length > 12) "${it.take(6)}...${it.takeLast(4)}" else it 
                    } ?: ""
                    Text(displayAddress, color = Color.Black.copy(0.65f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_broad_copy),
                        contentDescription = stringResource(R.string.wt_copy),
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(getFormattedAmount(asset?.balance ?: "0",asset?.decimals?:18), fontSize = 24.sp, fontWeight = FontWeight.W700, color = Color.Black.copy(0.9f))
                Text(asset?.symbol ?: "BNB", fontSize = 20.sp, color = Color.Black.copy(0.65f), fontWeight = FontWeight.W600)
                Spacer(modifier = Modifier.height(8.dp))
                Text("≈$${formatSixDecimalValue(asset?.value_usd ?: "0.00")}", fontSize = 14.sp, color = Color.Gray)
            }

            // Price Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.wt_current_price), fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$${formatSixDecimalValue(asset?.price_usd ?: "0.00")}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        val change = asset?.change_24h ?: "0"
                        val changeColor = if (change.startsWith("-")) Color.Red else Color(0xFF4CD964)
                        Text("${if (!change.startsWith("-") && change != "0") "+" else ""}${formatSixDecimalValue(change)}%", fontSize = 14.sp, color = changeColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                listOf(
                    stringResource(R.string.wt_tab_all),
                    stringResource(R.string.wt_tab_in),
                    stringResource(R.string.wt_tab_out)
                ).forEachIndexed { index, title ->
                    Column(
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .noRippleClickable() {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Normal else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF3B47DD) else Color.Black.copy(0.45f)
                        )
                        if (selectedTab == index) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(width = 24.dp, height = 2.dp)
                                    .background(Color(0xFF3B47DD), RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Transactions Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val transactions = remember(page, allTransactions) {
                    when (page) {
                        1 -> allTransactions.filter { it.direction == "in" }
                        2 -> allTransactions.filter { it.direction == "out" }
                        else -> allTransactions
                    }
                }

                if (transactions.isEmpty()) {
                    WTEmptyStateView()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(transactions.size) { index ->
                            val isFirst = index == 0
                            val isLast = index == transactions.size - 1
                            val shape = if (transactions.size == 1) {
                                RoundedCornerShape(10.dp)
                            } else if (isFirst) {
                                RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                            } else if (isLast) {
                                RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                            } else {
                                androidx.compose.ui.graphics.RectangleShape
                            }

                            TransactionItem(
                                item = transactions[index],
                                logoUrl = asset?.logo_url,
                                shape = shape,
                                showDivider = !isLast,
                                onClick = { onTransactionClick(transactions[index].tx_hash) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    item: LedgerItem,
    logoUrl: String?,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = shape,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
//            SubcomposeAsyncImage(
//                model = logoUrl,
//                contentDescription = null,
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape),
//                loading = {
//                    Image(
//                        painter = painterResource(id = R.mipmap.ic_wt_bsc),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop
//                    )
//                },
//                error = {
//                    if (item.token_symbol.equals("BNB", ignoreCase = true)) {
//                        Image(
//                            painter = painterResource(id = R.mipmap.ic_wt_bsc),
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop
//                        )
//                    } else {
//                        Surface(
//                            shape = CircleShape,
//                            color = Color(0xFF26A17B),
//                            modifier = Modifier.size(40.dp)
//                        ) {
//                            Box(contentAlignment = Alignment.Center) {
//                                Text(
//                                    text = item.token_symbol.take(1),
//                                    color = Color.White,
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize = 18.sp
//                                )
//                            }
//                        }
//                    }
//                }
//            )
                Image(
                    painter = if (item.direction == "in") painterResource(id = R.mipmap.ic_wt_detail_in) else painterResource(id = R.mipmap.ic_wt_detail_out)  ,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val displayHash = item.tx_hash.let { if (it.length > 10) "${it.take(3)}...${it.takeLast(4)}" else it }
                        Text(
                            text = displayHash,
                            fontWeight = FontWeight.W400,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.mipmap.ic_broad_copy),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Text(
                        text = if (item.direction == "in") stringResource(R.string.wt_received_format, item.token_symbol) else stringResource(R.string.wt_sent_format, item.token_symbol),
                        fontSize = 12.sp,
                        color = Color.Black.copy(0.45f), fontWeight = FontWeight.W400
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    // Convert amount from Wei
                    val amount = getFormattedAmount(item.amount,18)
                    Text(
                        text = "${if (item.direction == "in") "+" else "-"}$amount ${item.token_symbol}",
                        color = if (item.direction == "in")  Color(0xFF3B47DD) else  Color(0xFF3FAC0A),
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                        lineHeight = 18.75.sp
                    )
                    
                    // Format Date
                    val dateStr = item.block_time?.let { time ->
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                            val date = inputFormat.parse(time)
                            val outputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                            date?.let { outputFormat.format(it) }
                        } catch (e: Exception) {
                            null
                        }
                    } ?: ""
                    
                    Text(dateStr, fontSize = 12.sp, color = Color.Black.copy(0.45f), fontWeight = FontWeight.W400)
                }
            }
            if (showDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black.copy(alpha = 0.12f)
                )
            }
        }
    }
}
