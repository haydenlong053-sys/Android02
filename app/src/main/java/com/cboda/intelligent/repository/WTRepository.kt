package com.cboda.intelligent.repository


import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.NetworkBroadResult
import com.cboda.intelligent.model.AddTokenRequest
import com.cboda.intelligent.model.AssetListResponse
import com.cboda.intelligent.model.BatchPriceRequest
import com.cboda.intelligent.model.BatchPriceResponse
import com.cboda.intelligent.model.BroadcastTxRequest
import com.cboda.intelligent.model.BroadcastTxResponse
import com.cboda.intelligent.model.CreateWalletRequest
import com.cboda.intelligent.model.DeleteWalletRequest
import com.cboda.intelligent.model.GasEstimateRequest
import com.cboda.intelligent.model.GasEstimateResponse
import com.cboda.intelligent.model.LedgerListResponse
import com.cboda.intelligent.model.RemoveTokenRequest
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.model.SortWalletRequest
import com.cboda.intelligent.model.TokenListResponse
import com.cboda.intelligent.model.TokenPreviewRequest
import com.cboda.intelligent.model.TokenPreviewResponse
import com.cboda.intelligent.model.TxStatusResponse
import com.cboda.intelligent.model.UpdateWalletInfoRequest
import com.cboda.intelligent.model.ValidateTxRequest
import com.cboda.intelligent.model.ValidateTxResponse
import com.cboda.intelligent.model.WalletListResponse
import com.cboda.intelligent.model.WalletSourceRequest
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService

class WTRepository {
//    private  val wtBaseUrl="http://47.118.18.244:8080/"
    private  val wtBaseUrl="https://wallet.web4x.cn/"
//    private  val wtBaseUrl= UrlBroadBroadConstants.HOST+"/"

    companion object{

      const val wtDefaultChanId=97
        const val wtDefaultWalletPwd=""
        val bnbTestnetRpcs = listOf(
            "https://data-seed-prebsc-1-s1.bnbchain.org:8545",  // 官方
            "https://bsc-testnet-rpc.publicnode.com",           // 公共高可用
            "https://bsc-testnet.drpc.org"
        )

        val bnbMainnetRpcs = listOf(
            "https://bsc-dataseed.bnbchain.org",  // 官方
            "https://bsc-rpc.publicnode.com",
            "https://binance.llamarpc.com"
        )
    }

    // 示例：获取第一个可用的 Web3j
    suspend fun getWorkingWeb3j(rpcList: List<String>): Web3j? {
        for (url in rpcList) {
            try {
                val web3j = Web3j.build(HttpService(url))
                val blockNumber = web3j.ethBlockNumber().send()
                if (!blockNumber.hasError()) return web3j
            } catch (_: Exception) {}
        }
        return null
    }




    suspend fun  getSafeCenterInfo(): NetworkBroadResult<SafeCenterBroadBean> {
        val params = hashMapOf("code" to "")
        return  withContext(Dispatchers.IO){
            NetworkBroadRepository.postEncrypted(UrlBroadBroadConstants.SECURITY_CENTER, SafeCenterBroadBean::class.java,params)
        }

    }

