package com.cboda.intelligent.model

/**
 * 钱包相关数据模型
 */

/**
 * 创建/导入钱包请求
 */
data class CreateWalletRequest(
    val source: String, // create/import
    val chain_id: Int,
    val address: String,
    val label: String? = null,
)

/**
 * 更新钱包请求
 */
data class UpdateWalletRequest(
    val chain_id: Int,
    val address: String,
    val label: String? = null,
    val sort_order: Boolean? = null // 是否设为默认地址
)

/**
 * Mock 钱包列表项数据
 */
data class WalletItemData(
    val name: String,
    val address: String,
    val bscBalance: String,
    val usdBalance: String,
    val isSelected: Boolean,
    val isWatch: Boolean
)

/**
 * 钱包列表项
 */
data class WalletItem(
    val chain_id: Int,
    val address: String,

    val source: String,
    val label: String?,
    val sort_order: Int, // 响应中是 boolean，表示是否为默认/排序
    val backup_verified_at: String?,
)

/**
 * 钱包列表响应
 */
data class WalletListResponse(
    val items: List<WalletItem>?
)

/**
 * 钱包详情响应
 */
data class WalletDetailResponse(
    val chain_id: Int,
    val chain: String,
    val address: String,
    val address_type: String,
    val label: String?,
    val sort_order: Boolean
)

/**
 * 代币预览请求
 */
data class TokenPreviewRequest(
    val chain_id: Int,
    val token_address: String
)

/**
 * 代币元数据
 */
data class TokenMetadata(
    val chain_id: Int,
    val token_address: String,
    val symbol: String,
    val name: String,
    val decimals: Int
)

/**
 * 添加代币请求
 */
data class AddTokenRequest(
    val chain_id: Int,
    val chain: String? = null, // 代币不存在时必填
    val token_address: String
)

/**
 * 代币列表项
 */
data class TokenItem(
    val chain_id: Int,
    val chain: String,
    val token_address: String,
    val price_address: String?,
    val token_type: String, // native/erc20
    val symbol: String,
    val name: String,
    val decimals: Int,
    val min_transfer: String,
    val logo_url: String?,
    val is_hot: Boolean,
    val sort_order: Int,
    val status: Int
)

/**
 * 代币列表响应
 */
data class TokenListResponse(
    val items: List<TokenItem>?,
    val next_offset: Int,
    val has_more: Boolean
)

/**
 * 资产列表项
 */
data class AssetItem(
    val token_address: String,
    val token_type: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val balance: String,
    val price_usd: String,
    val value_usd: String,
    val change_24h: String,
    val logo_url: String?
)

/**
 * 资产列表响应
 */
data class AssetListResponse(
    val chain_id: Int,
    val address: String,
    val total_usd: String?,
    val items: List<AssetItem>?
)

/**
 * Gas 预估请求
 */
data class GasEstimateRequest(
    val chain_id: Int,
    val from_address: String,
    val to_address: String,
    val tx_type: String, // native/token/contract
    val token_address: String? = null,
    val amount: String? = null, // 最小单位
    val data: String? = null,
    val value: String? = null,
    val gas_price: String? = null
)

/**
 * Gas 详情
 */
data class GasDetail(
    val estimate_fee: String,
    val estimated_time: String,
    val max_fee: String,
    val max_fee_per_gas: String,
    val max_priority_fee_per_gas: String
)

/**
 * Gas 预估响应
 */
data class GasEstimateResponse(
    val gas_limit: String,
    val nonce: Int, // Changed to Int based on user input example "nonce": 123
    val fast: GasDetail,
    val slow: GasDetail,
    val standard: GasDetail
)

/**
 * 转账校验请求
 */
data class ValidateTxRequest(
    val chain_id: Int,
    val from_address: String,
    val to_address: String,
    val tx_type: String, // native/token/contract
    val token_address: String? = null,
    val amount: String? = null,
    val data: String? = null,
    val value: String? = null
)

/**
 * 转账校验响应
 */
data class ValidateTxResponse(
    val valid: Boolean,
    val balance_enough: Boolean,
    val reason: String?
)

/**
 * 交易广播请求
 */
data class BroadcastTxRequest(
    val request_id: String, // UUID
    val chain_id: Int,
    val raw_tx: String,
    val from_address: String,
    val to_address: String,
    val tx_type: String,
    val token_address: String? = null,
    val amount: String? = null,
    val memo: String? = null
)

/**
 * 交易广播响应
 */
data class BroadcastTxResponse(
    val business_id: String?,
    val tx_hash: String?,
    val status: String? // pending/confirmed/failed
)

/**
 * 交易状态响应
 */
data class TxStatusResponse(
    val business_id: String?,
    val tx_hash: String?,
    val status: String?,
    val block_number: Int?,
    val confirmations: Int?
)

/**
 * 账本流水项
 */
data class LedgerItem(
    val address: String,
    val amount: String,
    val block_number: Int?,
    val block_time: String?,
    val business_id: String,
    val business_type: String?,
    val chain_id: Int,
    val counterparty: String,
    val direction: String, // in/out
    val gas_fee: String?,
    val log_index: Int,
    val status: String,
    val token_address: String,
    val token_symbol: String,
    val tx_hash: String,
    val tx_type: String
)

/**
 * 账本流水响应
 */
data class LedgerListResponse(
    val items: List<LedgerItem>?
)

/**
 * 删除钱包请求
 */
data class DeleteWalletRequest(
    val chain_id: Int,
    val address: String
)

data class SortWalletRequest(
    val chain_id: Int,
    val items: List<SortItemRequest>
)

data class  SortItemRequest(
    val address:String,
    val sort_order:Int
)

/**
 * 更新钱包地址请求
 */
data class  UpdateWalletInfoRequest(
    val chain_id: Int,
    val address: String,
    val label:String

)

/**
 * 更新钱包来源类型请求
 */
data class  WalletSourceRequest(
    val chain_id: Int,
    val address: String,
    val new_source:String
)

/**
 * 代币预览响应
 */
data class TokenPreviewResponse(
    val chain_id: Int,
    val token_address: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val logo_url: String?
)

/**
 * 移除代币请求
 */
data class RemoveTokenRequest(
    val chain_id: Int,
    val token_address: String
)

/**
 * 价格批量查询请求项
 */
data class PriceQueryItem(
    val chain_id: Int,
    val token_address: String
)

/**
 * 价格批量查询请求
 */
data class BatchPriceRequest(
    val items: List<PriceQueryItem>
)

/**
 * 价格信息项
 */
data class PriceItem(
    val chain_id: Int,
    val token_address: String,
    val price_usd: String,
    val change_24h: String
)

/**
 * 价格批量查询响应
 */
data class BatchPriceResponse(
    val items: List<PriceItem>?
)
