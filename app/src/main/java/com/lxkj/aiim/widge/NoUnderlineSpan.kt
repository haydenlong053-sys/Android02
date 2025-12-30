package com.lxkj.aiim.widge

import android.text.TextPaint
import android.text.style.UnderlineSpan

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/5/13.
 */
class NoUnderlineSpan:UnderlineSpan() {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}