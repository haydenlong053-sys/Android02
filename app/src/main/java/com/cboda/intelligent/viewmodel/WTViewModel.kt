package com.cboda.intelligent.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.model.AddTokenRequest
import com.cboda.intelligent.model.AssetItem
import com.cboda.intelligent.model.BroadcastTxRequest
import com.cboda.intelligent.model.CreateWalletRequest
import com.cboda.intelligent.model.DeleteWalletRequest
import com.cboda.intelligent.model.GasEstimateRequest
import com.cboda.intelligent.model.GasEstimateResponse
import com.cboda.intelligent.model.LedgerItem
import com.cboda.intelligent.model.RemoveTokenRequest
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.model.SortItemRequest
import com.cboda.intelligent.model.SortWalletRequest
import com.cboda.intelligent.model.TokenPreviewRequest
import com.cboda.intelligent.model.TokenPreviewResponse
import com.cboda.intelligent.model.UpdateWalletInfoRequest
import com.cboda.intelligent.model.ValidateTxRequest
import com.cboda.intelligent.model.WalletItem
import com.cboda.intelligent.model.WalletItemData
import com.cboda.intelligent.model.WalletSourceRequest
import com.cboda.intelligent.repository.WTRepository
import com.cboda.intelligent.utils.AEBroadS
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.utils.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.CipherException
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class WTUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val walletList: List<WalletItem> = emptyList(),
    val currentWallet: WalletItem? = null,
    val isCreateSuccess: Boolean = false,
    
    // Asset related
    val totalUsd: String = "0.00",
    val assetList: List<AssetItem> = emptyList(),

    // Ledger related
    val ledgerList: List<LedgerItem> = emptyList(),

    // Token management
    val tokenPreview: TokenPreviewResponse? = null,
    val isActionSuccess: Boolean = false,
    val closeWalletDetail: Boolean = false,
    val actionError: String? = null,
    val exportResult: ExportResult? = null,
    
    // Safe Center
    val safeCenterInfo: SafeCenterBroadBean? = null,
    
    // Gas Estimation State
    val isEstimatingGas: Boolean = false,
    val gasEstimateResponse: GasEstimateResponse? = null,
    
    // Wallet Balances (Address -> Balance)
    val walletBalances: Map<String, WalletBalance> = emptyMap()
)

data class WalletBalance(
    val bscBalance: String = "0.00",
    val usdBalance: String = "0.00"
)

data class ExportResult(
    val type: String, // "private_key" or "mnemonic"
    val content: String,
    val error: String? = null
)

sealed class WalletCreationState {
    object Idle : WalletCreationState()
    object Loading : WalletCreationState()
    data class Success(val wallet: Bip39Wallet, val name: String, val address: String) : WalletCreationState()
    data class Error(val message: String) : WalletCreationState()
}

class WTViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = WTRepository()
    
    private val _uiState = MutableStateFlow(WTUiState())
    val uiState = _uiState.asStateFlow()

    // Local Wallet Creation/Import States
    private val _createWalletState = MutableStateFlow<WalletCreationState>(WalletCreationState.Idle)
    val createWalletState: StateFlow<WalletCreationState> = _createWalletState

    private val _importWalletState = MutableStateFlow<WalletCreationState>(WalletCreationState.Idle)
    val importWalletState: StateFlow<WalletCreationState> = _importWalletState

    private val _watchWalletState = MutableStateFlow<WalletCreationState>(WalletCreationState.Idle)
    val watchWalletState: StateFlow<WalletCreationState> = _watchWalletState

    private val _hasWallet = MutableStateFlow<Boolean>(false)
    val hasWallet: StateFlow<Boolean> = _hasWallet


    // Mock Wallet Data State
    private val _mockWallets = MutableStateFlow(listOf(
        WalletItemData("Wallet 8081", "0x6cb5742b...8081", "0.23", "12,400.32", true, false),
        WalletItemData("Wallet 9835", "0x6cb5742b...9835", "0.03", "2.13", false, false),
        WalletItemData("Watch Wallet", "0x6cb5742b...1234", "1.50", "120.00", false, true)
    ))
    val mockWallets: StateFlow<List<WalletItemData>> = _mockWallets

    fun updateMockWallets(wallets: List<WalletItemData>) {
        _mockWallets.value = wallets
    }

    fun selectMockWallet(walletName: String) {
        val currentList = _mockWallets.value
        val newList = currentList.map {
            it.copy(isSelected = it.name == walletName)
        }
        _mockWallets.value = newList
    }

    private fun getUserWalletDir(): File {
        val userId = MMKVBroadUtils.getUserAppToken() ?: "unknown"
        // Use noBackupFilesDir to prevent cloud backup leaks and ensure it's strictly internal
        val dir = File(getApplication<Application>().noBackupFilesDir, "wallets/$userId")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getTempWalletDir(): File {
        val dir = File(getApplication<Application>().cacheDir, "wallets_temp")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    init {
        checkWallets()
    }

    fun checkWallets() {
        viewModelScope.launch {
            val walletDir = getUserWalletDir()
            if (walletDir.exists() && walletDir.listFiles()?.isNotEmpty() == true) {
                _hasWallet.value = true
            } else {
                _hasWallet.value = false
            }
        }
    }

    // Local Wallet Creation
    fun createLocalWallet(name: String, password: String) {
        _createWalletState.value = WalletCreationState.Loading
        viewModelScope.launch {
            try {
                // Clear old temp files
                val tempDir = getTempWalletDir()
                tempDir.deleteRecursively()
                tempDir.mkdirs()
                
                // Run on IO thread
                val wallet = withContext(Dispatchers.IO) {
                    WalletManager.createWallet(WTRepository.wtDefaultWalletPwd, tempDir)
                }

                val credentials = WalletManager.getWalletAddressByMnemonic(WTRepository.wtDefaultWalletPwd, wallet.mnemonic)
                val address = credentials?.address ?: ""
                
                // 保存助记词到本地加密文件，以便后续导出
                saveEncryptedMnemonic(address, wallet.mnemonic, tempDir)
                
                _createWalletState.value = WalletCreationState.Success(wallet, name, address)
            } catch (e: Exception) {
                e.printStackTrace()
                _createWalletState.value = WalletCreationState.Error(e.message ?: "创建钱包失败")
            }
        }
    }

    fun confirmWalletReady() {
        _hasWallet.value = true
    }

    // Local Wallet Import
    fun importLocalWallet(input: String, name: String, expectedAddress: String? = null) {
        _importWalletState.value = WalletCreationState.Loading
        viewModelScope.launch {
            try {
                // Use temp dir first to avoid storing invalid/unconfirmed wallets in secure storage
                val tempDir = getTempWalletDir()
                if (!tempDir.exists()) tempDir.mkdirs()

                // 智能识别输入类型
                // 1. 助记词通常由12-24个单词组成，中间有空格
                // 2. 私钥是64位16进制字符串（可能带0x前缀），中间无空格
                val cleanInput = input.trim()
                val isMnemonic = cleanInput.contains(" ") || !cleanInput.startsWith("0x") && cleanInput.length != 64

                val walletFilename: String
                val address: String
                val mnemonic: String

                // 1. Generate Wallet Locally (Temp)
                if (isMnemonic) {
                    val wallet = withContext(Dispatchers.IO) {
                        WalletManager.importWallet(WTRepository.wtDefaultWalletPwd, input, tempDir)
                    }
                    walletFilename = wallet.filename
                    mnemonic = wallet.mnemonic
                    val credentials = WalletManager.getWalletAddressByMnemonic(WTRepository.wtDefaultWalletPwd, mnemonic)
                    address = credentials?.address ?: ""

                    // 保存助记词到本地加密文件
                    saveEncryptedMnemonic(address, mnemonic, tempDir)
                } else {
                    // Private Key Import
                    mnemonic = "" // No mnemonic for private key import
                    walletFilename = withContext(Dispatchers.IO) {
                        WalletManager.importPrivateKey(WTRepository.wtDefaultWalletPwd, input, tempDir)
                    }
                    val credentials = Credentials.create(input)
                    address = credentials.address
                }

                // Check address match if expectedAddress is provided
                if (!expectedAddress.isNullOrEmpty() && !address.equals(expectedAddress, ignoreCase = true)) {
                    _importWalletState.value = WalletCreationState.Error("导入的钱包地址与当前钱包不一致，请检查")
                    // Cleanup temp file
                    val tempFile = File(tempDir, walletFilename)
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                    return@launch
                }

                // 2. Call API to register/sync wallet
                val apiResult = if (!expectedAddress.isNullOrEmpty()) {
                    repository.updateWalletSource(
                        WalletSourceRequest(
                            chain_id = WTRepository.wtDefaultChanId,
                            address = address,
                            new_source = "import" // Assuming "import" is the correct source for transaction wallet
                        )
                    )
                } else {
                    repository.createWallet(
                        CreateWalletRequest(
                            source = "import",
                            chain_id = WTRepository.wtDefaultChanId,
                            address = address,
                            label = name
                        )
                    )
                }

                apiResult.onSuccess {
                    // 3. Move to Secure Storage (noBackupFilesDir)
                    val targetDir = getUserWalletDir()
                    val tempFile = File(tempDir, walletFilename)
                    val targetFile = File(targetDir, walletFilename)
                    
                    if (tempFile.exists()) {
                        withContext(Dispatchers.IO) {
                            tempFile.copyTo(targetFile, overwrite = true)
                            tempFile.delete()

                            // 如果存在助记词文件，也一并移动
                            try {
                                val mnemonicFilename = getMnemonicFilename(address)
                                val tempMnemonicFile = File(tempDir, mnemonicFilename)
                                if (tempMnemonicFile.exists()) {
                                    val targetMnemonicFile = File(targetDir, mnemonicFilename)
                                    tempMnemonicFile.copyTo(targetMnemonicFile, overwrite = true)
                                    tempMnemonicFile.delete()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // 4. Update State
                    val walletObj = Bip39Wallet(walletFilename, mnemonic)
                    _importWalletState.value = WalletCreationState.Success(walletObj, name, address)
                    _hasWallet.value = true
                    
                    // Refresh wallet list
                    getWalletList(WTRepository.wtDefaultChanId)
                }.onError {
                    _importWalletState.value = WalletCreationState.Error(it.message ?: "导入失败: 接口错误")
                    // Cleanup temp file on failure
                    val tempFile = File(tempDir, walletFilename)
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                }.onFailure { code, message ->
                    _importWalletState.value = WalletCreationState.Error(message ?: "导入失败: 接口错误")
                    // Cleanup temp file on failure
                    val tempFile = File(tempDir, walletFilename)
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _importWalletState.value = WalletCreationState.Error(e.message ?: "导入钱包失败，请检查助记词或私钥是否正确")
            }
        }
    }
    
    fun fetchLedger(address: String, tokenAddress: String? = null) {
        viewModelScope.launch {
            val result = repository.getLedger(WTRepository.wtDefaultChanId, address, tokenAddress)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    ledgerList = response?.items ?: emptyList()
                )
            }.onError {
                // Handle error silently or show toast
            }
        }
    }

    fun estimateGasFee(toAddress: String, amount: String, symbol: String) {
        val wallet = _uiState.value.currentWallet ?: return
        val asset = _uiState.value.assetList.find { it.symbol.equals(symbol, ignoreCase = true) } ?: return

        if ((amount.toDoubleOrNull() ?: 0.0) <= 0.0) return
        
        // Reset previous estimate
        _uiState.value = _uiState.value.copy(isEstimatingGas = true, gasEstimateResponse = null)

        viewModelScope.launch {
            try {
                val decimals = asset.decimals
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt <= 0.0) {
                     _uiState.value = _uiState.value.copy(isEstimatingGas = false)
                    return@launch
                }
                
                val base = BigDecimal.TEN.pow(decimals)
                val minimal = BigDecimal(amount).multiply(base).toBigInteger().toString()
                val isNative = asset.token_type.equals("native", true)
                val txType = "transfer"
                
                val req = GasEstimateRequest(
                    chain_id = wallet.chain_id,
                    from_address = wallet.address,
                    to_address = toAddress,
                    tx_type = txType,
                    token_address = asset.token_address,
                    amount = amount, // User input amount
                    value = if (isNative) amount else "0"
                )
                
                repository.estimateGas(req).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isEstimatingGas = false,
                        gasEstimateResponse = it
                    )
                }.onError {
                    _uiState.value = _uiState.value.copy(isEstimatingGas = false)
                    // ToastBroadUtil.show(it.message ?: "Gas预估失败") // Optional: Don't spam toast on typing
                }.onFailure { _, _ ->
                    _uiState.value = _uiState.value.copy(isEstimatingGas = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isEstimatingGas = false)
            }
        }
    }

    fun sendTransaction(toAddress: String, amount: String, symbol: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val wallet = _uiState.value.currentWallet
        val asset = _uiState.value.assetList.find { it.symbol.equals(symbol, ignoreCase = true) }
        val gasResponse = _uiState.value.gasEstimateResponse
        
        if (wallet == null || asset == null) {
            ToastBroadUtil.show("当前钱包或资产信息异常")
            onResult(false, "Wallet/Asset Error")
            return
        }
        
        if (gasResponse == null) {
            ToastBroadUtil.show("请等待Gas预估完成")
            onResult(false, "Gas Not Estimated")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Load Credentials
                val walletDir = getUserWalletDir()
                val walletFile = walletDir.listFiles()?.find { file ->
                    val cleanAddress = wallet.address.removePrefix("0x")
                    file.name.contains(cleanAddress, ignoreCase = true)
                }

                if (walletFile == null) {
                    ToastBroadUtil.show("未找到钱包文件")
                    onResult(false, "File Not Found")
                    return@launch
                }

                val credentials = try {
                    withContext(Dispatchers.IO) {
                        WalletManager.loadCredentials(WTRepository.wtDefaultWalletPwd, walletFile)
                    }
                } catch (e: Exception) {
                    onResult(false, "Password Error")
                    return@launch
                }

                val decimals = asset.decimals
                val base = BigDecimal.TEN.pow(decimals)
                val minimal = BigDecimal(amount).multiply(base).toBigInteger().toString()
                val isNative = asset.token_type.equals("native", true)
                val txType = "transfer"
                
                val validateReq = ValidateTxRequest(
                    chain_id = wallet.chain_id,
                    from_address = wallet.address,
                    to_address = toAddress,
                    tx_type = txType,
                    token_address = asset.token_address,
                    amount = amount,
                    value = if (isNative) amount else "0"
                )
                
                repository.validateTx(validateReq).onSuccess { res ->
                    if (res == null || !res.valid) {
                        ToastBroadUtil.show(res?.reason ?: "转账校验未通过")
                        onResult(false, res?.reason)
                        return@onSuccess
                    }
                    if (!res.balance_enough) {
                        ToastBroadUtil.show("余额不足")
                        onResult(false, "Balance Not Enough")
                        return@onSuccess
                    }

                    // 2. Use Cached Gas Response
                    // Use Standard Gas Settings
                    val nonce = BigInteger.valueOf(gasResponse.nonce.toLong())
                    val gasLimit = BigInteger(gasResponse.gas_limit)
                    val maxFeePerGas = BigInteger(gasResponse.standard.max_fee_per_gas)
                    val maxPriorityFeePerGas = BigInteger(gasResponse.standard.max_priority_fee_per_gas)
                    
                    val amountVal = BigInteger(amount)

                    // Note: Assuming EIP-1559 support if max_fee_per_gas is present.
                    // If Web3j version is old, we might need to fallback to legacy using maxFeePerGas as gasPrice.
                    // For this environment, we'll try to use legacy creation with gasPrice = maxFeePerGas
                    // to ensure compatibility unless we are sure about EIP-1559 factory.
                    // Using legacy transaction for broader compatibility in this context:
                    
                    val rawTransaction = if (isNative) {
                        RawTransaction.createTransaction(WTRepository.wtDefaultChanId.toLong(),nonce,gasLimit,toAddress,amountVal,"",
                            maxPriorityFeePerGas,maxFeePerGas)
//                        RawTransaction.createEtherTransaction(
//                            nonce,
//                            maxFeePerGas, // Using maxFeePerGas as gasPrice
//                            gasLimit,
//                            toAddress,
//                            amountVal
//                        )
                    } else {
                        RawTransaction.createTransaction(WTRepository.wtDefaultChanId.toLong(),nonce,gasLimit,toAddress,
                            BigInteger("0"),"",
                            maxPriorityFeePerGas,maxFeePerGas)
//                        val function = Function(
//                            "transfer",
//                            listOf(Address(toAddress), Uint256(0.toBigInteger())),
//                            emptyList()
//                        )
//                        val encodedFunction = FunctionEncoder.encode(function)
//                        RawTransaction.createTransaction(
//                            nonce,
//                            maxFeePerGas, // Using maxFeePerGas as gasPrice
//                            gasLimit,
//                            asset.token_address ?: "",
//                            encodedFunction
//                        )
                    }

                    // 3. Sign Transaction
                    val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                    var hexValue = Numeric.toHexString(signedMessage)
                    if (!hexValue.startsWith("0x")) {
                        hexValue = "0x" + hexValue
                    }

                    val broadcastReq = BroadcastTxRequest(
                        request_id = "req_" + UUID.randomUUID().toString(),
                        chain_id = wallet.chain_id,
                        raw_tx = hexValue,
                        from_address = wallet.address,
                        to_address = toAddress,
                        tx_type = txType,
                        token_address = asset.token_address,
                        amount = amount,
                        memo = null
                    )
                    
                    repository.broadcastTx(broadcastReq).onSuccess { br ->
                        if (br?.status.isNullOrEmpty()) {
                            ToastBroadUtil.show("交易已提交")
                        }
                        onResult(true, null)
                    }.onError {
                        ToastBroadUtil.show(it.message ?: "交易广播失败")
                        onResult(false, it.message)
                    }.onFailure { _, message ->
                        ToastBroadUtil.show(message ?: "交易广播失败")
                        onResult(false, message)
                    }

                }.onError {
                    ToastBroadUtil.show(it.message ?: "转账校验失败")
                    onResult(false, it.message)
                }.onFailure { _, message ->
                    ToastBroadUtil.show(message ?: "转账校验失败")
                    onResult(false, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastBroadUtil.show("发送交易失败：${e.message}")
                onResult(false, e.message)
            }
        }
    }

    // Add Watch Wallet
    fun addWatchWallet(address: String, note: String) {
        _watchWalletState.value = WalletCreationState.Loading
        viewModelScope.launch {
            repository.createWallet(CreateWalletRequest(
                source = "watch",
                chain_id = WTRepository.wtDefaultChanId,
                address = address,
                label = note
            )).onSuccess {
                // No local file needed for watch wallet
                // We use a dummy Bip39Wallet object as the success state payload requires it, 
                // but for watch wallet only address and name matter.
                _watchWalletState.value = WalletCreationState.Success(Bip39Wallet("", ""), note, address)
                _hasWallet.value = true
                getWalletList(WTRepository.wtDefaultChanId)
            }.onError {
                _watchWalletState.value = WalletCreationState.Error(it.message ?: "添加观察钱包失败")
            }.onFailure { code, message ->
                _watchWalletState.value = WalletCreationState.Error(message ?: "添加观察钱包失败")
            }
        }
    }
    
    fun resetLocalState() {
        _createWalletState.value = WalletCreationState.Idle
        _importWalletState.value = WalletCreationState.Idle
        _watchWalletState.value = WalletCreationState.Idle
    }

    fun getWalletList(chainId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getWalletList(chainId).onSuccess {
                val list = it?.items ?: emptyList()
                
                // Try to find the currently selected wallet in the new list to keep selection
                // This ensures we switch to the new instance of the same wallet object
                val previousWallet = _uiState.value.currentWallet
                val newCurrentWalletInstance = if (previousWallet != null) {
                    list.find { w -> w.address.equals(previousWallet.address, ignoreCase = true) }
                } else null
                
                // If not found (or no previous selection), fallback to default logic (sort_order or first)
                val current = newCurrentWalletInstance ?: list.find { w -> w.sort_order==0 } ?: list.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    walletList = list,
                    currentWallet = current
                )
                
                // If we have a current wallet, fetch its assets
                current?.let { wallet ->
                    getAssets(chainId, wallet.address)
                }
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun setCurrentWallet(wallet: WalletItem) {
        _uiState.value = _uiState.value.copy(currentWallet = wallet)
        // Refresh assets for the new wallet
        getAssets(wallet.chain_id, wallet.address)
    }

    // Remote Wallet Creation (API)
    fun createWallet(req: CreateWalletRequest, tempWalletFilename: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isCreateSuccess = false)
            repository.createWallet(req).onSuccess {
                
                if (tempWalletFilename != null) {
                    try {
                        val tempFile = File(getTempWalletDir(), tempWalletFilename)
                        if (tempFile.exists()) {
                            val targetDir = getUserWalletDir()
                            val targetFile = File(targetDir, tempWalletFilename)
                            tempFile.copyTo(targetFile, overwrite = true)
                            tempFile.delete() // Cleanup

                            // 如果存在助记词文件，也一并移动
                            try {
                                val mnemonicFilename = getMnemonicFilename(req.address)
                                val tempMnemonicFile = File(getTempWalletDir(), mnemonicFilename)
                                if (tempMnemonicFile.exists()) {
                                    val targetMnemonicFile = File(targetDir, mnemonicFilename)
                                    tempMnemonicFile.copyTo(targetMnemonicFile, overwrite = true)
                                    tempMnemonicFile.delete()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Even if file move fails, we might want to continue or show error?
                        // But API success is already done.
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false, isCreateSuccess = true)
                // Optionally refresh list
                getWalletList(req.chain_id)
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }
    
    fun getAssets(chainId: Int, address: String) {
        viewModelScope.launch {
            // Don't set full screen loading for asset refresh typically, or handle differently
             // _uiState.value = _uiState.value.copy(isLoading = true, error = null)
             
            repository.getAssets(chainId, address).onSuccess {
                _uiState.value = _uiState.value.copy(
                    totalUsd = it?.total_usd ?: "0.00",
                    assetList = it?.items ?: emptyList()
                )
            }.onError {
                 // specific error handling or silent fail
                 // _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    fun fetchAllWalletBalances(chainId: Int = WTRepository.wtDefaultChanId) {
        viewModelScope.launch {
            val wallets = _uiState.value.walletList
            if (wallets.isEmpty()) return@launch

            val balances = ConcurrentHashMap<String, WalletBalance>()
            
            val jobs = wallets.map { wallet ->
                launch {
                    repository.getAssets(chainId, wallet.address).onSuccess { resp ->
                        val bnb = resp?.items?.find { it.symbol == "BNB" }?.balance ?: "0.00"
                        val usd = resp?.total_usd ?: "0.00"
                        balances[wallet.address] = WalletBalance(bnb, usd)
                    }
                }
            }
            
            jobs.joinAll()
            
            _uiState.value = _uiState.value.copy(
                walletBalances = _uiState.value.walletBalances + balances
            )
        }
    }
    
    fun getLedger(chainId: Int, address: String, tokenAddress: String? = null) {
        viewModelScope.launch {
             // Optional: specific loading state for ledger
            repository.getLedger(chainId, address, tokenAddress).onSuccess {
                _uiState.value = _uiState.value.copy(
                    ledgerList = it?.items ?: emptyList()
                )
            }.onError {
                // handle error
            }
        }
    }

    fun sortWallet(wallets: List<WalletItemData>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isActionSuccess = false, actionError = null)
            val sortItems = wallets.mapIndexed { index, wallet ->
                SortItemRequest(wallet.address, index)
            }
            val req = SortWalletRequest(WTRepository.wtDefaultChanId, sortItems)
            
            repository.sortWallet(req).onSuccess {
                // Update local list order to match the sorted list
                // We need to update the full walletList based on the sorted subset (if wallets is a subset) 
                // or just replace if it's the full list.
                // Assuming wallets contains all wallets for the current chain.
                
                // Construct new list preserving other properties from original list but in new order
                val currentList = _uiState.value.walletList
                val sortedList = wallets.mapNotNull { sortedItem ->
                    currentList.find { it.address == sortedItem.address }?.copy(sort_order = sortedItem.isSelected.compareTo(false)) // Update sort_order if needed locally?
                    // Actually we should just reorder the list.
                    currentList.find { it.address == sortedItem.address }
                }
                
                // Append any wallets that might have been missing from the sorted list (safety check)
                val missingWallets = currentList.filter { old -> wallets.none { new -> new.address == old.address } }
                val finalList = sortedList + missingWallets

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isActionSuccess = true,
                    walletList = finalList
                )
                
                // Refresh list from server to ensure consistency
                getWalletList(WTRepository.wtDefaultChanId)
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, actionError = it.message)
            }
        }
    }

    fun updateWalletName(address: String, newName: String, chainId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isActionSuccess = false, actionError = null)
            val req = UpdateWalletInfoRequest(chainId, address, newName)
            repository.updateWalletInfo(req).onSuccess {
                // Update list locally
                val currentList = _uiState.value.walletList.map { 
                    if (it.address.equals(address, ignoreCase = true)) {
                        it.copy(label = newName)
                    } else {
                        it
                    }
                }
                
                // Update current wallet if it matches
                val currentWallet = _uiState.value.currentWallet
                val newCurrentWallet = if (currentWallet?.address.equals(address, ignoreCase = true)) {
                    currentWallet?.copy(label = newName)
                } else {
                    currentWallet
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    // isActionSuccess = true, // Do not set to true to avoid navigation back
                    walletList = currentList,
                    currentWallet = newCurrentWallet
                )
            }.onError {
                 _uiState.value = _uiState.value.copy(isLoading = false, actionError = it.message)
            }
        }
    }

    private fun getMnemonicFilename(address: String): String {
        return "${address.removePrefix("0x").lowercase()}.mnemonic"
    }

    private fun saveEncryptedMnemonic(address: String, mnemonic: String, directory: File) {
        try {
            val encrypted = AEBroadS.encrypt(mnemonic)
            if (encrypted != null) {
                val file = File(directory, getMnemonicFilename(address))
                file.writeText(encrypted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getStoredMnemonic(address: String): String? {
        try {
            val dir = getUserWalletDir()
            val file = File(dir, getMnemonicFilename(address))
            if (file.exists()) {
                val encrypted = file.readText()
                return AEBroadS.decrypt(encrypted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun exportPrivateKey(address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, exportResult = null)
            try {
                val dir = getUserWalletDir()
                val cleanAddress = address.removePrefix("0x").lowercase()
                val file = dir.listFiles()?.find { it.name.lowercase().contains(cleanAddress) }
                
                var privateKey = ""
                
                // 优先尝试从助记词文件恢复私钥（如果存在）
                // 这样即使 Keystore 文件损坏或丢失（但助记词文件还在），也能导出
                val storedMnemonic = getStoredMnemonic(address)
                if (!storedMnemonic.isNullOrEmpty()) {
                    val credentials = WalletManager.getWalletAddressByMnemonic(WTRepository.wtDefaultWalletPwd, storedMnemonic)
                    privateKey = credentials?.ecKeyPair?.privateKey?.toString(16) ?: ""
                }

                if (privateKey.isEmpty() && file != null && file.exists()) {
                    // Try to load with empty password (for imported wallets)
                    // TODO: If this fails, we need to prompt for password
                    val credentials = withContext(Dispatchers.IO) {
                        WalletManager.loadCredentials(WTRepository.wtDefaultWalletPwd, file)
                    }
                    privateKey = credentials.ecKeyPair.privateKey.toString(16)
                }
                
                if (privateKey.isNotEmpty()) {
                    // Add 0x prefix as requested
                    val finalPrivateKey = if (privateKey.startsWith("0x")) privateKey else "0x$privateKey"
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        exportResult = ExportResult("private_key", finalPrivateKey)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        exportResult = ExportResult("private_key", "", "未找到该钱包文件，无法导出私钥")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Check if it's a password error
                val msg = if (e.message?.contains("Invalid password") == true || e is CipherException) {
                    "导出失败：钱包受密码保护，无法直接导出"
                } else {
                    "导出私钥失败: ${e.message}"
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportResult = ExportResult("private_key", "", msg)
                )
            }
        }
    }
    
    fun exportMnemonic(address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, exportResult = null)
            
            // 1. 尝试从本地加密文件读取助记词
            val storedMnemonic = getStoredMnemonic(address)
            if (!storedMnemonic.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportResult = ExportResult("mnemonic", storedMnemonic)
                )
                return@launch
            }

            val dir = getUserWalletDir()
            val cleanAddress = address.removePrefix("0x").lowercase()
            val file = dir.listFiles()?.find { it.name.lowercase().contains(cleanAddress) }
            
            if (file == null || !file.exists()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportResult = ExportResult("mnemonic", "", "未找到该钱包文件")
                )
                return@launch
            }
            
            // 尝试读取文件以验证其有效性，但已知 Keystore 文件不包含助记词
            try {
                // 验证文件是否是有效的 Keystore 文件（这里不需要密码，只是为了确认文件格式可读）
                // 如果文件损坏，这里会抛出异常
                val fileContent = file.readText()
                if (!fileContent.contains("\"crypto\"") && !fileContent.contains("\"Crypto\"")) {
                     throw Exception("无效的钱包文件格式")
                }
                
                // 文件存在且格式正确，但 Web3j 生成的 Keystore 文件标准上不包含助记词
                // 助记词只在创建时的内存中短暂存在，若当时未备份，后续无法从 Keystore 恢复
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportResult = ExportResult("mnemonic", "", "无法导出：当前钱包文件（Keystore）仅包含私钥信息，不包含助记词。助记词仅在创建/导入时显示，请使用您的物理备份。")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportResult = ExportResult("mnemonic", "", "钱包文件读取失败：${e.message}")
                )
            }
        }
    }
    
    fun clearExportResult() {
        _uiState.value = _uiState.value.copy(exportResult = null)
    }

    fun deleteWallet(req: DeleteWalletRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isActionSuccess = false, actionError = null)
            repository.deleteWallet(req).onSuccess {
                // Delete local file if exists
                deleteLocalWallet(req.address)
                
                // Update list locally first to ensure UI reacts correctly immediately
                val newList = _uiState.value.walletList.filter { !it.address.equals(req.address, ignoreCase = true) }
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    isActionSuccess = true,
                    closeWalletDetail = true,
                    walletList = newList,
                    currentWallet = if (newList.isEmpty()) null else newList.first()
                )
                
                if (newList.isEmpty()) {
                    _hasWallet.value = false
                }
                
                getWalletList(req.chain_id) // Refresh list from server to be sure
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, actionError = it.message)
            }
        }
    }

    private fun deleteLocalWallet(address: String) {
        try {
            val dir = getUserWalletDir()
            val files = dir.listFiles()
            val cleanAddress = address.removePrefix("0x").lowercase()
            files?.forEach { file ->
                // Web3j wallet filenames contain the address (lowercase, no 0x)
                if (file.name.lowercase().contains(cleanAddress)) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun previewToken(req: TokenPreviewRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, tokenPreview = null, actionError = null)
            repository.previewToken(req).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, tokenPreview = it)
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, actionError = it.message)
            }
        }
    }

    fun addToken(req: AddTokenRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isActionSuccess = false, actionError = null)
            repository.addToken(req).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isActionSuccess = true)
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, actionError = it.message)
            }
        }
    }

    fun removeToken(req: RemoveTokenRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isActionSuccess = false, actionError = null)
            repository.removeToken(req).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isActionSuccess = true)
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, actionError = it.message)
            }
        }
    }

    fun resetActionState() {
        _uiState.value = _uiState.value.copy(isActionSuccess = false, closeWalletDetail = false, actionError = null, tokenPreview = null)
    }

    fun resetCreateState() {
        _uiState.value = _uiState.value.copy(isCreateSuccess = false)
    }

    fun checkWalletList(chainId: Int, callback: CheckWalletCallback) {
        viewModelScope.launch {
            repository.getWalletList(chainId).onSuccess {
                callback.onSuccess(it?.items.isNullOrEmpty())
            }.onError {
                it.printStackTrace()
                ToastBroadUtil.show(it.message)
                callback.onError(it.message)
            }.onFailure { code,message ->
                ToastBroadUtil.show("xxxx")

            }
        }
    }

    interface CheckWalletCallback {
        fun onSuccess(isEmpty: Boolean)
        fun onError(msg: String?)
    }

    fun getSafeCenterInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getSafeCenterInfo().onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, safeCenterInfo = it)
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }
}
