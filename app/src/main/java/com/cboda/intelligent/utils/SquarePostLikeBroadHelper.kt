package com.cboda.intelligent.utils

import android.widget.ImageView
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.bean.SquarePostLikeBroadEvent
import org.greenrobot.eventbus.EventBus

object SquarePostLikeBroadHelper {

    fun bindLikeIcon(imageView: ImageView, isLike: Int) {
        imageView.setImageResource(
            if (isLike == 1) {
                R.mipmap.ic_square_post_like2
            } else {
                R.mipmap.ic_square_post_like
            },
        )
    }

    fun buildUpdatedPost(post: SquarePostBean, like: Boolean): SquarePostBean {
        val newCount = (anyToInt(post.likeCount) + if (like) 1 else -1).coerceAtLeast(0)
        return post.copy(
            isLike = if (like) 1 else 0,
            likeCount = newCount.toString(),
        )
    }

    fun notifyLikeChanged(postId: String, isLike: Int, likeCount: String) {
        EventBus.getDefault().post(
            SquarePostLikeBroadEvent(
                postId = postId.trim(),
                isLike = isLike,
                likeCount = likeCount,
            ),
        )
    }
}
