package com.cboda.intelligent.bean

import java.io.Serializable

/**
 * 扫码/申请入群前置校验（{@code api/checkGroupJoin}）扁平响应。
 *
 * 用法1：不传 uids，校验当前登录用户。
 * 用法2：传 uids，校验邀请好友是否在群黑名单。
 *
 * 注意：需支付时服务端常返回 `success=false` + `needPay=1`，属业务态而非网络失败。
 */
class CheckGroupJoiBean : Serializable {
    var msg: String? = null
    var code: Int = 0
    var success: Boolean = false
    /** 是否允许直接加入："0" / "1" */
    var allow: String? = null
    /** 是否在黑名单："0" / "1"（单人校验） */
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

    /** 批量校验：每人结果（字段名兼容 list / results / items） */
    var list: List<CheckGroupJoinUserItem>? = null
    var results: List<CheckGroupJoinUserItem>? = null
    var items: List<CheckGroupJoinUserItem>? = null
    /** 批量校验：直接返回的黑名单 uid 列表 */
    var blacklistedUids: List<String>? = null

    fun isBlacklisted(): Boolean = isFlagOn(blacklisted)

    fun isNeedPay(): Boolean = isFlagOn(needPay) || isFlagOn(openJoinPay)

    fun isAllowJoin(): Boolean = isFlagOn(allow) && !isBlacklisted() && !isNeedPay()

    /**
     * 收集黑名单 userId。
     * @param requestedUids 本次请求传入的 uids；单人扁平 `blacklisted=1` 时用于回填。
     */
    fun collectBlacklistedUserIds(requestedUids: Collection<String> = emptyList()): Set<String> {
        val out = linkedSetOf<String>()
        blacklistedUids?.forEach { uid ->
            if (!uid.isNullOrBlank()) out.add(uid)
        }
        sequenceOf(list, results, items)
            .filterNotNull()
            .flatten()
            .forEach { item ->
                if (item.isBlacklisted()) {
                    item.resolveUserId()?.let { out.add(it) }
                }
            }
        if (isBlacklisted() && requestedUids.size == 1) {
            out.add(requestedUids.first())
        }
        return out
    }

    private fun isFlagOn(flag: String?): Boolean = flag == "1" || flag == "true"
}

/** [api/checkGroupJoin] 批量校验单项。 */
class CheckGroupJoinUserItem : Serializable {
    var userId: String? = null
    var uid: String? = null
    var blacklisted: String? = null

    fun resolveUserId(): String? =
        userId?.takeIf { it.isNotBlank() } ?: uid?.takeIf { it.isNotBlank() }

    fun isBlacklisted(): Boolean = blacklisted == "1" || blacklisted == "true"
}
