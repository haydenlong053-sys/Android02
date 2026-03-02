package com.cboda.baselibrary.widge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.DanmakuItem
import java.util.LinkedList


class VerticalDanmakuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var maxLines = 6
    private val itemHeightPx: Int = dp2px(44f)
    private val viewQueue = LinkedList<View>()
    private val viewPool = LinkedList<DanmakuViewHolder>()

    var imageLoader: ((ImageView, String?) -> Unit)? = null

    init {
        clipChildren = false
        clipToPadding = false
    }

    /**
     * 添加一条弹幕
     * 所有弹幕都从底部下方滑入
     */
    fun addDanmaku(item: DanmakuItem) {
        val holder = if (viewPool.isNotEmpty()) {
            viewPool.removeFirst()
        } else {
            createViewHolder()
        }

        holder.bind(item, imageLoader)

        val rootView = holder.rootView
        rootView.alpha = 1f
        // 【关键】初始位置在容器底部下方，确保滑入动画
        rootView.y = (height + itemHeightPx).toFloat()

        addView(rootView)
        viewQueue.addLast(rootView)

        animateItems()

        if (viewQueue.size > maxLines) {
            removeOldestItem()
        }
    }

    /**
     * 执行所有可见项的位置动画
     */
    private fun animateItems() {
        val queueSize = viewQueue.size

        viewQueue.forEachIndexed { index, view ->
            // 目标位置：最早在最上，最新在最下
            val targetY = (height - (queueSize - index) * itemHeightPx).toFloat()

            if (kotlin.math.abs(view.y - targetY) > 1f) {
                ObjectAnimator.ofFloat(view, "y", view.y, targetY).apply {
                    duration = 300
                    interpolator = AccelerateInterpolator()
                    start()
                }
            }
        }
    }

    /**
     * 移除最旧的一条弹幕
     * 动画：向上移动 + 透明度渐变消失
     */
    private fun removeOldestItem() {
        val oldestView = viewQueue.removeFirst()
        val currentY = oldestView.y
        val removeY = currentY - itemHeightPx

        val animatorSet = AnimatorSet()

        // Y 轴向上移动
        val translateYAnim = ObjectAnimator.ofFloat(oldestView, "y", currentY, removeY).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }

        // 透明度渐变 (1.0 -> 0.0)
        val alphaAnim = ObjectAnimator.ofFloat(oldestView, "alpha", 1f, 0f).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }

        animatorSet.playTogether(translateYAnim, alphaAnim)

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                removeView(oldestView)
                val holder = oldestView.tag as? DanmakuViewHolder
                holder?.reset()
                holder?.let { viewPool.addLast(it) }
            }
        })

        animatorSet.start()

        postDelayed({
            if (viewQueue.isNotEmpty()) {
                animateItems()
            }
        }, 50)
    }

    private fun createViewHolder(): DanmakuViewHolder {
        val rootView = LayoutInflater.from(context)
            .inflate(R.layout.item_danmaku, this, false)
        val holder = DanmakuViewHolder(rootView)
        rootView.tag = holder
        return holder
    }

    private fun dp2px(dp: Float): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    fun clearAll() {
        viewQueue.forEach { removeView(it) }
        viewQueue.clear()
        viewPool.clear()
    }

    fun setMaxLines(max: Int) {
        maxLines = max
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAll()
    }

    class DanmakuViewHolder(val rootView: View) {
        private val ivAvatar: ImageView = rootView.findViewById(R.id.ivAvatar)
        private val tvContent: TextView = rootView.findViewById(R.id.tvContent)
        private var clickListener: (() -> Unit)? = null

        fun bind(item: DanmakuItem, imageLoader: ((ImageView, String?) -> Unit)?) {
            tvContent.text = item.text
            tvContent.setTextColor(item.textColor)
            clickListener = item.onClick

            if (item.showAvatar && !item.avatarUrl.isNullOrBlank()) {
                ivAvatar.visibility = View.VISIBLE
                imageLoader?.invoke(ivAvatar, item.avatarUrl)
            } else if (item.iconResId != null) {
                ivAvatar.visibility = View.VISIBLE
                ivAvatar.setImageResource(item.iconResId)
            } else {
                ivAvatar.visibility = View.GONE
            }

            rootView.setOnClickListener { clickListener?.invoke() }
        }

        fun reset() {
            tvContent.text = ""
            ivAvatar.setImageResource(0)
            ivAvatar.visibility = View.GONE
            clickListener = null
            rootView.setOnClickListener(null)
            rootView.alpha = 1f
            rootView.y = 0f
        }
    }
}