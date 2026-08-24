package com.cboda.intelligent.bean

import java.io.Serializable

/** 群黑名单分页 data（{@code api/groupBlacklist}）。 */
class GroupBlacklistPageBean : Serializable {
    var pageNo: Int = 1
    var count: Int = 0
    var pageSize: Int = 20
    var list: List<GroupBlacklistItem>? = null
}

/** 群黑名单列表项。 */
class GroupBlacklistItem : Serializable {
    var groupId: String? = null
    var userId: String? = null
    /** 黑名单记录 id */
    var id: String? = null
    var nickname: String? = null
    var icon: String? = null
    var sex: String? = null
    var source: String? = null
    var operatorId: String? = null
    var idno: String? = null
    var createDate: String? = null
    var reason: String? = null

    fun displayName(): String = nickname?.takeIf { it.isNotBlank() } ?: userId.orEmpty()

    fun displayId(): String = idno?.takeIf { it.isNotBlank() } ?: userId.orEmpty()

    fun avatarUrl(): String = icon?.takeIf { it.isNotBlank() } ?: ""

    fun displayReason(): String = reason?.takeIf { it.isNotBlank() } ?: ""

    fun displayTime(): String = createDate?.takeIf { it.isNotBlank() } ?: ""
}
