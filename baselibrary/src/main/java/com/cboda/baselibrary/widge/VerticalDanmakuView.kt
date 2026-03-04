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
import androidx.core.view.doOnLayout
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.blankj.utilcode.util.SizeUtils.px2dp
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.DanmakuItem
import com.cboda.baselibrary.utils.eLogD
import java.util.LinkedList


class VerticalDanmakuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // 弹幕显示数量
//    private var maxLines = 6

    // 弹幕存活时间（毫秒）
//    private var danmakuLifetime = 8000L
    private val itemHeightPx: Int = dp2px(44f)
    private val viewQueue = LinkedList<View>()
    private val viewPool = LinkedList<DanmakuViewHolder>()

    // 主线程 Handler
    private val mainHandler = ThreadUtils.getMainHandler()

    // 跟踪所有待执行的移除任务，便于清理
//    private val pendingRemoveTasks = LinkedList<RemoveTask>()
    private val addViewQueue = LinkedList<DanmakuItem>()

    var imageLoader: ((ImageView, String?) -> Unit)? = null

    private val maxSize = 10000 //弹幕存储最大数量


    private val mainDelayMillis = 500L //每条弹幕间隔时长

    private val danMaAnimateTime = 3000L//弹幕动画时长


    init {
        clipChildren = false
        clipToPadding = false
    }

    /**
     * 添加一条弹幕
     * 所有弹幕都从底部下方滑入，3 秒后自动移除
     */
    fun addDanmaku(item: DanmakuItem) {
        if (addViewQueue.isNotEmpty() && item.isMeSend) {
            var haveAdd = false
            addViewQueue.forEachIndexed { index, item2 ->
                if (index != 0 && !item2.isMeSend && !haveAdd) {
                    haveAdd = true
                    addViewQueue.add(index, item)
                    if (addViewQueue.size > maxSize) {
                        addViewQueue.removeLast()
                    }
                }
            }
        } else {
            if (addViewQueue.size < maxSize) {
                addViewQueue.add(item)
            }
//            eLogD("VerticalDanmakuViewClass", "addViewQueueSize:${addViewQueue.size}")
        }
        if (addViewQueue.size == 1) {
            addAnimate()
        }
    }

    private fun addAnimate() {
        if (addViewQueue.isNotEmpty()) {
            danMaPause()
            val holder = if (viewPool.isNotEmpty()) {
                viewPool.removeFirst()
            } else {
                createViewHolder()
            }
            val item = addViewQueue[0]
            holder.bind(item, imageLoader)

            val rootView = holder.rootView
            rootView.alpha = 1f
            // 先放到屏幕外下方，避免 addView 后到 post 执行前以默认 y=0 在顶部闪一下
            rootView.y = (resources.displayMetrics.heightPixels + itemHeightPx).toFloat()

            addView(rootView)
            viewQueue.addLast(rootView)

            // 【新增】设置 3 秒后自动移除
//        scheduleRemove(rootView)

            // 延迟到布局完成后再设置位置并执行动画，避免首条弹幕时 height 仍为 0 导致 targetY 为负不显示
            post {
                eLogD("VerticalDanmakuViewClass", "height:$height ,itemHeightPx:$itemHeightPx")
                rootView.y = (height + itemHeightPx).toFloat()
                animateItems()
            }

            mainHandler.postDelayed(mainRun, mainDelayMillis)

            // 如果超过最大行数，立即移除最旧的（不受 3 秒限制）
//        if (viewQueue.size > maxLines) {
//            removeOldestItem()
//        }

        }

    }

    fun danMaResume() {
        mainHandler.postDelayed(mainRun, mainDelayMillis)
    }

    fun danMaPause() {
        mainHandler.removeCallbacks(mainRun)
    }

    private val mainRun = Runnable {
//        eLogD("VerticalDanmakuViewClass", "addViewQueueSize2:${addViewQueue.size}")
        if(addViewQueue.isNotEmpty()){
            addViewQueue.removeFirst()
        }
//        eLogD("VerticalDanmakuViewClass", "addViewQueueSize3:${addViewQueue.size}")
        addAnimate()
    }
    /**
     * 为 View 安排移除任务
     */
