package com.cboda.baselibrary.bean

/**
 * POST /im/user/changePassword 响应 content。
 */
data class ChangePasswordBean(
    var success: Boolean = false,
    var message: String? = null,
    var revokedOtherSessions: Int = 0,
)
