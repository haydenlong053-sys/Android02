package com.base.commonlibrary.view

import android.content.Context
import android.util.AttributeSet

class SquareImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null): AppRoundImage(context,attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}