package com.cboda.baselibrary.view

import android.content.Context
import android.util.AttributeSet
import com.hjq.shape.view.ShapeView

class SquareView(context: Context, attrs: AttributeSet) : ShapeView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}