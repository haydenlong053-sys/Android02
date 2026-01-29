package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.R
import com.cboda.intelligent.viewmodel.WTViewModel
import com.cboda.intelligent.model.WalletItem
import com.cboda.intelligent.model.WalletItemData
import java.util.Collections
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

import coil.compose.SubcomposeAsyncImage
import com.cboda.intelligent.compose.getFormattedAmount
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.model.AssetItem
import com.cboda.intelligent.utils.ToastBroadUtil
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import com.cboda.intelligent.model.RemoveTokenRequest
import kotlinx.coroutines.launch


@Preview
@Composable
fun PreviewWTAssetScreen() {
    WTAssetScreen(
        onBack = {},
        onScanClick = {},
        onTransferClick = {},
        onReceiveClick = {},
        onAddWalletClick = {},
        onAddTokenClick = {},
        onWalletDetailClick = { _, _, _ -> },
        onTokenClick = { _,_ -> }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTAssetScreen(
    onBack: () -> Unit,
    onScanClick: () -> Unit,
    onTransferClick: () -> Unit,
    onReceiveClick: (AssetItem?) -> Unit,
    onAddWalletClick: () -> Unit,
    onAddTokenClick: () -> Unit,
    onWalletDetailClick: (String, String, Boolean) -> Unit,
    onTokenClick: (Boolean, String) -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getWalletList(97)
    }

    val currentWallet = uiState.currentWallet
    val totalUsd = uiState.totalUsd
    val assetList = uiState.assetList
    val walletBalances = uiState.walletBalances
    
    // Find BNB balance for display
    val bscBalance = assetList.find { it.symbol == "BNB" }?.balance ?: "0.00"
    val isWatch = currentWallet?.source == "watch"
    
    // Map real wallet list to WalletItemData for UI
    val wallets = uiState.walletList.map { wallet ->
        val isCurrent = wallet.address == currentWallet?.address
        val balance = walletBalances[wallet.address]
        WalletItemData(
            name = wallet.label ?: "Wallet",
            address = wallet.address,
            // Only current wallet has real balance fetched
            bscBalance = balance?.bscBalance ?: (if (isCurrent) bscBalance else "0.00"),
            usdBalance = balance?.usdBalance ?: (if (isCurrent) totalUsd else "0.00"),
            isSelected = isCurrent,
            isWatch = wallet.source == "watch"
        )
    }
    
    var showWalletSelect by remember { mutableStateOf(false) }
    var hideAssets by remember { mutableStateOf(false) }

    WTAssetContent(
        onBack = onBack,
        onScanClick = onScanClick,
        onTransferClick = onTransferClick,
        onReceiveClick = onReceiveClick,
        onAddWalletClick = onAddWalletClick,
        onAddTokenClick = onAddTokenClick,
        onWalletDetailClick = onWalletDetailClick,
        showWalletSelect = showWalletSelect,
        onWalletTitleClick = { showWalletSelect = true },
        onWalletSelectDismiss = { showWalletSelect = false },
        hideAssets = hideAssets,
        onToggleHideAssets = { hideAssets = !hideAssets },
        onTokenClick = { symbol -> onTokenClick(isWatch, symbol) },
        currentWallet = currentWallet,
        isWatch = isWatch,
        totalUsd = totalUsd,
        bscBalance = bscBalance,
        wallets = wallets,
        assetList = assetList,
        isLoading = uiState.isLoading,
        isSuccess = uiState.isActionSuccess,
        onWalletSelected = { selectedData ->
            // Find the real WalletItem that matches selectedData
            val selectedWallet = uiState.walletList.find { it.address == selectedData.address }
            if (selectedWallet != null) {
                viewModel.setCurrentWallet(selectedWallet)
            }
            showWalletSelect = false
        },
        onWalletsReordered = { sortedWallets ->
            viewModel.sortWallet(sortedWallets)
        },
        onFetchBalances = {
            viewModel.fetchAllWalletBalances()
        },
        onRemoveToken = { asset ->
             val chainId = currentWallet?.chain_id ?: 97
             if (!asset.token_address.isNullOrEmpty()) {
                 viewModel.removeToken(RemoveTokenRequest(chainId, asset.token_address))
             }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTAssetContent(
    onBack: () -> Unit,
    onScanClick: () -> Unit,
    onTransferClick: () -> Unit,
    onReceiveClick: (AssetItem?) -> Unit,
    onAddWalletClick: () -> Unit,
    onAddTokenClick: () -> Unit,
    onWalletDetailClick: (String, String, Boolean) -> Unit,
    showWalletSelect: Boolean,
    onWalletTitleClick: () -> Unit,
    onWalletSelectDismiss: () -> Unit,
    hideAssets: Boolean,
    onToggleHideAssets: () -> Unit,
    onTokenClick: (String) -> Unit,
    currentWallet: WalletItem?,
    isWatch: Boolean,
    totalUsd: String,
    bscBalance: String,
    wallets: List<WalletItemData>,
    assetList: List<AssetItem> = emptyList(),
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    onWalletSelected: (WalletItemData) -> Unit,
    onWalletsReordered: (List<WalletItemData>) -> Unit,
    onFetchBalances: () -> Unit,
    onRemoveToken: (AssetItem) -> Unit
) {
    val mContext = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wallet_eth),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .noRippleClickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.wt_back),
                            tint = Color.Black
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
        ) {
            // Wallet Selector & Chain Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wallet Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.noRippleClickable { onWalletTitleClick() }
                ) {
                    Text(
                        text = currentWallet?.label ?: "My wallet",
                        fontSize = 16.sp,
                        lineHeight =18.75.sp ,
                        fontWeight = FontWeight.W400,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_wt_arrow_down),
                        contentDescription = "切换钱包",
                        tint = Color.Unspecified
                    )
                    
                    if (isWatch) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = Color(0xFF3B47DD).copy(0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "观察钱包",
                                fontSize = 10.sp,
                                color = Color(0xFF3B47DD),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)
                            )
                        }
                    }
                }

                // BSC Icon
                Surface(
//                    shape = RoundedCornerShape(16.dp),
//                    color = Color(0xFFF7F8FA)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.ic_wt_bsc_small),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BSC",
                            fontSize = 14.sp,
                            lineHeight = 18.75.sp,
                            fontWeight = FontWeight.W400,
                            color = Color.Black
                        )
                    }
                }
            }

            // 资产总览部分
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.wt_total_asset_value),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // 金额与眼睛图标
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hideAssets) "****" else "$$totalUsd",
                        fontSize = 38.sp,
                        lineHeight = 44.23.sp,
                        fontWeight = FontWeight.W700,
                        color = Color(0xFF3B47DD) // Blue color
                    )
