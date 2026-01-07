package com.cboda.intelligent.widge

import android.text.TextPaint
import android.text.style.UnderlineSpan

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/5/13.
 */
class NoUnderlineBroadSpan:UnderlineSpan() {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}