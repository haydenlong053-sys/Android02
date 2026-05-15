package com.cboda.baselibrary.widge

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * 嵌套滑动场景下的 ViewPager2 容器。
 *
 * [ViewPager2] 在 AndroidX 中为 `final`，不能直接继承；通过 [FrameLayout] 包裹内部
 * [viewPager2]，在容器层处理 [onInterceptTouchEvent]，与原先继承写法意图一致。
 */
class NestedViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    val viewPager2: ViewPager2 = ViewPager2(context, attrs).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private var startX = 0f
    private var startY = 0f
    private var isHorizontalDrag = false

    init {
        addView(viewPager2)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!viewPager2.isUserInputEnabled) return super.onInterceptTouchEvent(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
                isHorizontalDrag = false
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - startX
                val dy = ev.y - startY

                if (!isHorizontalDrag &&
                    (abs(dx) > abs(dy) || abs(dx) > ViewConfiguration.get(context).scaledTouchSlop)
                ) {
                    isHorizontalDrag = abs(dx) > abs(dy)
                }

                if (isHorizontalDrag) {
                    val itemCount = viewPager2.adapter?.itemCount ?: 0
                    val isAtStart = viewPager2.currentItem == 0
                    val isAtEnd = viewPager2.currentItem == itemCount - 1
                    val swipingToPrev = dx > 0
                    val swipingToNext = dx < 0

                    val isEdgeAndCannotScroll =
                        (isAtStart && swipingToPrev) || (isAtEnd && swipingToNext)

                    if (isEdgeAndCannotScroll) {
                        parent?.requestDisallowInterceptTouchEvent(false)
                    } else {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                } else {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHorizontalDrag = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return false
    }
}
