package com.cboda.baselibrary.common

/**
 * 信用分构成项类型（与后端 code 对应）
 */
enum class CreditScoreTypeEnum(val code: Int, val label: String) {

    /** 在线时长达标 */
    ONLINE_TIME(1, "在线时长达标"),

    /** 注册天数 */
    REGISTER_DAY(2, "注册天数"),

    /** 平台贡献 */
    CONTRIBUTION(5, "平台贡献"),

    /** 违规扣除 */
    VIOLATION(6, "违规扣除"),

    /** 系统添加 */
    SYSTEM_ADD(7, "系统添加"),

    /** 系统扣减 */
    SYSTEM_DEDUCT(8, "系统扣减"),

    /** 邀请好友 */
    INVITE_USER(9, "邀请好友"),

    /** 持有 DUALX */
    HOLD_DUALX(10, "持有DUALX"),

    /** 担保交易 */
    GUARANTEED_TRANSACTION(11, "担保交易"),

    /** 获得背书 */
    ENDORSEMENT_GAINED(12, "获得背书");


    companion object {
        /**
         * 根据服务端返回的 code 解析枚举
         */
        fun fromCode(code: Int): CreditScoreTypeEnum? =
            entries.find { it.code == code }
    }
}
