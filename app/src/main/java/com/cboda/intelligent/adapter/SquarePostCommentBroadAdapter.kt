package com.cboda.intelligent.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.CommentListBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquarePostCommentBroadAdapter :
    BaseQuickAdapter<CommentListBean, BaseViewHolder>(R.layout.item_square_post_comment) {

    private val nameColor = "#FF110F67".toColorInt()

    override fun convert(holder: BaseViewHolder, item: CommentListBean) {
        holder.getView<TextView>(R.id.squareCommentText).text = buildCommentSpannable(item)
    }

    private fun buildCommentSpannable(comment: CommentListBean): CharSequence {
        val name = comment.name.orEmpty()
        val content = comment.content.orEmpty()
        val parentId = anyToInt(comment.parentId)
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(content)) {
            return ""
        }

        val colon = "："
        val replyPrefix = context.getString(com.cboda.baselibrary.R.string.square_comment_reply)
        var replyTarget = ""
        if (parentId != 0) {
            replyTarget = data.find { it.id == parentId }?.name ?: ""
        }
        val fullText = if (replyTarget.isNotBlank()) {
            "$name$colon$replyPrefix$replyTarget$content"
        } else {
            "$name$colon$content"
        }
        val spannable = SpannableString(fullText)
        if (name.isNotEmpty()) {
            spannable.setSpan(
                ForegroundColorSpan(nameColor),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        if (replyTarget.isNotBlank()) {
            val targetStart = name.length + colon.length + replyPrefix.length
            val targetEnd = targetStart + replyTarget.length
            spannable.setSpan(
                ForegroundColorSpan(nameColor),
                targetStart,
                targetEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        return spannable
    }
}
