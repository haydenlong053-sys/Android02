package com.cboda.baselibrary.view

import android.content.Context
import android.util.AttributeSet
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView

class SquareDashBroadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ShapeTextBroadView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}