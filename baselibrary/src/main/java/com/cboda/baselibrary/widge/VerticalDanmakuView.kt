package com.cboda.baselibrary.widge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.DanmakuItem
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
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
    private var oldY = 0f

    // 主线程 Handler
//    private val mainHandler = ThreadUtils.getMainHandler()

    // 跟踪所有待执行的移除任务，便于清理
//    private val pendingRemoveTasks = LinkedList<RemoveTask>()
    private val addViewQueue = LinkedList<DanmakuItem>()

    private val animatorSetQueue = LinkedList<AnimatorSet>()

    var imageLoader: ((ImageView, String?) -> Unit)? = null

    private val maxSize = 10000 //弹幕存储最大数量


//    private val mainDelayMillis = 500L //每条弹幕间隔时长

    private val danMaAnimateTime = 3000L//弹幕动画时长

//    private var canRun = false


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
            var insertIndex = -1
            for (i in addViewQueue.indices) {
                if (i != 0 && !addViewQueue[i].isMeSend) {
                    insertIndex = i
                    break
                }
            }
            if (insertIndex >= 0) {
                addViewQueue.add(insertIndex, item)
                if (addViewQueue.size > maxSize) addViewQueue.removeLast()
            } else {
                if (addViewQueue.size < maxSize) addViewQueue.add(item)
            }
        } else {
            if (addViewQueue.size < maxSize) addViewQueue.add(item)
        }
//        eLogD("VerticalDanmakuViewClass", "addViewQueueSize:${addViewQueue.size}")
//        if (addViewQueue.size == 1) {
        addAnimate()
//        }
    }

    private fun addAnimate() {
//        if (!canRun) {
//            return
//        }
        try {
            if (height.toFloat() - oldY <= itemHeightPx) {
//            eLogD("VerticalDanmakuViewClass", "height:$height ,oldY:$oldY ,itemHeightPx:$itemHeightPx")
                return
            }
            if (addViewQueue.isNotEmpty()) {
//            canRun = false
//            mainHandler.removeCallbacks(mainRun)
                val rootViewY1 = (resources.displayMetrics.heightPixels + itemHeightPx).toFloat()
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
                rootView.y = rootViewY1

                addView(rootView)
                viewQueue.addLast(rootView)

                // 【新增】设置 3 秒后自动移除
//        scheduleRemove(rootView)

                // 延迟到布局完成后再设置位置并执行动画，避免首条弹幕时 height 仍为 0 导致位置计算错误
                post {
                    // 初始 Y 放在「容器底部的下面一点」，保证从容器底部滑上来而不是直接出现在容器内部
                    val containerHeight = height
                    // 往下偏移 0.5 个弹幕高度，可按需要微调
                    val startY = containerHeight
                    rootView.y = startY.toFloat()
//                eLogD("VerticalDanmakuViewClass", "rootViewY1:$rootViewY1 ,startY:$startY")
                    animateItems()
                }
                if (addViewQueue.isNotEmpty()) {
                    addViewQueue.removeFirst()
                }
//            mainHandler.postDelayed(mainRun, mainDelayMillis)

                // 如果超过最大行数，立即移除最旧的（不受 3 秒限制）
//        if (viewQueue.size > maxLines) {
//            removeOldestItem()
//        }

            } else {
//            canRun = true
            }
        } catch (e: Exception) {
            e.toLog("VerticalDanmakuViewClass")
        }

    }


    fun danMaResume() {
        if (animatorSetQueue.isNotEmpty()) {
            for (i in animatorSetQueue.indices) {
                animatorSetQueue[i].resume()
            }
        }
//        mainHandler.postDelayed(mainRun, mainDelayMillis)
//        mainHandler.post(mainRun)
    }

    fun danMaPause() {
//        mainHandler.removeCallbacks(mainRun)
        if (animatorSetQueue.isNotEmpty()) {
            for (i in animatorSetQueue.indices) {
                animatorSetQueue[i].pause()
            }
        }
//        canRun = false
    }

