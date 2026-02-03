package com.cboda.baselibrary.view

import android.content.Context
import android.util.AttributeSet
import com.cboda.baselibrary.R
import com.cboda.baselibrary.utils.initBold
import com.hjq.shape.view.ShapeTextView

class MyTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeTextView(context, attrs, defStyleAttr) {
    private var style: Int = 0

    init {
        attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.MyTextView).apply {
                style = getInt(R.styleable.MyTextView_tv_style, 0)
                recycle()
            }
        }
        initBold(style)
    }


}