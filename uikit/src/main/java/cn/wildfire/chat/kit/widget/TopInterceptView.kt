package cn.wildfire.chat.kit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View



class TopInterceptView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 按下事件
                return true // 返回true表示消费事件
            }
            MotionEvent.ACTION_UP -> {
                // 抬起事件 - 处理点击
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                // 事件取消
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}