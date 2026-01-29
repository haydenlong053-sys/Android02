package com.cboda.intelligent.compose.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import org.web3j.crypto.Bip39Wallet
import android.net.Uri
import androidx.compose.ui.res.stringResource
import com.cboda.intelligent.R
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

import androidx.compose.ui.graphics.Color
import com.cboda.intelligent.activity.PaySuccessBroadActivity
import com.cboda.intelligent.activity.ScanBroadActivity
import com.cboda.intelligent.compose.wallet.WTAssetScreen
import com.cboda.intelligent.compose.wallet.WTAddTokenScreen
import com.cboda.intelligent.compose.wallet.WTBackupScreen
import com.cboda.intelligent.compose.wallet.WTCreateScreen
import com.cboda.intelligent.compose.wallet.WTEmptyScreen
import com.cboda.intelligent.compose.wallet.WTExportPrivateKeyScreen
import com.cboda.intelligent.compose.wallet.WTImportScreen
import com.cboda.intelligent.compose.wallet.WTReceiveScreen
import com.cboda.intelligent.compose.wallet.WTTokenDetailScreen
import com.cboda.intelligent.compose.wallet.WTTransactionDetailScreen
import com.cboda.intelligent.compose.wallet.WTTransferProcessingScreen
import com.cboda.intelligent.compose.wallet.WTTransferScreen
import com.cboda.intelligent.compose.wallet.WTWalletDetailScreen
import com.cboda.intelligent.compose.wallet.WTWatchScreen
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.model.CreateWalletRequest
import com.cboda.intelligent.model.DeleteWalletRequest
import com.cboda.intelligent.utils.AEBroadS
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.viewmodel.WTViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun WTFlowHost(
    startDestination: String = WTRoute.CREATE_WT,
    userId: String,
    hasPassword: Boolean = true,
    onBack: (() -> Unit)? = {},
    viewModel: WTViewModel = viewModel()
) {
    val navController = rememberNavController()
    val hasWallet by viewModel.hasWallet.collectAsState()
    
    val systemUiController = rememberSystemUiController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val (statusBarColor, useDarkIcons) = remember(currentRoute) {
        when (currentRoute) {
            WTRoute.TRANSFER_WT, WTRoute.WALLET_DETAIL -> Color(0xFFF7F8FA) to true
            WTRoute.TOKEN_DETAIL, WTRoute.TRANSACTION_DETAIL, WTRoute.EXPORT_PRIVATE_KEY -> Color(0xFFF9F9F9) to true
            WTRoute.RECEIVE_WT -> Color(0xFF3B47DD) to false
            else -> Color.White to true
        }
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons
        )
    }

    // Redirect to Asset screen if wallet exists and we are at start
    LaunchedEffect(hasWallet) {
        if (hasWallet) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == WTRoute.CREATE_WT) {
                navController.navigate(WTRoute.ASSET_WT) {
                    popUpTo(WTRoute.CREATE_WT) { inclusive = true }
                }
            }
        }
    }
    
    // Handle Export Result
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = result.data?.getStringExtra("scan_result")
            if (!scanResult.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(scanResult)
                    val encryptedAddress = uri.getQueryParameter("toAddress")
                    val amount = uri.getQueryParameter("amount")
                    val symbol = uri.getQueryParameter("symbol")
                    
                    // URL encoded string needs to be decoded before AEBroadS decryption if it was encoded
                    // But here getQueryParameter usually handles basic decoding.
                    // However, AEBroadS encrypted string might contain characters that are special in URL (+, /, =)
                    // If they were not properly encoded in QR code, or if getQueryParameter decoded them,
                    // spaces might have replaced + signs.
                    
                    val toAddress = if (!encryptedAddress.isNullOrEmpty()) {
                        // Handle potential space vs + issue in base64 strings passed via URL
                        val fixedEncryptedAddress = encryptedAddress.replace(" ", "+")
                        try {
                             AEBroadS.decryptEth(fixedEncryptedAddress)
                        } catch (e: Exception) {
                            // Fallback to original if replacement failed
                             AEBroadS.decryptEth(encryptedAddress)
                        }
                    } else ""
                    
                    if (!toAddress.isNullOrEmpty()) {
                        navController.navigate(WTRoute.transferWt(toAddress, amount, symbol))
                    } else {
                        ToastBroadUtil.show("二维码解析失败: 地址无效")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastBroadUtil.show("二维码解析失败")
                }
            }
        }
    }

    LaunchedEffect(uiState.exportResult) {
        val result = uiState.exportResult
        if (result != null) {
            if (result.error != null) {
                ToastBroadUtil.show(result.error)
            } else if (result.content.isNotEmpty()) {
                if (result.type == "private_key") {
                    navController.navigate(WTRoute.exportPrivateKey(result.content))
                } else {
                    // Mnemonic
                    navController.navigate(WTRoute.viewMnemonic(result.content))
                }
            }
            viewModel.clearExportResult()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(WTRoute.CREATE_WT) {
            WTEmptyScreen(
                onBack = { 
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        onBack?.invoke() 
                    }
                },
                onImportClick = { navController.navigate(WTRoute.importWt()) },
                onWatchClick = { navController.navigate(WTRoute.WATCH_WT) },
                onCreateSuccess = { wallet, name, address ->
                    navController.navigate(WTRoute.backupMnemonic(wallet, name, address))
                }
            )
        }
        composable(WTRoute.ADD_WT) {
            WTCreateScreen(
                onBack = { navController.popBackStack() },
                onImportClick = { navController.navigate(WTRoute.importWt()) },
                onWatchClick = { navController.navigate(WTRoute.WATCH_WT) },
                onCreateSuccess = { wallet, name, address ->
                    navController.navigate(WTRoute.backupMnemonic(wallet, name, address))
                }
            )
        }
        composable(WTRoute.ASSET_WT) {
            WTAssetScreen(
                onBack = { onBack?.invoke() },
                onScanClick = {
                    val intent = Intent(context, ScanBroadActivity::class.java)
                    intent.putExtra("jumpSource", ScanJumpBroadEnum.ETH_TRANSFER.sourceType)
                    scanLauncher.launch(intent)
                },
                onTransferClick = {  
                    navController.navigate(WTRoute.transferWt(null, null, null))
                },
                onReceiveClick = { asset ->
                    val currentWallet = viewModel.uiState.value.currentWallet
                    val address = currentWallet?.address ?: ""
                    if (address.isNotEmpty()) {
                        navController.navigate(WTRoute.receiveWt(
                            address = address,
                            symbol = asset?.symbol,
                            logoUrl = asset?.logo_url,
                            chainId = currentWallet?.chain_id,
                            chain = "BSC"
                        ))
                    }
                },
                onAddWalletClick = { navController.navigate(WTRoute.ADD_WT) },
                onWalletDetailClick = { name, address, isWatch ->
                    navController.navigate(WTRoute.walletDetail(name, address, isWatch))
                },
                onTokenClick = { isWatch, symbol ->
                    navController.navigate(WTRoute.tokenDetail(isWatch, symbol))
                },
                onAddTokenClick = {
                    navController.navigate(WTRoute.ADD_TOKEN)
                },
                viewModel = viewModel
            )
        }
        composable(WTRoute.ADD_TOKEN) {
            WTAddTokenScreen(
                onBack = {
                    navController.popBackStack()
                         },
                viewModel = viewModel
            )
        }
        composable(
            route = WTRoute.TRANSFER_WT,
            arguments = listOf(
                navArgument("address") { type = NavType.StringType; defaultValue = "" },
                navArgument("amount") { type = NavType.StringType; defaultValue = "" },
                navArgument("symbol") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val amount = backStackEntry.arguments?.getString("amount") ?: ""
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            WTTransferScreen(
                initialAddress = address.ifEmpty { null },
                initialAmount = amount.ifEmpty { null },
                initialSymbol = symbol.ifEmpty { null },
                onBack = { navController.popBackStack() },
                onConfirmSend = {
                    //跳转支付成功页面
//                    navController.navigate(WTRoute.TRANSFER_PROCESSING)
                    val intent = Intent(context, PaySuccessBroadActivity::class.java)
                    scanLauncher.launch(intent)

                },
                viewModel = viewModel
            )
        }
        composable(
            route = WTRoute.IMPORT_WT,
            arguments = listOf(navArgument("expectedAddress") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val expectedAddress = backStackEntry.arguments?.getString("expectedAddress")
            WTImportScreen(
                expectedAddress = expectedAddress,
                onBack = { 
                    if (!navController.popBackStack()) {
                        onBack?.invoke()
                    }
                },
                onImportSuccess = {
                    // Navigate to Asset screen and clear back stack up to Asset
                    navController.navigate(WTRoute.ASSET_WT) {
                        popUpTo(WTRoute.ASSET_WT) { inclusive = false }
                        launchSingleTop = true
                    }
                    // If we have an expectedAddress (conversion flow), we might want to pop WalletDetailScreen too.
                    // The above popUpTo(ASSET_WT, inclusive=false) keeps ASSET_WT and removes everything above it.
                    // WalletDetailScreen is above AssetScreen, so it will be removed. This is correct.
                }
            )
        }
        composable(WTRoute.WATCH_WT) {
            WTWatchScreen(
                onBack = { 
                    if (!navController.popBackStack()) {
                        onBack?.invoke()
                    }
                },
                viewModel = viewModel
            )
        }
        composable(
            route = WTRoute.BACKUP_MNEMONIC,
            arguments = listOf(
                navArgument("mnemonic") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType },
                navArgument("filename") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mnemonic = backStackEntry.arguments?.getString("mnemonic") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val filename = backStackEntry.arguments?.getString("filename") ?: ""
            
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isCreateSuccess) {
                if (uiState.isCreateSuccess) {
                    viewModel.confirmWalletReady()
                    viewModel.resetCreateState()
                    navController.navigate(WTRoute.ASSET_WT) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }

            WTBackupScreen(
                mnemonic = mnemonic,
                onBack = { navController.popBackStack() },
                onBackupConfirmed = {
                    viewModel.createWallet(
                        CreateWalletRequest(
                            source = "create",
                            chain_id = 97,
                            address = address,
                            label = name,
                        ),
                        filename
                    )
                }
            )
        }
        composable(
            route = WTRoute.VIEW_MNEMONIC,
            arguments = listOf(
                navArgument("mnemonic") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mnemonic = backStackEntry.arguments?.getString("mnemonic") ?: ""
            WTBackupScreen(
                mnemonic = mnemonic,
                title = stringResource(R.string.wt_view_mnemonic_title),
                buttonText = stringResource(R.string.wt_finish),
                onBack = { navController.popBackStack() },
                onBackupConfirmed = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = WTRoute.EXPORT_PRIVATE_KEY,
            arguments = listOf(navArgument("privateKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val privateKey = backStackEntry.arguments?.getString("privateKey") ?: ""
            WTExportPrivateKeyScreen(
                privateKey = privateKey,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = WTRoute.WALLET_DETAIL,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType },
                navArgument("isWatch") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val isWatch = backStackEntry.arguments?.getBoolean("isWatch") ?: false

            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(uiState.closeWalletDetail) {
                if (uiState.closeWalletDetail) {
                    viewModel.resetActionState()
                    if (uiState.walletList.isEmpty()) {
                        navController.navigate(WTRoute.CREATE_WT) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            }

            WTWalletDetailScreen(
                name = name,
                address = address,
                isWatch = isWatch,
                onBack = { navController.popBackStack() },
                onRename = { newName ->
                    viewModel.updateWalletName(address, newName, 97)
                },
                onDelete = {
                    viewModel.deleteWallet(DeleteWalletRequest(chain_id = 97, address = address))
                },
                onExportMnemonic = {
                    viewModel.exportMnemonic(address)
                },
                onExportPrivateKey = {
                    viewModel.exportPrivateKey(address)
                },
                onConvert = {
                    // Navigate to Import screen with expected address
                    navController.navigate(WTRoute.importWt(address))
                }
            )
        }
        composable(
            route = WTRoute.TOKEN_DETAIL,
            arguments = listOf(
                navArgument("isWatch") { type = NavType.BoolType },
                navArgument("symbol") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isWatch = backStackEntry.arguments?.getBoolean("isWatch") ?: false
            val symbol = backStackEntry.arguments?.getString("symbol") ?: "BNB"
            WTTokenDetailScreen(
                isWatch = isWatch,
                symbol = symbol,
                onBack = { navController.popBackStack() },
                onTransactionClick = { txHash -> navController.navigate(WTRoute.transactionDetail(txHash)) },
                onReceiveClick = { asset ->
                    val currentWallet = viewModel.uiState.value.currentWallet
                    val address = currentWallet?.address ?: ""
                    if (address.isNotEmpty()) {
                        navController.navigate(WTRoute.receiveWt(
                            address = address,
                            symbol = asset.symbol,
                            logoUrl = asset.logo_url,
                            chainId = currentWallet?.chain_id,
                            chain = "BSC"
                        ))
                    }
                },
                viewModel = viewModel
            )
        }
        composable(
            route = WTRoute.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("tx_hash") { type = NavType.StringType })
        ) { backStackEntry ->
            val txHash = backStackEntry.arguments?.getString("tx_hash") ?: ""
            val uiState by viewModel.uiState.collectAsState()
            val transaction = uiState.ledgerList.find { it.tx_hash == txHash }
            
            if (transaction != null) {
                WTTransactionDetailScreen(
                    transaction = transaction,
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Should not happen if flow is correct, but handle gracefully
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
        composable(
            route = WTRoute.RECEIVE_WT,
            arguments = listOf(
                navArgument("address") { type = NavType.StringType },
                navArgument("symbol") { type = NavType.StringType; nullable = true },
                navArgument("logo_url") { type = NavType.StringType; nullable = true },
                navArgument("chain_id") { type = NavType.StringType; nullable = true },
                navArgument("chain") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val symbol = backStackEntry.arguments?.getString("symbol")
            val logoUrl = backStackEntry.arguments?.getString("logo_url")
            val chainId = backStackEntry.arguments?.getString("chain_id")?.toIntOrNull()
            val chain = backStackEntry.arguments?.getString("chain")
            WTReceiveScreen(
                address = address,
                symbol = symbol,
                logoUrl = logoUrl,
                chainId = chainId,
                chain = chain,
                onBack = { navController.popBackStack() }
            )
        }
        composable(WTRoute.TRANSFER_PROCESSING) {
            WTTransferProcessingScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate(WTRoute.ASSET_WT) {
                        popUpTo(WTRoute.ASSET_WT) { inclusive = false }
                    }
                }
            )
        }
    }
}


/**
 * 这是此模块的相关路由
 * 为了方便上架和代码检测 没有做相关词汇中文注释
 */
class WTRoute {
    companion object {
        //创建(Landing Page)
        const val CREATE_WT = "create_wt"
        //添加钱包
        const val ADD_WT = "add_wt"
        //资产首页
        const val ASSET_WT = "asset_wt"
        //导入
        const val IMPORT_WT = "import_wt?expectedAddress={expectedAddress}"
        //观察
        const val WATCH_WT = "watch_wt"
        //备份助记词
        const val BACKUP_MNEMONIC = "backup_mnemonic/{mnemonic}/{name}/{address}/{filename}"
        //查看助记词
        const val VIEW_MNEMONIC = "view_mnemonic/{mnemonic}"
        //导出私钥
        const val EXPORT_PRIVATE_KEY = "export_private_key/{privateKey}"
        //钱包详情
        const val WALLET_DETAIL = "wallet_detail/{name}/{address}/{isWatch}"
        //代币详情
        const val TOKEN_DETAIL = "token_detail/{isWatch}/{symbol}"
        //交易详情
        const val TRANSACTION_DETAIL = "transaction_detail/{tx_hash}"
        //收款
        const val RECEIVE_WT = "receive_wt/{address}?symbol={symbol}&logo_url={logo_url}&chain_id={chain_id}&chain={chain}"
        //转账
        const val TRANSFER_WT = "transfer_wt?address={address}&amount={amount}&symbol={symbol}"
        //处理中
        const val TRANSFER_PROCESSING = "transfer_processing"
        //添加代币
        const val ADD_TOKEN = "add_token"

        fun backupMnemonic(wallet: Bip39Wallet, name: String, address: String) = "backup_mnemonic/${wallet.mnemonic}/$name/$address/${wallet.filename}"
        fun viewMnemonic(mnemonic: String) = "view_mnemonic/$mnemonic"
        fun exportPrivateKey(privateKey: String) = "export_private_key/$privateKey"
        fun walletDetail(name: String, address: String, isWatch: Boolean) = "wallet_detail/$name/$address/$isWatch"
        fun tokenDetail(isWatch: Boolean, symbol: String) = "token_detail/$isWatch/$symbol"
        fun transactionDetail(txHash: String) = "transaction_detail/$txHash"
        fun receiveWt(address: String, symbol: String?, logoUrl: String?, chainId: Int?, chain: String?): String {
            val base = StringBuilder("receive_wt/$address")
            var first = true
            fun appendParam(key: String, value: String?) {
                if (!value.isNullOrEmpty()) {
                    base.append(if (first) "?" else "&")
                    base.append("$key=${Uri.encode(value)}")
                    first = false
                }
            }
            appendParam("symbol", symbol)
            appendParam("logo_url", logoUrl)
            appendParam("chain_id", chainId?.toString())
            appendParam("chain", chain)
            return base.toString()
        }
        fun importWt(expectedAddress: String? = null): String {
            return if (expectedAddress.isNullOrEmpty()) "import_wt" else "import_wt?expectedAddress=$expectedAddress"
        }
        fun transferWt(address: String?, amount: String?, symbol: String?): String {
            val base = StringBuilder("transfer_wt")
            var first = true
            fun appendParam(key: String, value: String?) {
                if (!value.isNullOrEmpty()) {
                    base.append(if (first) "?" else "&")
                    base.append("$key=${Uri.encode(value)}")
                    first = false
                }
            }
            appendParam("address", address)
            appendParam("amount", amount)
            appendParam("symbol", symbol)
            return base.toString()
        }
    }
}