//                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .noRippleClickable(onClick = onToggleHideAssets),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (hideAssets) R.mipmap.ic_wt_eye_close else R.mipmap.ic_wt_eye_open),
                            contentDescription = "Toggle Visibility",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                // 地址栏
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.noRippleClickable { 
                        // Copy logic should be handled by caller or added here
                         val clipboard = mContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                         val clip = android.content.ClipData.newPlainText("Address", currentWallet?.address ?: "")
                         clipboard.setPrimaryClip(clip)
                        ToastBroadUtil.show(mContext.getString(R.string.wt_address_copied))
                    }
                ) {
                    Text(
                        text = currentWallet?.address?.let { 
                            if (it.length > 10) "${it.take(6)}...${it.takeLast(4)}" else it 
                        } ?: "0x...",
                        fontSize = 12.sp,
                        lineHeight = 17.38.sp,
                        color = Color.Black.copy(0.65f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_wt_detail_copy),
                        contentDescription = "Copy",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 功能按钮区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 扫一扫 (Orange)
                FunctionButton(
                    iconPainter = painterResource(id = R.mipmap.ic_wt_detail_scan),
                    text = stringResource(R.string.wt_scan),
                    color = Color(0xFFD9622C),
                    backgroundColor = Color(0xFFFCEDD8),
                    onClick = {
                        if (isWatch) {
                            ToastBroadUtil.show(mContext.getString(R.string.wt_watch_wallet_not_supported))
                        } else {
                            onScanClick()
                        }
                    }
                )

                // 收款 (Green)
                FunctionButton(
                    iconPainter = painterResource(id = R.mipmap.ic_wt_detail_get),
                    text = stringResource(R.string.wt_receive),
                    color = Color(0xFF4CA154),
                    backgroundColor = Color(0xFFE2FBE8),
                    onClick = {
                        if (isWatch) {
                            ToastBroadUtil.show(mContext.getString(R.string.wt_watch_wallet_not_supported))
                        } else {
                            onReceiveClick(assetList.firstOrNull())
                        }
                    }
                )
                
                // 转账 (Blue)
                FunctionButton(
                    iconPainter = painterResource(id = R.mipmap.ic_wt_tran),
                    text = stringResource(R.string.wt_transfer),
                    color = Color(0xFF3B47DD),
                    backgroundColor = Color(0xFFDEEAFC),
                    onClick = {
                        if (isWatch) {
                            ToastBroadUtil.show(mContext.getString(R.string.wt_watch_wallet_not_supported))
                        } else {
                            onTransferClick()
                        }
                    }
                )
            }

//            Spacer(modifier = Modifier.height(20.dp))
            Row( modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.wt_assets),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 20.27.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                Icon(
                    painter = painterResource(id = R.mipmap.ic_wt_token_add),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp).noRippleClickable(onClick = onAddTokenClick)
                )

            }
            


            // 代币列表
            TokenList(hideAssets, onTokenClick, assetList, onRemoveToken)
        }
    }

    if (showWalletSelect) {
        LaunchedEffect(Unit) {
            onFetchBalances()
        }
        WalletSelectBottomSheet(
            wallets = wallets,
            isLoading = isLoading,
            isSuccess = isSuccess,
            onDismiss = onWalletSelectDismiss,
            onAddWallet = {
                onWalletSelectDismiss()
                onAddWalletClick()
            },
            onWalletDetailClick = onWalletDetailClick,
            onWalletSelected = onWalletSelected,
            onWalletsReordered = onWalletsReordered
        )
    }
}



