package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cboda.intelligent.R
import com.cboda.intelligent.viewmodel.WTViewModel
import com.cboda.intelligent.viewmodel.WalletCreationState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import org.web3j.crypto.Bip39Wallet
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.utils.ToastBroadUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTEmptyScreen(
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
    val createWalletState by viewModel.createWalletState.collectAsState()
    val watchWalletState by viewModel.watchWalletState.collectAsState()
    var wtPassword by remember { mutableStateOf("") }
    var jumpType by remember { mutableStateOf(0) }

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
                ToastBroadUtil.show( state.message)
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
                viewModel.createLocalWallet(mContext,name, wtPassword)
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wallet_eth),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.size(48.dp).noRippleClickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Top Image
            Image(
                painter = painterResource(id = R.mipmap.bg_wt_empty_top),
                contentDescription = null,
                modifier = Modifier
                    .width(320.dp)
                    .height(320.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Action Buttons
            WalletActionButton(
                iconRes = R.mipmap.ic_wt_create,
                title = stringResource(R.string.wt_create_new_wallet),
                description = stringResource(R.string.wt_add_new_multi_chain_account),
                onClick = { 
                    jumpType = 0
                    showPasswordDialog = true 
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            WalletActionButton(
                iconRes = R.mipmap.ic_wt_import,
                title = stringResource(R.string.wt_import_wallet),
                description = stringResource(R.string.wt_import_mnemonic_private_key),
                onClick = {
                    jumpType = 2
                    showPasswordDialog = true
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            WalletActionButton(
                iconRes = R.mipmap.ic_wt_watch,
                title = stringResource(R.string.wt_add_watch_wallet),
                description = stringResource(R.string.wt_track_assets_via_address),
                onClick = { 
//                    jumpType = 1
//                    showPasswordDialog = true
                    showWatchDialog = true
                }
            )
        }
    }
}

@Composable
fun WalletActionButton(
    iconRes: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF7F8FA),
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Black.copy(0.35f)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF5A5A68)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWTEmptyScreen() {
    WTEmptyScreen(
        onBack = {},
        onImportClick = {},
        onWatchClick = {},
        onCreateSuccess = {_,_,_->}
    )
}
