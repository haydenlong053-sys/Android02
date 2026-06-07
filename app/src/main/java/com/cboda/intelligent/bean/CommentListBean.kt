package com.cboda.intelligent.bean

data class CommentListBean(
    val id: Int = 0,
    val userId: String = "",
    val sign: String = "",
    val content: String = "",
    val name: String = "",
    val icon: String = "",
    val time: String = "",
    val grade: String = "",
    val isLiang: String = "",
    val isvip: String = "",
    val registDay: String = "",
    val parentId: String = ""
) {
    fun parentIdForReply(): String = id.takeIf { it > 0 }?.toString().orEmpty()
}