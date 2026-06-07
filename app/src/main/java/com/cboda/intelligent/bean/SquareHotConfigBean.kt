package com.cboda.intelligent.bean

data class SquareHotConfigBean(
    val likeThreshold: Int = 0,
    val viewThreshold: Int = 0,
    val remainPublishCount: Int = 0,
    val dailyPublishLimit: Int = 0,
    val todayPublishCount: Int = 0,
    val totalOdicStake: String = "",
    val stakeTip: String = "",
    val publishRules: SquarePublishRules? = null,
    val userCreditScore: String = "",
    val commentCreditMin: Int = 60,
    val commentCreditMax: Int = 999,
    val creditMin: Int = 60,
    val creditMax: Int = 999,
    val publishQuotaEnabled: String = "",
    val publishQuotaTiers: List<SquarePublishQuotaTier>? = null,
    val matchedCreditMin: Int = 0,
    val matchedCreditMax: Int = 0,
    val complaintDesc: String = "",
) {
    fun odicStakeAmount(): String {
        return totalOdicStake.takeIf { it.isNotBlank() } ?: remainPublishCount.toString()
    }
}

data class SquarePublishQuotaTier(
    val id: Int = 0,
    val creditMin: Int = 0,
    val creditMax: Int = 0,
    val dailyLimit: Int = 0,
    val sortOrder: Int = 0,
    val status: Int = 0,
)

data class SquarePublishRules(
    val stakeRule: SquarePublishRuleItem? = null,
    val auditRule: SquarePublishRuleItem? = null,
)

data class SquarePublishRuleItem(
    val title: String = "",
    val content: String = "",
)