    suspend fun getWalletList(chainId: Int): NetworkBroadResult<WalletListResponse> {
        val params = mapOf("chain_id" to chainId.toString())
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.get(UrlBroadBroadConstants.WT_WALLETS, wtBaseUrl,WalletListResponse::class.java,params)
//            NetworkBroadRepository.get(UrlBroadBroadConstants.WT_WALLETS, WalletListResponse::class.java, params)
        }
    }
    
    suspend fun createWallet(req: CreateWalletRequest): NetworkBroadResult<Any> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_WALLETS,wtBaseUrl, Any::class.java,req)
        }
    }

    suspend fun getAssets(chainId: Int, address: String, showAll: Boolean = true): NetworkBroadResult<AssetListResponse> {
        val params = mapOf(
            "chain_id" to chainId.toString(),
            "address" to address,
            "show_all" to (if (showAll) "true" else "false")
        )
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.get(UrlBroadBroadConstants.WT_ASSETS, wtBaseUrl,AssetListResponse::class.java, params)
        }
    }

    suspend fun getTokenList(chainId: Int): NetworkBroadResult<TokenListResponse> {
        val params = mapOf("chain_id" to chainId.toString())
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.get(UrlBroadBroadConstants.WT_TOKENS, TokenListResponse::class.java, params)
        }
    }

    suspend fun estimateGas(req: GasEstimateRequest): NetworkBroadResult<GasEstimateResponse> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_TX_GAS, wtBaseUrl,GasEstimateResponse::class.java, req)
        }
    }

    suspend fun validateTx(req: ValidateTxRequest): NetworkBroadResult<ValidateTxResponse> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_TX_VALIDATE, wtBaseUrl,ValidateTxResponse::class.java, req)
        }
    }

    suspend fun broadcastTx(req: BroadcastTxRequest): NetworkBroadResult<BroadcastTxResponse> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_TX_BROADCAST, wtBaseUrl,BroadcastTxResponse::class.java, req)
        }
    }
    
    suspend fun getTxStatus(chainId: Int, txHash: String): NetworkBroadResult<TxStatusResponse> {
        val params = mapOf(
            "chain_id" to chainId.toString(),
            "tx_hash" to txHash
        )
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.get(UrlBroadBroadConstants.WT_TX_STATUS, TxStatusResponse::class.java, params)
        }
    }

    suspend fun getLedger(chainId: Int, address: String, tokenAddress: String? = null, limit: Int = 20, offset: Int = 0): NetworkBroadResult<LedgerListResponse> {
        val params = mutableMapOf(
            "chain_id" to chainId.toString(),
            "address" to address,
            "limit" to limit.toString(),
            "offset" to offset.toString()
        )
        if (!tokenAddress.isNullOrEmpty()) {
            params["token_address"] = tokenAddress
        }
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.get(UrlBroadBroadConstants.WT_LEDGER, wtBaseUrl,LedgerListResponse::class.java, params)
        }
    }

    suspend fun deleteWallet(req: DeleteWalletRequest): NetworkBroadResult<Any> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_WALLETS_DELETE,wtBaseUrl, Any::class.java,req)
        }
    }

    suspend fun  sortWallet(req: SortWalletRequest): NetworkBroadResult<Any>{
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_WALLETS_SORT,wtBaseUrl, Any::class.java,req)
        }
    }

    suspend fun  updateWalletInfo(req: UpdateWalletInfoRequest): NetworkBroadResult<Any>{
        return  withContext(Dispatchers.IO){
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_WALLETS_UPDATE,wtBaseUrl, Any::class.java,req)
        }
    }

    suspend fun  updateWalletSource(req: WalletSourceRequest): NetworkBroadResult<Any>{
        return  withContext(Dispatchers.IO){
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_WALLETS_SOURCE,wtBaseUrl,Any::class.java,req)
        }
    }

    suspend fun previewToken(req: TokenPreviewRequest): NetworkBroadResult<TokenPreviewResponse> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_TOKENS_PREVIEW, wtBaseUrl,TokenPreviewResponse::class.java, req)
        }
    }

    suspend fun addToken(req: AddTokenRequest): NetworkBroadResult<Any> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_TOKENS, wtBaseUrl,Any::class.java,req)
        }
    }

    suspend fun removeToken(req: RemoveTokenRequest): NetworkBroadResult<Any> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_TOKENS_DELETE, wtBaseUrl,Any::class.java,req)
        }
    }

    suspend fun batchGetPrices(req: BatchPriceRequest): NetworkBroadResult<BatchPriceResponse> {
        return withContext(Dispatchers.IO) {
            NetworkBroadRepository.post(UrlBroadBroadConstants.WT_PRICES, BatchPriceResponse::class.java, req)
        }
    }
}
