package com.cboda.intelligent.bean

import java.io.Serializable

/** 群黑名单列表项（字段按服务端可扩展）。 */
class GroupBlacklistItem : Serializable {
    var userId: String? = null
    var id: String? = null
    var name: String? = null
}
