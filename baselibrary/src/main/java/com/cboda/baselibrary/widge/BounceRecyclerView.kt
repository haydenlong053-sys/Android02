package com.cboda.baselibrary.widge

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView

class BottomBounceRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var startY = 0f
    private var isDragging = false
    private var dragDistance = 0f

    // 阻尼系数：0.25(偏重) ~ 0.4(偏轻)，推荐 0.32
    private val DAMPING = 0.32f
    // 最大拖拽距离（px），防止过度拉伸
    private val MAX_DRAG = 200f

    init {
        // 关闭系统默认的越界发光/水波纹
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                isDragging = false
                dragDistance = translationY
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - startY
                startY = ev.y

                val atBottom = !canScrollVertically(1)

                // ✅ 仅在底部且继续下拉时，才进入拖拽状态
                if (atBottom && deltaY > 0) {
                    isDragging = true
                    dragDistance += deltaY * DAMPING
                    dragDistance = dragDistance.coerceAtMost(MAX_DRAG)
                    translationY = dragDistance
                    return true // 拦截事件，阻断传递给 SwipeRefreshLayout
                }
                // ✅ 已在拖拽状态，处理手指回滑（向上）
                else if (isDragging) {
                    dragDistance += deltaY * DAMPING
                    dragDistance = dragDistance.coerceAtLeast(0f)
                    translationY = dragDistance

                    if (dragDistance <= 0f) {
                        isDragging = false
                        translationY = 0f
                    } else {
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging && dragDistance > 0f) {
                    // 松手平滑弹回
                    animate().translationY(0f)
                        .setDuration(280)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            isDragging = false
                            dragDistance = 0f
                            translationY = 0f
                        }
                        .start()
                    return true
                }
            }
        }
        // 🟢 顶部滑动、正常滚动、点击Item等，全部交由系统处理（SwipeRefreshLayout可正常拦截）
        return super.dispatchTouchEvent(ev)
    }
}