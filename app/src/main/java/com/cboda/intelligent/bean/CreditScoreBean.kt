package com.cboda.intelligent.bean

data class CreditScoreBean(
    var creditScore: String? = null,
    var gradeStatus: Boolean = false,
    var icon: String? = null,
    var isLianghao: Boolean = false,
    var isVip: Boolean = false,
    var nickname: String? = null
)