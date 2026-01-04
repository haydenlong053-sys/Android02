package com.cboda.baselibrary.view


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView

/**
 * 循环向上滚动的TextView
 * 支持渐变效果
 */
class ScrollTextBroadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textViews = mutableListOf<TextView>()
    private val textList = mutableListOf<String>()
    private var currentIndex = 0
    private var isScrolling = false
    private var scrollDuration = 1500L // 滚动持续时间（毫秒）
    private var stayDuration = 2500L // 停留时间（毫秒）

    private var gradientPaint: Paint? = null
    private var showGradient = true // 是否显示渐变遮罩
    private var gradientHeight = 20f // 渐变区域高度（dp转px）

    init {
        clipChildren = true
        clipToPadding = false
        setWillNotDraw(false)
    }

    /**
     * 设置文本列表
     */
    fun setTextList(texts: List<String>) {
        textList.clear()
        textList.addAll(texts)

        // 为了实现循环，需要添加额外的文本
        if (textList.size > 1) {
            textList.add(textList[0]) // 添加第一个文本到末尾，实现无缝循环
        }

        createTextViews()
        startScroll()
    }

    /**
     * 设置滚动速度（毫秒）
     */
    fun setScrollDuration(duration: Long) {
        scrollDuration = duration
    }

    /**
     * 设置停留时间（毫秒）
     */
    fun setStayDuration(duration: Long) {
        stayDuration = duration
    }

    /**
     * 设置是否显示渐变遮罩
     */
    fun setShowGradient(show: Boolean) {
        showGradient = show
        if (show && height > 0) {
            initGradientPaint()
        }
        invalidate()
    }

    /**
     * 初始化渐变画笔
     */
    private fun initGradientPaint() {
        if (height <= 0) return

        val gradientHeightPx = gradientHeight * resources.displayMetrics.density

        gradientPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f,
                0f, gradientHeightPx,
                intArrayOf(
                    0xFFFFFFFF.toInt(), // 顶部完全不透明（白色遮罩）
                    0x00FFFFFF.toInt()  // 底部完全透明
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT) // 使用DST_OUT模式实现渐变遮罩
        }
    }

    private fun createTextViews() {
        removeAllViews()
        textViews.clear()

        textList.forEach { text ->
            val textView = TextView(context).apply {
                this.text = text
                textSize = 14f
                setTextColor(0xFF666666.toInt())
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
            }
            textViews.add(textView)
            addView(textView)
        }

        // 等待布局完成后再设置位置
        post {
            // 初始位置：第一个在可见区域，第二个在下方
            if (textViews.size >= 2) {
                textViews[0].translationY = 0f
                textViews[1].translationY = measuredHeight.toFloat()
            } else if (textViews.size == 1) {
                textViews[0].translationY = 0f
            }
        }
    }

    private fun startScroll() {
        if (isScrolling || textViews.size < 2) return

        isScrolling = true
        scrollToNext()
    }

    private fun scrollToNext() {
        if (textViews.size < 2 || measuredHeight <= 0) return

        val currentView = textViews[currentIndex]
        val nextIndex = (currentIndex + 1) % (textList.size - 1) // 减1是因为最后一个是重复的
        val nextView = textViews[nextIndex]

        // 将下一个TextView移到下方
        nextView.translationY = measuredHeight.toFloat()

        // 创建向上滚动动画
        val currentAnimator = ObjectAnimator.ofFloat(
            currentView,
            "translationY",
            currentView.translationY,
            -measuredHeight.toFloat()
        ).apply {
            duration = scrollDuration
            interpolator = LinearInterpolator()
        }

        val nextAnimator = ObjectAnimator.ofFloat(
            nextView,
            "translationY",
            nextView.translationY,
            0f
        ).apply {
            duration = scrollDuration
            interpolator = LinearInterpolator()
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(currentAnimator, nextAnimator)
            start()
        }

        // 更新索引
        currentIndex = nextIndex

        // 延迟后继续滚动
        postDelayed({
            // 将已经滚出视野的TextView移回底部，实现循环
            if (currentView.translationY < -measuredHeight / 2) {
                currentView.translationY = measuredHeight.toFloat()
            }

            // 继续滚动
            postDelayed({
                if (isScrolling) {
                    scrollToNext()
                }
            }, stayDuration)
        }, scrollDuration)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 确保高度足够显示一行文本
        val minHeight = (textSize * 1.5f + paddingTop + paddingBottom).toInt()
        if (measuredHeight < minHeight) {
            setMeasuredDimension(measuredWidth, minHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 初始化渐变画笔
        if (showGradient && h > 0) {
            initGradientPaint()
        }

        // 重新定位TextView
        if (textViews.isNotEmpty() && h > 0) {
            textViews.forEachIndexed { index, textView ->
                if (index == currentIndex) {
                    textView.translationY = 0f
                } else {
                    textView.translationY = h.toFloat()
                }
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // 先绘制子视图
        super.dispatchDraw(canvas)

        // 绘制顶部和底部的渐变遮罩（淡入淡出效果）
        if (showGradient && height > 0) {
            val gradientHeightPx = gradientHeight * resources.displayMetrics.density

            // 绘制顶部渐变遮罩（从上到下淡出）
            val topGradient = LinearGradient(
                0f, 0f,
                0f, gradientHeightPx,
                intArrayOf(
                    0xFFFFFFFF.toInt(), // 顶部白色（遮罩）
                    0x00FFFFFF.toInt()  // 底部透明
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            val topPaint = Paint().apply {
                shader = topGradient
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), gradientHeightPx, topPaint)

            // 绘制底部渐变遮罩（从下到上淡出）
            val bottomGradient = LinearGradient(
                0f, height - gradientHeightPx,
                0f, height.toFloat(),
                intArrayOf(
                    0x00FFFFFF.toInt(), // 顶部透明
                    0xFFFFFFFF.toInt()  // 底部白色（遮罩）
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            val bottomPaint = Paint().apply {
                shader = bottomGradient
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }
            canvas.drawRect(0f, height - gradientHeightPx, width.toFloat(), height.toFloat(), bottomPaint)
        }
    }

    /**
     * 停止滚动
     */
    fun stopScroll() {
        isScrolling = false
        removeCallbacks(null)
        clearAnimation()
        textViews.forEach { it.clearAnimation() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScroll()
    }

    private val textSize: Float
        get() = if (textViews.isNotEmpty()) {
            textViews[0].textSize
        } else {
            14f * resources.displayMetrics.scaledDensity
        }
}