//    private val mainRun = Runnable {
////        eLogD("VerticalDanmakuViewClass", "addViewQueueSize2:${addViewQueue.size}")
//        canRun = true
////        eLogD("VerticalDanmakuViewClass", "addViewQueueSize3:${addViewQueue.size}")
//        addAnimate()
//    }
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
     * 使用快照遍历，避免动画回调里 viewQueue.remove 导致 ConcurrentModificationException
     */
    private fun animateItems() {
        this.doOnLayout {
            val snapshot = viewQueue.toList()
            if (snapshot.isEmpty()) return@doOnLayout
            val queueSize = snapshot.size
            val lastView = snapshot[queueSize - 1]
            val viewY = lastView.y
            val targetTop = this.top
            val targetBottom = this.bottom
            val viewY2 = viewY - (targetBottom - targetTop)
//            eLogD("VerticalDanmakuViewClass", "viewY:$viewY ,viewY2:$viewY2")
            val animatorSet = AnimatorSet()
            val translateYAnim = ObjectAnimator.ofFloat(lastView, "y", viewY, viewY2).apply {
                duration = danMaAnimateTime
                interpolator = LinearInterpolator()
            }
            translateYAnim.addUpdateListener {
//                val currentX = lastView.x  // ✅ 实时坐标
//                val currentY = lastView.y
                if (viewQueue.size - 1 >= 0 && lastView == viewQueue[viewQueue.size - 1]) {
                    oldY = lastView.y
                    addAnimate()
//                    eLogD("VerticalDanmakuViewClass", "currentX:$currentX ,currentY:$currentY")
                }
                // 处理逻辑
            }
            val alphaAnim = ObjectAnimator.ofFloat(lastView, "alpha", 0f, 0.3f, 1f, 1f, 0.8f, 0f).apply {
                duration = danMaAnimateTime
                interpolator = LinearInterpolator()
            }
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animatorSetQueue.isNotEmpty()) {
                        animatorSetQueue.removeFirst()
                    }
                    removeView(lastView)
                    viewQueue.remove(lastView)
                    val holder = lastView.tag as? DanmakuViewHolder
                    holder?.reset()
                    holder?.let { viewPool.addLast(it) }
                }


            })
            animatorSet.playTogether(translateYAnim, alphaAnim)
            if (animatorSetQueue.size < maxSize) {
                animatorSetQueue.add(animatorSet)
            }
//            animatorSetQueue.add(animatorSet)
            animatorSet.start()

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
//        mainHandler.removeCallbacks(mainRun)
        val views = viewQueue.toList()
        viewQueue.clear()
        views.forEach { removeView(it) }
        viewPool.clear()
        addViewQueue.clear()
        animatorSetQueue.clear()
        oldY = 0f
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
        private val rightIv: ImageView = rootView.findViewById(R.id.rightIv)
        private val tvContent: TextView = rootView.findViewById(R.id.tvContent)
        private var clickListener: (() -> Unit)? = null

        fun bind(item: DanmakuItem, imageLoader: ((ImageView, String?) -> Unit)?) {
            tvContent.text = item.content
//            tvContent.setTextColor(item.textColor)
//            clickListener = item.onClick
            if (!item.avatarUrl.isNullOrBlank()) {
                ivAvatar.isGone = imageLoader == null
                imageLoader?.invoke(ivAvatar, item.avatarUrl)
            } else {
                ivAvatar.isGone = true
            }
            if (item.iconResId != null && item.iconResId != 0) {
                item.iconResId?.let { rightIv.setImageResource(it) }
            }
            rightIv.isGone = item.iconResId == null
//            rootView.setOnClickListener { clickListener?.invoke() }
        }

        fun reset() {
            tvContent.text = ""
            ivAvatar.setImageResource(0)
            ivAvatar.isGone = true
            clickListener = null
            rootView.setOnClickListener(null)
            rootView.alpha = 1f
            rootView.y = 0f
        }
    }
}