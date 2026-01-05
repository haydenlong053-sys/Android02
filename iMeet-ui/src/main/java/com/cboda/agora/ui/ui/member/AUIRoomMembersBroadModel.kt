package com.cboda.agora.ui.ui.member

data class MemberBroadInfo(
    var userId: String,
    var userName: String,
    var userAvatar: String
)

class MemberItemBroadModel(
    val user: MemberBroadInfo?,
    val micIndex: Int?
) {

    fun micString(): String? {
        if (micIndex == null) {
            return null
        }
        return if (micIndex == 0) {
            "房主"
        } else {
            "${micIndex + 1}号麦"
        }
    }
}