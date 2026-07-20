package com.freddy.chat.utils

import com.freddy.im.group.GroupRole

object BroadE {
    fun wildfireUserTypeForRole(myRole: Int): String = when (myRole) {
        GroupRole.OWNER -> "1"
        GroupRole.ADMIN -> "2"
        else -> "0"
    }
}