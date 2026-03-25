package com.cboda.baselibrary.widge

import com.blankj.utilcode.util.SizeUtils.dp2px
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.cboda.baselibrary.R
import kotlin.math.max

/**
 * 数字滚动：仅在**起始值 → 目标值**之间做一次向上滚动（旧文案上移，新文案从下方进入），
 * 中间不会出现其它整数（例如 0→6 时不会闪过 1～5）。
 */
class CountPillView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var targetNumber: Int = 0
    /** 静止时显示的数值 */
    private var currentNumber: Int = 0
    private var suffixText: String = "人"
    private var animationDuration: Long = 500L

    /** 当前这一次动画的起止（仅两行文案参与滚动） */
    private var transitionFrom: Int = 0
    private var transitionTo: Int = 0

    /** 单次过渡进度 0f～1f，对应从 [transitionFrom] 滚到 [transitionTo] */
    private var transitionProgress: Float = 1f

    private val backgroundPaint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var valueAnimator: ValueAnimator? = null
    private var cornerRadius: Float = 20f

    private val clipPath = Path()
    private val roundRect = RectF()

    private val padH = dp2px(16f)
    private val padV = dp2px(6f)

    /** 两行滚动文案之间的额外垂直间距（像素），越大上下离得越远；也可在布局里设 `app:rollLineGapExtra` */
    private var scrollLineGapExtraPx: Float = 0f

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CountPillView)
        backgroundPaint.color = ta.getColor(R.styleable.CountPillView_pillBgColor, Color.TRANSPARENT)
        val textColor = ta.getColor(R.styleable.CountPillView_suffixTextColor, Color.WHITE)
        val textSize = ta.getDimension(R.styleable.CountPillView_suffixTextSize, dp2px(14f).toFloat())
        cornerRadius = ta.getDimension(R.styleable.CountPillView_cornerRadius, dp2px(0f).toFloat())
        scrollLineGapExtraPx = ta.getDimension(R.styleable.CountPillView_rollLineGapExtra, dp2px(10f).toFloat())
        suffixText = ta.getString(R.styleable.CountPillView_suffixText) ?: ""
        textPaint.textSize = textSize
        textPaint.color = textColor

        ta.recycle()
    }

    fun setTarget(number: Int, mDuration: Long = 500L) {
        val n = max(0, number)
        if (number != currentNumber) {
            cancelAnimation()
            transitionFrom = currentNumber
            transitionTo = n
            transitionProgress = 0f
            targetNumber = n
            animationDuration = mDuration
            requestLayout()

            valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animationDuration
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    transitionProgress = it.animatedValue as Float
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentNumber = targetNumber
                        transitionFrom = currentNumber
                        transitionTo = currentNumber
                        transitionProgress = 1f
                        invalidate()
                    }
                })
                start()
            }
//        } else {
//            cancelAnimation()
//            currentNumber = n
//            targetNumber = n
//            transitionFrom = n
//            transitionTo = n
//            transitionProgress = 1f
//            requestLayout()
//            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sampleNum = maxOf(currentNumber, targetNumber, transitionFrom, transitionTo, 999)
        val sample = "${sampleNum}$suffixText"
        val textW = textPaint.measureText(sample)
        val fm = textPaint.fontMetrics
        val lineHeight = fm.descent - fm.ascent
        // 滚动时两行错位约 lineHeight+gap，预留高度避免裁切
        val minW = (textW + padH * 2).toInt().coerceAtLeast((dp2px(48f)))
        val minH = (lineHeight * 2 + scrollLineGapExtraPx + padV * 2).toInt().coerceAtLeast((dp2px(32f)))
        setMeasuredDimension(
            resolveSize(minW, widthMeasureSpec),
            resolveSize(minH, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        val h = height.toFloat()
        val w = width.toFloat()

        roundRect.set(0f, 0f, w, h)
        backgroundPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(roundRect, cornerRadius, cornerRadius, backgroundPaint)

        clipPath.reset()
        clipPath.addRoundRect(roundRect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)

        val fm = textPaint.fontMetrics
        val lineHeight = fm.descent - fm.ascent
        val rollStep = lineHeight + scrollLineGapExtraPx
        val centerBaseline = h / 2f - (fm.ascent + fm.descent) / 2f

        if (valueAnimator?.isRunning == true && transitionFrom != transitionTo) {
            val frac = transitionProgress.coerceIn(0f, 1f)
            val textOld = "$transitionFrom$suffixText"
            val textNew = "$transitionTo$suffixText"
            val offsetUp = frac * rollStep
            canvas.drawText(textOld, w / 2f, centerBaseline - offsetUp, textPaint)
            canvas.drawText(textNew, w / 2f, centerBaseline + (1f - frac) * rollStep, textPaint)
        } else {
            val text = "$currentNumber$suffixText"
            canvas.drawText(text, w / 2f, centerBaseline, textPaint)
        }

        canvas.restore()
    }

//    private fun dpToPx(dp: Float): Float {
//        return dp * resources.displayMetrics.density
//    }

    fun cancelAnimation() {
        valueAnimator?.cancel()
        valueAnimator = null
    }

    override fun onDetachedFromWindow() {
        cancelAnimation()
        super.onDetachedFromWindow()
    }
}