@Composable
fun FunctionButton(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    text: String,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .noRippleClickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.W500
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun TokenList(hideAssets: Boolean, onTokenClick: (String) -> Unit, assetList: List<AssetItem>, onRemoveToken: (AssetItem) -> Unit) {
    if (assetList.isEmpty()) {
        WTEmptyStateView()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assetList.size) { index ->
                val item = assetList[index]
                TokenItem(
                    hideAssets = hideAssets, 
                    onClick = { onTokenClick(item.symbol) }, 
                    onDelete = { onRemoveToken(item) },
                    canDelete = !item.token_address.isNullOrEmpty() && item.symbol != "BNB",
                    symbol = item.symbol,
                    name = item.name, 
                    amount = getFormattedAmount(item.balance,item.decimals),
                    value = "≈$${item.value_usd?:"0"}",
                    change = item.change_24h?:"+0.0%",
                    logoUrl = item.logo_url
                )
            }
        }
    }
}

@Composable
fun WTEmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_wt_empty_list_logo),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
//        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.wt_no_data),
            color = Color(0xFF798398),
            fontSize = 12.sp
        )
    }
}

@Composable
fun TokenItem(
    hideAssets: Boolean, 
    onClick: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean = false,
    symbol: String,
    name: String,
    amount: String,
    value: String,
    change: String = "+0.0%",
    logoUrl: String? = null
) {
    val deleteButtonWidth = 80.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val deleteButtonWidthPx = with(density) { deleteButtonWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background (Delete Button)
        if (canDelete) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red)
                    .clickable { 
                        scope.launch { 
                            offsetX.animateTo(0f) 
                            onDelete() 
                        } 
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.shanchu),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }

        // Foreground (Content)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(Color.Transparent)
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (canDelete) {
                            val newValue = (offsetX.value + delta).coerceIn(-deleteButtonWidthPx, 0f)
                            scope.launch { offsetX.snapTo(newValue) }
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX.value < -deleteButtonWidthPx / 2) {
                            scope.launch { offsetX.animateTo(-deleteButtonWidthPx, animationSpec = tween(300)) }
                        } else {
                            scope.launch { offsetX.animateTo(0f, animationSpec = tween(300)) }
                        }
                    }
                )
                .noRippleClickable {
                     if (offsetX.value < -10f) {
                         scope.launch { offsetX.animateTo(0f) }
                     } else {
                         onClick()
                     }
                }
        ) {
            Image(
                painter = painterResource(id = R.mipmap.bg_detail_asset_item),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    loading = {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_wt_bsc),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    },
                    error = {
                        if (symbol == "BNB") {
                            Image(
                                painter = painterResource(id = R.mipmap.ic_wt_bsc),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF26A17B),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        symbol.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = symbol, fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color.Black)
                    Text(text = name, fontSize = 14.sp, color = Color.Black.copy(0.65f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (hideAssets) "****" else amount, 
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.W600
                    )
                    Text(
                        text = if (hideAssets) "****" else value, 
                        fontSize = 12.sp,
                        color = Color.Black.copy(0.65f),
                        fontWeight = FontWeight.W400
                    )
                    Text(
                        text = change,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = if (change.startsWith("-")) Color(0xFFEB534B) else Color(0xFF52C41A)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectBottomSheet(
    wallets: List<WalletItemData>,
    isLoading: Boolean,
    isSuccess: Boolean,
    onDismiss: () -> Unit,
    onAddWallet: () -> Unit,
    onWalletDetailClick: (String, String, Boolean) -> Unit,
    onWalletSelected: (WalletItemData) -> Unit,
    onWalletsReordered: (List<WalletItemData>) -> Unit
) {
    var isManageMode by remember { mutableStateOf(false) }
    var localWallets by remember { mutableStateOf(wallets) }
    var isSaving by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sync localWallets with wallets when not in manage mode or initially
    LaunchedEffect(wallets) {
        if (!isManageMode) {
            localWallets = wallets
        }
    }

    // Handle saving completion
    LaunchedEffect(isLoading, isSuccess) {
        if (!isLoading && isSaving) {
            if (isSuccess) {
                isSaving = false
                isManageMode = false
                onDismiss()
            } else {
                isSaving = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(507.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp) 
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.noRippleClickable { onDismiss() }
                    )
                    Text(
                        text = "选择钱包",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500
                    )
                    if (isLoading && isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF3B47DD)
                        )
                    } else {
                        Text(
                            text = if (isManageMode) "完成" else "管理",
                            color = if (isManageMode) Color(0xFF3B47DD) else Color.Black,
                            fontSize = 14.sp,
                            lineHeight = 20.27.sp,
                            modifier = Modifier.noRippleClickable {
                                if (isManageMode) {
                                    // Clicked Done
                                    isSaving = true
                                    onWalletsReordered(localWallets)
                                } else {
                                    // Clicked Manage
                                    isManageMode = true
                                }
                            }
                        )
                    }
                }
                
                // Wallet List
                var draggingIndex by remember { mutableStateOf(-1) }
                var dragOffset by remember { mutableStateOf(0f) }
                var itemHeight by remember { mutableStateOf(0) }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = rememberLazyListState()
                ) {
                    itemsIndexed(localWallets, key = { _, item -> item.address }) { index, wallet ->
                        val isDragging = index == draggingIndex
                        val offsetOrZero = if (isDragging) dragOffset else 0f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    translationY = offsetOrZero
                                    shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                                    alpha = if (isDragging) 0.9f else 1f
                                    scaleX = if (isDragging) 1.02f else 1f
                                    scaleY = if (isDragging) 1.02f else 1f
                                }
                                .onGloballyPositioned { coordinates ->
                                    if (itemHeight == 0) itemHeight = coordinates.size.height
                                }
                                .pointerInput(isManageMode) {
                                    if (isManageMode) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { 
                                                draggingIndex = index 
                                                dragOffset = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount.y
                                                
                                                val height = itemHeight.toFloat()
                                                if (height > 0) {
                                                    // Move Down
                                                    if (dragOffset > height / 2 && index < localWallets.size - 1) {
                                                        val newList = localWallets.toMutableList()
                                                        Collections.swap(newList, index, index + 1)
                                                        localWallets = newList
                                                        draggingIndex = index + 1
                                                        dragOffset -= height
                                                    }
                                                    // Move Up
                                                    else if (dragOffset < -height / 2 && index > 0) {
                                                        val newList = localWallets.toMutableList()
                                                        Collections.swap(newList, index, index - 1)
                                                        localWallets = newList
                                                        draggingIndex = index - 1
                                                        dragOffset += height
                                                    }
                                                }
                                            },
                                            onDragEnd = { 
                                                draggingIndex = -1
                                                dragOffset = 0f
                                            },
                                            onDragCancel = { 
                                                draggingIndex = -1
                                                dragOffset = 0f
                                            }
                                        )
                                    }
                                }
                        ) {
                            WalletItemRow(
                                wallet = wallet,
                                isManageMode = isManageMode,
                                isFirst = index == 0,
                                isLast = index == localWallets.size - 1,
                                onSelect = { 
                                    if (draggingIndex == -1) {
                                        onWalletSelected(wallet)
                                        onDismiss() 
                                    }
                                },
                                onEdit = {
                                    onWalletDetailClick(wallet.name, wallet.address, wallet.isWatch)
                                },
                                onMoveUp = {
                                    if (index > 0) { 
                                        val newList = localWallets.toMutableList()
                                        Collections.swap(newList, index, index - 1)
                                        localWallets = newList
                                    }
                                },
                                onMoveDown = {
                                    if (index < localWallets.size - 1) {
                                        val newList = localWallets.toMutableList()
                                        Collections.swap(newList, index, index + 1)
                                        localWallets = newList
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Add Wallet Button - Fixed at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 12.dp, top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (!isManageMode && !isLoading) Color(0xFF3B47DD) else Color(0xffEB534B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .noRippleClickable(enabled = !isManageMode && !isLoading, onClick = onAddWallet)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.ic_wt_btn_add),
                            contentDescription = null,
                            tint = if (!isManageMode) Color.White else Color(0xffEB534B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "添加钱包",
                            color = if (!isManageMode) Color.White else Color(0xffEB534B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalletItemRow(
    wallet: WalletItemData,
    isManageMode: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(enabled = !isManageMode) { onSelect() }
            .background(if (wallet.isSelected) Color(0xFFEFF0FC) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Surface(
            shape = CircleShape,
            color = if (wallet.isWatch) Color(0xFF5B6CFF) else Color(0xFFFF9500),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val initial = if (wallet.name.isNotEmpty()) wallet.name.take(1).uppercase() else "?"
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = wallet.name, fontSize = 16.sp, fontWeight = FontWeight.W400, color = Color.Black)
                if (wallet.isWatch) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF3B47DD).copy(0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "观察钱包",
                            color = Color(0xFF3B47DD),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                
                // Address Row with Copy Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "(*${wallet.address.takeLast(4)}", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_wt_copy_blue),
                        contentDescription = "Copy",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(14.dp)
                            .noRippleClickable {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Address", wallet.address)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                            }
                    )
                    Text(text = ")", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(text = "BSC: ${wallet.bscBalance}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "余额: $${wallet.usdBalance}", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Right Side Action
        if (isManageMode) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_wt_edit),
                    contentDescription = "Edit",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(16.dp)
                        .noRippleClickable { onEdit() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                
                // Sort Icon
                Icon(
                    painter = painterResource(id = R.mipmap.ic_wt_up_down),
                    contentDescription = "Sort",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(16.dp)
                        .noRippleClickable {
                            if (isLast) {
                                onMoveUp()
                            } else {
                                onMoveDown()
                            }
                        }
                )
            }
        } else {
            if (wallet.isSelected) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_wt_select_right),
                    contentDescription = "Selected",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
