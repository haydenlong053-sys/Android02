package com.lxkj.aiim.widge

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.lxkj.aiim.lj.utils.AppUtil

class VoiceWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D4AF37") // 圆圈颜色
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private var centerX = 0f
    private var centerY = 0f

    private val baseRadii = floatArrayOf((AppUtil.dp2px(context,56)/2).toFloat(), (AppUtil.dp2px(context,68)/2).toFloat(), (AppUtil.dp2px(context,80)/2).toFloat()) // 三个圆的基础半径
    private val scaleFactors = floatArrayOf(1f, 1f, 1f)    // 动态缩放因子
    // 三圈的透明度，取值 0~255
    private val alphas = intArrayOf(255, 90, 38) // ≈ 100%, 35%, 15%

    private var currentIndex = -1 // 当前显示的圈索引
    private var animator: ValueAnimator? = null

    private val animators = mutableListOf<ValueAnimator>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        for (i in baseRadii.indices) {
//            paint.alpha=alphas[i]
//            canvas.drawCircle(centerX, centerY, baseRadii[i] * scaleFactors[i], paint)
//        }
        if (currentIndex >= 0) {
            for (i in 0..currentIndex) {
                paint.alpha = alphas[i]
                canvas.drawCircle(centerX, centerY, baseRadii[i], paint)
            }
        }

    }

    fun startLoopAnim() {
//        // 每个圆的动画有点延迟，形成波浪感
//        baseRadii.indices.forEach { i ->
//            val animator = ValueAnimator.ofFloat(0.8f, 1.2f).apply {
//                duration = 2000
//                repeatCount = ValueAnimator.INFINITE
//                repeatMode = ValueAnimator.RESTART
//                startDelay = i * 400L  // 每个圈延迟开始
//                addUpdateListener {
//                    scaleFactors[i] = it.animatedValue as Float
//                    invalidate()
//                }
////                interpolator= LinearInterpolator()
//            }
//            animators.add(animator)
//            animator.start()
//        }
        animator = ValueAnimator.ofInt(0, baseRadii.size).apply {
            duration = 1500 // 一轮时间
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { va ->
                val value = va.animatedValue as Int
                currentIndex = if (value == baseRadii.size) -1 else value
                invalidate()
            }
            interpolator= LinearInterpolator()
            start()
        }
    }

    fun stopLoopAnim() {
//        animators.forEach { it.cancel() }
//        animators.clear()
        animator?.cancel()
        animator = null
        currentIndex = -1
        invalidate()
    }
}
