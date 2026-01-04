package com.cboda.baselibrary.view

import android.content.Context
import android.util.AttributeSet

class SquareImageBroadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null): AppRoundBroadImage(context,attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}