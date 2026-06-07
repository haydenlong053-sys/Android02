package com.cboda.intelligent.bean

data class SquarePostBean(
    val id: String = "",
    val userId: String = "",
    val imgs: Array<String>? = null,
    val content: String = "",
    val type: String = "",

    val video: String = "",
    val odicStake: String = "",
    val viewCount: String = "",

    val odicStakeLabel: String = "",
    val time: String = "",

    val commentCount: String = "",
    val likeCount: String = "",
    val commentGateTypes: String = "",

    val commentCreditMin: String = "",
    val canComment: String = "",
    val name: String = "",
    val sign: String = "",
    val icon: String = "",
    val grade: String = "",

    val creditScore: String = "",
    val gradeStatus: String = "",
    val isLiang: String = "",
    val isvip: String = "",
    val odicStakeStatus: String = "",
    val registDay: String = "",
    val quoteCount: String = "",
    val quote: Duote? = null,
    val isLike: Int = 0,
    val isTop: Int = 0,
    val imageUrls: Array<String>? = null,
    val commentList: List<CommentListBean>? = null
) {
    fun displayImages(): Array<String>? {
        val fromImgs = imgs?.filter { it.isNotBlank() }.orEmpty()
        if (fromImgs.isNotEmpty()) return fromImgs.toTypedArray()
        val fromUrls = imageUrls?.filter { it.isNotBlank() }.orEmpty()
        if (fromUrls.isNotEmpty()) return fromUrls.toTypedArray()
        return null
    }
}


