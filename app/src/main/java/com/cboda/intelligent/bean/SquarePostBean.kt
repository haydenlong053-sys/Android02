package com.cboda.intelligent.bean

import com.cboda.intelligent.utils.SquarePostMediaBroadHelper

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
    /** 广场：图片优先 imgs，其次 imageUrls */
    private fun squareMediaUrls(): List<String> {
        val fromImgs = imgs?.filter { it.isNotBlank() }.orEmpty()
        if (fromImgs.isNotEmpty()) return fromImgs
        return imageUrls?.filter { it.isNotBlank() }.orEmpty()
    }

    /** 朋友圈：仅使用 imageUrls */
    private fun momentMediaUrls(): List<String> {
        return imageUrls?.filter { it.isNotBlank() }.orEmpty()
    }

    /** 广场视频地址，来自 video 字段 */
    fun squareVideoUrl(): String = video.trim()

    /** 朋友圈视频地址，来自 imageUrls 中识别出的视频 */
    fun momentVideoUrl(): String {
        return momentMediaUrls()
            .firstOrNull { SquarePostMediaBroadHelper.isVideoUrl(it) }
            .orEmpty()
    }

    /** 广场展示图片（不含 video 字段，仅过滤 URL 列表中的视频后缀） */
    fun squareDisplayImages(): Array<String>? {
        val images = squareMediaUrls().filter { !SquarePostMediaBroadHelper.isVideoUrl(it) }
        if (images.isNotEmpty()) return images.toTypedArray()
        return null
    }

    /** 朋友圈展示图片，来自 imageUrls 中非视频项 */
    fun momentDisplayImages(): Array<String>? {
        val images = momentMediaUrls().filter { !SquarePostMediaBroadHelper.isVideoUrl(it) }
        if (images.isNotEmpty()) return images.toTypedArray()
        return null
    }

    fun displayImages(): Array<String>? = squareDisplayImages()

    fun videoUrl(): String = squareVideoUrl()
}
