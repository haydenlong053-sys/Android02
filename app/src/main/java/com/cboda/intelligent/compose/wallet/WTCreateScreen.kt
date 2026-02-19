package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.R
import com.cboda.intelligent.viewmodel.WTViewModel
import com.cboda.intelligent.viewmodel.WalletCreationState

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import org.web3j.crypto.Bip39Wallet
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.compose.widget.ComposeNormalScreenToolBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTCreateScreen(
    onBack: () -> Unit,
    onImportClick: () -> Unit,
    onWatchClick: () -> Unit,
    onCreateSuccess: (String, String, String) -> Unit,
    viewModel: WTViewModel = viewModel()
) {
    val  mContext= LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showWatchDialog by remember { mutableStateOf(false) }
    var jumpType by remember { mutableStateOf(0) }
    val createWalletState by viewModel.createWalletState.collectAsState()
    val watchWalletState by viewModel.watchWalletState.collectAsState()
    var wtPassword by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getSafeCenterInfo()
    }

    when (val state = createWalletState) {
        is WalletCreationState.Loading -> {
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
                        Text(stringResource(R.string.wt_creating_wallet), fontSize = 16.sp)
                    }
                }
            }
        }
        is WalletCreationState.Success -> {
            LaunchedEffect(state) {
                showDialog = false
                viewModel.resetLocalState()
                onCreateSuccess(state.mnemonic, state.name, state.address)
            }
        }
        is WalletCreationState.Error -> {
            LaunchedEffect(state) {
                android.widget.Toast.makeText(mContext, state.message, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.resetLocalState()
            }
        }
        else -> {}
    }

    when (val state = watchWalletState) {
        is WalletCreationState.Loading -> {
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
                        Text(stringResource(R.string.wt_adding), fontSize = 16.sp)
                    }
                }
            }
        }
        is WalletCreationState.Success -> {
            LaunchedEffect(state) {
                showWatchDialog = false
                viewModel.resetLocalState()
                onBack()
            }
        }
        is WalletCreationState.Error -> {
            LaunchedEffect(state) {
                 android.widget.Toast.makeText(mContext, state.message, android.widget.Toast.LENGTH_SHORT).show()
                 viewModel.resetLocalState()
            }
        }
        else -> {}
    }
    WTSecurityVerification(
        showDialog = showPasswordDialog,
        onDismiss = { showPasswordDialog = false },
        onPassWordVerifySuccess = { pwd ->
            wtPassword = pwd
            when (jumpType) {
                0 -> showDialog = true
                1 -> showWatchDialog = true
                2 -> onImportClick()
            }
        },
        viewModel = viewModel
    )

    if (showDialog) {
        CreateWalletDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                viewModel.createLocalWallet(mContext,name, wtPassword) // Use default password as UI doesn't provide it
            }
        )
    }
    
    if (showWatchDialog) {
        WatchWalletDialog(
            onDismiss = { showWatchDialog = false },
            onConfirm = { address, note ->
                viewModel.addWatchWallet(address, note)
                showWatchDialog = false
            }
        )
    }

    WTCreateContent(
        onBack = onBack,
        onImportClick = {
            jumpType = 2
            showPasswordDialog = true
        },
        onWatchClick = {
//            jumpType = 1
//            showPasswordDialog = true
            showWatchDialog = true
        },
        onCreateClick = {
            jumpType = 0
            showPasswordDialog = true
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (address: String, note: String) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .padding(bottom = 20.dp), // Add extra bottom padding for safety
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                 Box(
                    modifier = Modifier.align(Alignment.CenterStart).size(24.dp).noRippleClickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }
                Text(
                    text = stringResource(R.string.wt_add_watch_address),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Address Input
            Text(
                text = stringResource(R.string.wt_watch_address),
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
             TextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text(stringResource(R.string.wt_input_address_placeholder), color = Color.Gray, fontSize = 14.sp) },
                trailingIcon = {
                    if (address.isEmpty()) {
                        Text(
                            text = stringResource(R.string.wt_paste),
                            color = Color(0xFF3B47DD),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.noRippleClickable {
                                clipboardManager.getText()?.text?.let {
                                    address = it.trim()
                                }
                            }.padding(8.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.wt_clear),
                            color = Color(0xFF3B47DD),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.noRippleClickable {
                                address = ""
                            }.padding(8.dp)
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF3B47DD)
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Network
            Text(
                text = stringResource(R.string.wt_network),
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Spacer(modifier = Modifier.height(16.dp))
            
            // Note Input
            Text(
                text = stringResource(R.string.wt_wallet_note),
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            TextField(
                value = note,
                onValueChange = { 
                    if (it.length <= 20 && !it.contains("\n")) {
                        note = it 
                    }
                },
                placeholder = { Text(stringResource(R.string.wt_input_placeholder), color = Color.Gray, fontSize = 14.sp) },
                singleLine = true,
                 colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF3B47DD)
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Button
            Surface(
                color = Color(0xFF3B47DD),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .noRippleClickable { onConfirm(address, note) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.wt_confirm), fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTCreateContent(
    onBack: () -> Unit,
    onImportClick: () -> Unit,
    onWatchClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    Scaffold(
        topBar = {
            ComposeNormalScreenToolBar(stringResource(R.string.wt_add_wallet),
                onBack=onBack,
                backgroundColor = Color(0xFFF7F8FA))

        },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // 创建钱包
            WalletOptionItem(
                iconRes = R.mipmap.ic_wt_create,
                title = stringResource(R.string.wt_create_new_wallet),
                subtitle = stringResource(R.string.wt_create_new_wallet_subtitle),
                onClick = onCreateClick,
                backgroundColor = Color.White,
                contentColor = Color.Black,
                subtitleColor = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // 导入钱包
            WalletOptionItem(
                iconRes = R.mipmap.ic_wt_import,
                title = stringResource(R.string.wt_import_wallet),
                subtitle = stringResource(R.string.wt_import_mnemonic_private_key),
                onClick = onImportClick,
                backgroundColor = Color.White,
                contentColor = Color.Black,
                subtitleColor = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 观察钱包
            WalletOptionItem(
                iconRes = R.mipmap.ic_wt_watch,
                title = stringResource(R.string.wt_add_watch_wallet),
                subtitle = stringResource(R.string.wt_track_assets_via_address),
                onClick = onWatchClick,
                backgroundColor = Color.White,
                contentColor = Color.Black,
                subtitleColor = Color.Gray
            )
        }
    }
}

@Preview
@Composable
fun PreviewWTCreateScreen() {
    WTCreateContent(
        onBack = {},
        onImportClick = {},
        onWatchClick = {},
        onCreateClick = {}
    )
}

@Preview
@Composable
fun PreviewWatchWalletDialog() {
    WatchWalletDialog(
        onDismiss = {},
        onConfirm = { _, _ -> }
    )
}

@Composable
fun WalletOptionItem(
    @androidx.annotation.DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFFF7F8FA),
    contentColor: Color = Color.Black,
    subtitleColor: Color = Color.Gray
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var walletName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.width(326.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.wt_input_wallet_note),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                TextField(
                    value = walletName,
                    onValueChange = { 
                        if (it.length <= 20 && !it.contains("\n")) {
                            walletName = it 
                        }
                    },
                    placeholder = { Text(stringResource(R.string.wt_input_limit_20), color = Color.Gray, fontSize = 14.sp) },
                    singleLine = false,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF3B47DD)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(87.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Network Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B47DD)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .noRippleClickable(onClick = onDismiss)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("取消", fontSize = 16.sp, color = Color(0xFF3B47DD))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF3B47DD),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .noRippleClickable { onConfirm(walletName) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("确定", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
