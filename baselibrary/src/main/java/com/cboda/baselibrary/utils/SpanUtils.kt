package com.cboda.baselibrary.utils

import android.content.res.Resources
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt

object SpanUtils {

    /**
     * 让 "100分" 中的 "分" 变小并改颜色
     * @param text       完整文字，如 "100分"
     * @param smallText  要变小的部分，如 "分"
     * @param smallSizeSp 小文字大小（sp）
     * @param smallColor  小文字颜色
     */
    @JvmOverloads
    fun small(
        text: String,
        smallText: String,
        smallSizeSp: Float,
        @ColorInt smallColor: Int = Color.GRAY
    ): CharSequence {
        val spannable = SpannableString(text)
        val start = text.indexOf(smallText)
        if (start == -1) return text

        // 改大小（绝对大小，最准）
        spannable.setSpan(
            AbsoluteSizeSpan(smallSizeSp.spToPx(), false),
            start,
            start + smallText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 改颜色
        spannable.setSpan(
            ForegroundColorSpan(smallColor),
            start,
            start + smallText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }
}

// 扩展：sp 转 px（防止 dip 和 sp 混淆）
private fun Float.spToPx(): Int =
    (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()