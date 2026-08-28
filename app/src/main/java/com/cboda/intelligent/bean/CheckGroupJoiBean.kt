package com.cboda.intelligent.bean

import java.io.Serializable

/**
 * 扫码/申请入群前置校验（{@code api/checkGroupJoin}）扁平响应。
 * 注意：需支付时服务端常返回 `success=false` + `needPay=1`，属业务态而非网络失败。
 */
class CheckGroupJoiBean : Serializable {
    var msg: String? = null
    var code: Int = 0
    var success: Boolean = false
    /** 是否允许直接加入："0" / "1" */
    var allow: String? = null
    /** 是否在黑名单："0" / "1" */
    var blacklisted: String? = null
    /** 是否需付费："0" / "1" */
    var needPay: String? = null
    /** 是否开启群收费："0" / "1" */
    var openJoinPay: String? = null
    /** 是否已申请："0" / "1" */
    var applied: String? = null

    var groupId: String? = null
    var groupName: String? = null
    var groupIcon: String? = null
    var memberCount: Int = 0

    var priceDualx: String? = null
    var priceUsdt: String? = null
    var feeRate: String? = null
    var payMode: String? = null
    var tokenSymbol: String? = null
    var scene: String? = null
    var odicPriceAvailable: Boolean = false

    fun isBlacklisted(): Boolean = isFlagOn(blacklisted)

    fun isNeedPay(): Boolean = isFlagOn(needPay) || isFlagOn(openJoinPay)

    fun isAllowJoin(): Boolean = isFlagOn(allow) && !isBlacklisted() && !isNeedPay()

    private fun isFlagOn(flag: String?): Boolean = flag == "1" || flag == "true"
}