//    private fun scheduleRemove(view: View) {
//        val removeTask = RemoveTask(view)
//        mainHandler.postDelayed(removeTask, danmakuLifetime)
//        pendingRemoveTasks.add(removeTask)
//        // 将任务绑定到 View 上，便于单独取消
//        view.setTag(R.id.tag_remove_task, removeTask)
//    }

    /**
     * 取消 View 的移除任务
     */
//    private fun cancelRemoveTask(view: View) {
//        val task = view.getTag(R.id.tag_remove_task) as? RemoveTask
//        task?.let {
//            mainHandler.removeCallbacks(it)
//            pendingRemoveTasks.remove(it)
//        }
//    }

    /**
     * 执行所有可见项的位置动画
     */
    private fun animateItems() {
        val queueSize = viewQueue.size


//
        this.doOnLayout { view ->
            viewQueue.forEachIndexed { index, view ->
                if (index == queueSize - 1) {
                    val animatorSet = AnimatorSet()
                    val viewY = view.y
                    val viewH = view.height
//                val targetY = (height - (queueSize - index - 1) * itemHeightPx).toFloat()
                    val targetY = this.top
                    val targetY2 = this.bottom

//                    val location = IntArray(2)
//                    view.getLocationOnScreen(location)
//                    val topY = location[1]
//                    val bottomY = topY + viewH
//                    val topY2 = px2dp(topY.toFloat())
//                    val bottomY2 = px2dp(bottomY.toFloat())
//                    eLogD("VerticalDanmakuViewClass", "viewY:$viewY ,targetY:$targetY ,targetY2:$targetY2 topY:$topY ,bottomY:$bottomY ,viewH:$viewH ,index:$index")
//                    eLogD("VerticalDanmakuViewClass", "topY:$topY ,bottomY:$bottomY ,topY2:$topY2 ,bottomY2:$bottomY2")
                    val viewY2 = viewY - (targetY2 - targetY)
                    val translateYAnim = ObjectAnimator.ofFloat(view, "y", viewY, viewY2).apply {
                        duration = danMaAnimateTime
                        interpolator = AccelerateInterpolator()
                    }
//                    val translateYAnim = ObjectAnimator.ofFloat(view, "y", viewY, 0f).apply {
//                        duration = 5000
//                        interpolator = AccelerateInterpolator()
//                    }
                    val alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                        duration = danMaAnimateTime
                        interpolator = AccelerateInterpolator()
                    }
//                    mainHandler.postDelayed({
//                        val viewH2 = view.height
//                        eLogD("VerticalDanmakuViewClass", "viewH2:$viewH2")
//                    }, 2000L)
                    animatorSet.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            removeView(view)
                            if (!viewQueue.isEmpty()) {
                                viewQueue.removeFirst()
                            }
                            val holder = view.tag as? DanmakuViewHolder
                            holder?.reset()
                            holder?.let { viewPool.addLast(it) }
                        }
                    })
                    animatorSet.playTogether(translateYAnim, alphaAnim)
                    animatorSet.start()
                }
            }
        }

    }

    /**
     * 移除最旧的一条弹幕
     * 动画：向上移动 + 透明度渐变消失
     */
//    private fun removeOldestItem() {
//        if (viewQueue.isEmpty()) return
//
//        val oldestView = viewQueue.removeFirst()
//
//        // 【重要】取消该 View 的自动移除任务，避免重复执行
//        cancelRemoveTask(oldestView)
//
////        val currentY = oldestView.y
////        val removeY = currentY - itemHeightPx
//
//        val animatorSet = AnimatorSet()
//
//        // Y 轴向上移动
////        val translateYAnim = ObjectAnimator.ofFloat(oldestView, "y", currentY, removeY).apply {
////            duration = 300
////            interpolator = AccelerateInterpolator()
////        }
//
//        // 透明度渐变 (1.0 -> 0.0)
////        val alphaAnim = ObjectAnimator.ofFloat(oldestView, "alpha", 1f, 0f).apply {
////            duration = 300
////            interpolator = AccelerateInterpolator()
////        }
////
////        animatorSet.playTogether(alphaAnim)
//
//        animatorSet.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                removeView(oldestView)
//                val holder = oldestView.tag as? DanmakuViewHolder
//                holder?.reset()
//                holder?.let { viewPool.addLast(it) }
//            }
//        })
//
//        animatorSet.start()
//
//        // 剩余弹幕重新计算位置
//        postDelayed({
//            if (viewQueue.isNotEmpty()) {
//                animateItems()
//            }
//        }, 50)
//    }

    /**
     * 移除指定弹幕（可用于手动删除）
     */
