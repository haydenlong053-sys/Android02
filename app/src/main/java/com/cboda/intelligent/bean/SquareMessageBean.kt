package com.cboda.intelligent.bean

data class SquareMessageBean(
    val id: String = "",
    val msgType: String = "",
    val msgTip: String = "",
    val isRead: String = "0",
    val time: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val refId: String = "",
    val content: String = "",
    val fromUserId: String = "",
    val name: String = "",
    val icon: String = "",
    val isvip: String = "0",
    val isLiang: String = "0",
    val lianghao: String = "",
    val grade: String = "",
    val creditScore: String = "",
    val gradeStatus: String = "0",
    val sign: String = "",
    val idno: String = "",
) {
    fun isUnread(): Boolean = isRead != "1"

    fun isSystemMessage(): Boolean {
        return msgType.contains("system", ignoreCase = true) ||
            msgType.contains("delete", ignoreCase = true) ||
            name == SYSTEM_NAME
    }

    fun isCommentType(): Boolean = msgType.contains("comment", ignoreCase = true)

    fun displayName(): String {
        return if (isSystemMessage()) SYSTEM_NAME else name
    }

    fun displayMessage(): String {
        return when {
            isCommentType() -> {
                val prefix = msgTip.ifBlank { "评论了你" }
                if (content.isNotBlank()) "$prefix：$content" else prefix
            }
            else -> msgTip.ifBlank { content }
        }
    }

    fun displayScore(): String? {
        if (isSystemMessage()) return null
        val score = creditScore.takeIf { it.isNotBlank() && it != "0" }
            ?: grade.takeIf { gradeStatus == "1" && it.isNotBlank() && it != "0" }
        return score
    }

    companion object {
        const val SYSTEM_NAME = "系统提醒"
        const val TARGET_SQUARE = "square"
        const val TARGET_MOMENT = "moment"
    }
}
