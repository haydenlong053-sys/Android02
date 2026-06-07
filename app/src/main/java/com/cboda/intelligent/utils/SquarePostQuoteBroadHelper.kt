package com.cboda.intelligent.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import com.cboda.baselibrary.R as BaseR
import com.cboda.intelligent.bean.SquarePostBean

object SquarePostQuoteBroadHelper {

    const val QUOTE_REF_TYPE_SQUARE = "square"
    const val QUOTE_REF_TYPE_MOMENT = "moment"

    private val nameColor = "#FF110F67".toColorInt()

    fun bindQuoteText(
        textView: TextView,
        name: String,
        content: String,
        imageCount: Int = 0,
    ) {
        val imagePlaceholder = textView.context.getString(BaseR.string.base_digest_img)
        val fullContent = buildString {
            append(content)
            repeat(imageCount.coerceAtLeast(0)) {
                append(' ')
                append(imagePlaceholder)
            }
        }
        textView.isGone = TextUtils.isEmpty(fullContent) && name.isBlank()
        if (fullContent.isEmpty() && name.isBlank()) {
            textView.isGone = true
            return
        }
        textView.isGone = false
        val textBefore = "$name："
        val spannable = SpannableString(textBefore + fullContent)
        if (name.isNotEmpty()) {
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(nameColor),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        textView.text = spannable
    }

    fun imageCount(post: SquarePostBean): Int {
        return post.imgs.orEmpty().count { it.isNotBlank() }
    }

    fun quoteRefTypeForSquarePost(): String = QUOTE_REF_TYPE_SQUARE
}