//    fun removeDanmaku(view: View) {
//        if (viewQueue.contains(view)) {
//            // 从队列中移除
//            viewQueue.remove(view)
//            // 取消定时任务
//            cancelRemoveTask(view)
//            // 执行移除动画
//            val currentY = view.y
//            val removeY = currentY - itemHeightPx
//
//            val animatorSet = AnimatorSet()
////            val translateYAnim = ObjectAnimator.ofFloat(view, "y", currentY, removeY).apply {
////                duration = 300
////                interpolator = AccelerateInterpolator()
////            }
//            val alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
//                duration = 300
//                interpolator = AccelerateInterpolator()
//            }
//            animatorSet.playTogether(alphaAnim)
//            animatorSet.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    removeView(view)
//                    val holder = view.tag as? DanmakuViewHolder
//                    holder?.reset()
//                    holder?.let { viewPool.addLast(it) }
//                }
//            })
//            animatorSet.start()
//
//            // 剩余弹幕重新计算位置
//            postDelayed({
//                if (viewQueue.isNotEmpty()) {
//                    animateItems()
//                }
//            }, 50)
//        }
//    }

    private fun createViewHolder(): DanmakuViewHolder {
        val rootView = LayoutInflater.from(context)
            .inflate(R.layout.item_danmaku, this, false)
        val holder = DanmakuViewHolder(rootView)
        rootView.tag = holder
        return holder
    }

//    private fun dp2px(dp: Float): Int {
//        return (dp * resources.displayMetrics.density + 0.5f).toInt()
//    }

    /**
     * 清空所有弹幕
     */
    fun clearAll() {
        // 取消所有待执行的移除任务

        mainHandler.removeCallbacks(mainRun)
//        pendingRemoveTasks.forEach { mainHandler.removeCallbacks(it) }
//        pendingRemoveTasks.clear()

        // 移除所有 View
        viewQueue.forEach { removeView(it) }
        viewQueue.clear()
        viewPool.clear()
        addViewQueue.clear()
    }

//    fun setMaxLines(max: Int) {
//        maxLines = max
//    }

    /**
     * 设置弹幕存活时间（毫秒）
     */
//    fun setDanmakuLifetime(millis: Long) {
//        danmakuLifetime = millis
//    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 【重要】清理所有 pending 任务，防止内存泄漏
//        pendingRemoveTasks.forEach { mainHandler.removeCallbacks(it) }
//        pendingRemoveTasks.clear()
        clearAll()
    }

    /**
     * 移除任务类
     */
//    private inner class RemoveTask(private val targetView: View) : Runnable {
//        override fun run() {
//            // 检查 View 是否仍在队列中
//            if (viewQueue.contains(targetView)) {
//                removeOldestItem()
//            }
//            // 从待执行任务列表中移除
//            pendingRemoveTasks.remove(this)
//        }
//    }

    class DanmakuViewHolder(val rootView: View) {
        private val ivAvatar: ImageView = rootView.findViewById(R.id.ivAvatar)
        private val tvContent: TextView = rootView.findViewById(R.id.tvContent)
        private var clickListener: (() -> Unit)? = null

        fun bind(item: DanmakuItem, imageLoader: ((ImageView, String?) -> Unit)?) {
            tvContent.text = item.text
            tvContent.setTextColor(item.textColor)
            clickListener = item.onClick

            if (item.showAvatar && !item.avatarUrl.isNullOrBlank()) {
                ivAvatar.visibility = VISIBLE
                imageLoader?.invoke(ivAvatar, item.avatarUrl)
            } else if (item.iconResId != null) {
                ivAvatar.visibility = VISIBLE
                ivAvatar.setImageResource(item.iconResId)
            } else {
                ivAvatar.visibility = GONE
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