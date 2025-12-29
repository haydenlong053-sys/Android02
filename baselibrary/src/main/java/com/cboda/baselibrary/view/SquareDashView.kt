package com.cboda.baselibrary.view

import android.content.Context
import android.util.AttributeSet
import com.cboda.baselibrary.shapeview.shape.ShapeTextView

class SquareDashView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ShapeTextView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}