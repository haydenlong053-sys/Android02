package com.cboda.intelligent.bean

import java.io.Serializable

/** 群收费价格查询结果（{@code api/getGroupJoinPayPrice}）。 */
class GroupJoinPayPriceBean : Serializable {
    /** 入群费用（DUALX），展示用 */
    var priceDualx: String? = null
    var priceUsdt: String? = null
    /** 手续费率，如 0.01 */
    var feeRate: String? = null
    /** 是否开启群收费："0" / "1" */
    var openJoinPay: String? = null
    var payMode: String? = null
    var tokenSymbol: String? = null
    var scene: String? = null
}
