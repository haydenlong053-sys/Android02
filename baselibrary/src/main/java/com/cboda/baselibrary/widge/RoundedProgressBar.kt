package com.cboda.baselibrary.widge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.cboda.baselibrary.utils.anyToFloat
import com.cboda.baselibrary.utils.getAppColor

class RoundedProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var bgPaint: Paint? = null
    private var progressPaint: Paint? = null
//    private var bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.parseColor("#E0E0E0")
//        style = Paint.Style.FILL
//    }
//
//    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.parseColor("#4CAF50")
//        style = Paint.Style.FILL
//    }

    private val rectF = RectF()
    private var progress = 0f
    private val cornerRadius = 20f

    fun setProgress(value: String, maxValue: String, bgColor: Int, proColor: Int) {
        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getAppColor(context, bgColor)
            style = Paint.Style.FILL
        }
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getAppColor(context, proColor)
            style = Paint.Style.FILL
        }
        val progressFloat = anyToFloat(value) * 100f / anyToFloat(maxValue)
        progress = progressFloat.coerceIn(0f, 100f)
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bgPaint?.let {
            progressPaint?.let { it2 ->
                val width = width.toFloat()
                val height = height.toFloat()

                // 绘制背景
                rectF.set(0f, 0f, width, height)
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, it)

                // 绘制进度
                val progressWidth = width * (progress / 100f)
                rectF.set(0f, 0f, progressWidth, height)
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, it2)
            }
        }
    }
}