package com.cboda.intelligent.widge

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.customview.widget.ViewDragHelper

/**
 * 侧滑布局：主内容在左侧，操作区在右侧。
 * 随左滑手势，右侧操作区从屏幕右方滑入（整体内容左移，露出右侧按钮）。
 */
class SwipeRevealLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    init {
        clipChildren = true
        clipToPadding = true
    }

    /** 整体内容容器 [主布局 | 操作区]，随手势水平移动 */
    private var contentView: View? = null
    private var revealWidth: Int = 0
    private var scrollX: Int = 0

    private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean = child == contentView

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return left.coerceIn(-revealWidth, 0)
        }

        override fun getViewHorizontalDragRange(child: View): Int = if (child == contentView) revealWidth else 0

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            scrollX = -left
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val settle = when {
                xvel < -VEL_THRESHOLD -> -revealWidth
                xvel > VEL_THRESHOLD -> 0
                scrollX > revealWidth / 2 -> -revealWidth
                else -> 0
            }
            dragHelper.settleCapturedViewAt(settle, releasedChild.top)
            invalidate()
        }
    })

    companion object {
        private const val VEL_THRESHOLD = 400f
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount >= 1) {
            contentView = getChildAt(0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpec = MeasureSpec.getSize(heightMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        contentView?.let { content ->
            // 先测量操作区宽度（content 的第二个子 View）
            if (content is ViewGroup && content.childCount >= 2) {
                val buttons = content.getChildAt(1)
                measureChild(buttons, MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), heightMeasureSpec)
                revealWidth = buttons.measuredWidth
            }
            val contentWidth = width + revealWidth
            measureChild(content, MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), heightMeasureSpec)
        }

        val h = when (hMode) {
            MeasureSpec.EXACTLY -> heightSpec
            else -> contentView?.measuredHeight ?: 0
        }
        setMeasuredDimension(width, h)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val h = b - t
        contentView?.let { content ->
            val left = content.left.coerceIn(-revealWidth, 0)
            content.layout(left, 0, left + w + revealWidth, h)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = dragHelper.shouldInterceptTouchEvent(ev)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            val left = contentView?.left ?: 0
            scrollX = -left
            invalidate()
        }
    }

    /** 关闭侧滑，收起右侧操作区 */
    fun closeSwipe() {
        contentView?.let {
            dragHelper.smoothSlideViewTo(it, 0, it.top)
            invalidate()
        }
    }
}
