package com.cboda.intelligent.bean

import java.io.Serializable

/** 扫码/申请入群前置校验（{@code api/checkGroupJoi}）。 */
class CheckGroupJoiBean : Serializable {
    /** 是否在黑名单：1 是 */
    var blacklisted: Int = 0
    /** 是否需付费入群："0" / "1" */
    var openJoinPay: String? = null

    fun isBlacklisted(): Boolean = blacklisted == 1

    fun isJoinPayRequired(fallbackOpenJoinPay: String? = null): Boolean {
        return when (openJoinPay) {
            "1" -> true
            "0" -> false
            else -> fallbackOpenJoinPay == "1"
        }
    }
}
