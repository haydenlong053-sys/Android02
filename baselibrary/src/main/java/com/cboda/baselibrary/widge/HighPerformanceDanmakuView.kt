package com.cboda.baselibrary.widge

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.cboda.baselibrary.bean.DanmakuItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class HighPerformanceDanmakuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    // 配置
    private var danmakuHeight = 44f          // 每条弹幕高度 (dp)
    private var danmakuPadding = 16f         // 内边距 (dp)
    private var danmakuMargin = 8f           // 间距 (dp)
    private var animationDuration = 300L     // 动画时长 (ms)
    private var targetFPS = 90               // 目标帧率

    // 弹幕队列：正在播放中的 & 等待进入的
    private val danmakuQueue = CopyOnWriteArrayList<DanmakuItem>()
    private val pendingQueue = LinkedBlockingQueue<DanmakuItem>()

    // 渲染相关
//    private val holder: SurfaceHolder = holder
    private var isRendering = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    // 对象池
    private val danmakuPool = LinkedBlockingQueue<DanmakuItem>(20)

    // 尾图 Bitmap 缓存，避免每条弹幕都 decodeResource 导致主线程卡顿
    private val trailingBitmapCache = ConcurrentHashMap<Int, Bitmap>()

    // 性能监控
    private var frameCount = 0
    private var lastFpsTime = 0L
    private var currentFPS = 0

    // 逐帧时间计算，用于平滑动画
    private var lastUpdateTime = System.currentTimeMillis()

    // 底部触发间距（px）：当最底部弹幕距离底部达到该值时，才允许下一条从队列进入飘屏
    private var bottomTriggerPx = 0f

    // 后台单线程执行 lockCanvas + 绘制，避免与消息列表争抢主线程导致卡顿
    private var renderExecutor: ExecutorService? = null

    init {
        holder.addCallback(this)
        setZOrderOnTop(true) // 确保在最上层
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // dp 转 px
        val density = resources.displayMetrics.density
        danmakuHeight = 44f * density
        danmakuPadding = 16f * density
        danmakuMargin = 8f * density
        // 最底部弹幕距离底部达到 5dp 时，才允许下一条从队列进入飘屏
        bottomTriggerPx = 5f * density
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startRendering()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRendering()
    }

    /**
     * 添加弹幕
     */
    fun addDanmaku(item: DanmakuItem) {
        // 从对象池获取或创建新对象
        val danmaku = danmakuPool.poll() ?: DanmakuItem()

        // 复用对象属性
        danmaku.content = item.content
        danmaku.avatarBitmap = item.avatarBitmap
        danmaku.textColor = item.textColor
        danmaku.textSize = item.textSize
        danmaku.backgroundColor = item.backgroundColor
        danmaku.speed = item.speed
        // isMeSend 用于在待展示队列中排序，必须从外部传进来的 item 拷贝
        danmaku.isMeSend = item.isMeSend
        danmaku.createdAt = System.currentTimeMillis()
        danmaku.alpha = 1f
        danmaku.isVisible = true
        // 文字后图片：支持 Bitmap（远程/已解码）或本地 resId
        danmaku.trailingImageBitmap = item.trailingImageBitmap
        danmaku.iconResId = item.iconResId
        danmaku.trailingImageWidthDp = item.trailingImageWidthDp
        danmaku.trailingImageHeightDp = item.trailingImageHeightDp
        if (item.iconResId != null && item.iconResId != 0) {
            val resId = item.iconResId!!
            // 优先从缓存中取，避免频繁 decodeResource 卡住 UI
            val cached = trailingBitmapCache[resId]
            if (cached != null && !cached.isRecycled) {
                danmaku.trailingImageBitmap = cached
            } else {
                try {
                    val bmp = BitmapFactory.decodeResource(resources, resId)
                    if (bmp != null && !bmp.isRecycled) {
                        trailingBitmapCache[resId] = bmp
                        danmaku.trailingImageBitmap = bmp
                    } else {
                        danmaku.trailingImageBitmap = null
                    }
                } catch (e: Exception) {
                    danmaku.trailingImageBitmap = null
                }
            }
        }

        // 计算尺寸（提前测量，避免渲染时计算）
        measureDanmaku(danmaku)

        // 加入待处理队列
        pendingQueue.offer(danmaku)
        // 渲染循环在后台线程固定帧率运行，这里无需再手动触发
    }

    /**
     * 测量弹幕尺寸
     */
    private fun measureDanmaku(danmaku: DanmakuItem) {
        val density = resources.displayMetrics.density
        textPaint.textSize = danmaku.textSize * density
        val textWidth = textPaint.measureText(danmaku.content)
        danmaku.width = textWidth + danmakuPadding * 2
        danmaku.height = danmakuHeight

        // 如果有头像，增加宽度
        if (danmaku.avatarBitmap != null) {
            danmaku.width += danmaku.height + danmakuPadding
        }
        // 如果有文字后图片，增加宽度和间距
        if (danmaku.trailingImageBitmap != null) {
            val wDp = if (danmaku.trailingImageWidthDp > 0) danmaku.trailingImageWidthDp else 24f
            val gapDp = 4f
            danmaku.width += wDp * density + gapDp * density
        }
    }

    /**
     * 开始渲染：注册 Choreographer 回调，创建后台绘制线程
     */
    private fun startRendering() {
        if (isRendering) return
        isRendering = true
        lastUpdateTime = System.currentTimeMillis()
        if (renderExecutor == null || renderExecutor?.isShutdown == true) {
            renderExecutor = Executors.newSingleThreadExecutor { r ->
                Thread(r, "DanmakuRender").apply { priority = Thread.NORM_PRIORITY }
            }
        }

        // 后台固定帧率循环渲染，不再依赖主线程 Choreographer，避免主线程被消息列表占用时导致弹幕抖动
        val frameIntervalMs = 1000L / targetFPS.coerceAtLeast(30)
        renderExecutor?.execute {
            while (isRendering) {
                val frameStart = System.currentTimeMillis()
                try {
                    renderFrame()
                } catch (_: Exception) {
                }
                val cost = System.currentTimeMillis() - frameStart
                val sleep = frameIntervalMs - cost
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }
        }
    }

    /**
     * 停止渲染：移除 Choreographer 回调并关闭后台线程
     */
    private fun stopRendering() {
        if (!isRendering) return
        isRendering = false
        renderExecutor?.shutdownNow()
        renderExecutor = null
    }

    /**
     * 渲染单帧
     */
    private fun renderFrame() {
        val canvas: Canvas?
        try {
            canvas = holder.lockCanvas()
        } catch (e: Exception) {
            return
        }

        if (canvas == null) return

        try {
            // 清空画布
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            // 处理待添加的弹幕
            processPendingQueue()

            // 更新弹幕位置（基于创建时间的绝对时间差，避免逐帧累积误差导致抖动）
            updateDanmakuPositions()

            // 绘制弹幕
            drawDanmakus(canvas)

            // 性能监控
            updateFPS()
        } finally {
            try {
                holder.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                // 忽略
            }
        }
    }

    /**
     * 处理待添加队列
     * 规则：
     * - 新弹幕先进入 pendingQueue
     * - 只有当当前最底部可见弹幕距离底部至少 bottomTriggerPx(5dp) 时，才从队列中取出一条加入飘屏
     * - 如果当前没有任何可见弹幕，则可以直接取出一条加入飘屏
     */
    private val maxSize = 66 // 仿照 VerticalDanmakuView，控制队列最大存储数量

    /**
     * 处理待添加队列
     * 规则：
     * - 使用 maxSize 限制 pendingQueue + danmakuQueue 的总量，避免无限堆积
     * - 新弹幕进入时，优先保证 isMeSend 的弹幕尽量靠前（仿照 VerticalDanmakuView）
     * - 进入飘屏时仍然遵守“最底部弹幕与底部间距”规则，避免重叠
     */
    private fun processPendingQueue() {
        if (pendingQueue.isEmpty()) return

        // 总量限制：正在播放 + 等待队列超过 maxSize 时，丢弃最旧的一条等待弹幕
        while (pendingQueue.size + danmakuQueue.size > maxSize) {
            pendingQueue.poll()
        }

        val visibleList = danmakuQueue.filter { it.isVisible }
        val canShowNew = if (visibleList.isEmpty()) {
            // 屏幕上没有弹幕时，可以直接显示一条
            true
        } else {
            // 找到当前最靠近底部的弹幕
            val bottomMost = visibleList.maxByOrNull { it.y + it.height }!!
            val bottomY = bottomMost.y + bottomMost.height
            val gapToBottom = height - bottomY
            // 只有当最底部弹幕离底部一定距离时，才允许新的弹幕进入，避免重叠过多
            gapToBottom >= -120f
        }

        if (!canShowNew) return

        // 出队规则：优先从 pendingQueue 中挑选 isMeSend 的弹幕，使自己的弹幕更优先上屏
        val danmaku: DanmakuItem = run {
            val list = pendingQueue.toMutableList()
            val indexOfMe = list.indexOfFirst { it.isMeSend }
            if (indexOfMe > 0) {
                val meItem = list.removeAt(indexOfMe)
                pendingQueue.clear()
                pendingQueue.addAll(list)
                meItem
            } else {
                pendingQueue.poll() ?: return
            }
        }

        // 初始位置：从容器底部往下开始，保证从底部滑入
        danmaku.startY = height + danmaku.height
        danmaku.y = danmaku.startY
        // 目标位置设为顶部之外，仅作参考
        danmaku.targetY = -danmaku.height

        danmaku.isVisible = true
        danmaku.createdAt = System.currentTimeMillis()

        danmakuQueue.add(danmaku)

        // 如果超过最大数量，标记最旧的为隐藏
//        while (danmakuQueue.size > maxVisibleCount) {
//            danmakuQueue.firstOrNull()?.isVisible = false
//        }
    }

    /**
     * 更新弹幕位置
     * 每条弹幕从下往上匀速飘动，接近顶部时逐渐透明直至消失。
     * 使用「当前时间 - 创建时间」的绝对时间差来计算位置，消除逐帧累积误差带来的抖动。
     */
    private fun updateDanmakuPositions() {
        val now = System.currentTimeMillis()
        var deltaMs = now - lastUpdateTime
        if (deltaMs <= 0L) deltaMs = 16L
        // 为了进一步减小单帧位移波动，将时间差限制在 [8ms, 40ms] 区间
        deltaMs = deltaMs.coerceIn(8L, 40L)
        lastUpdateTime = now
        val deltaSec = deltaMs / 1000f

        danmakuQueue.forEach { danmaku ->
            if (!danmaku.isVisible) return@forEach
            // 垂直向上匀速移动：基于创建时间的绝对 elapsed，避免受帧率波动影响
            val elapsedSec = ((now - danmaku.createdAt).coerceAtLeast(0L)) / 1000f
            // 默认速度调低到 180px/s，整体更加平滑
            val baseSpeed = if (danmaku.speed > 0f) danmaku.speed else 250f // px/s，缺省速度
            val targetY = danmaku.startY - baseSpeed * elapsedSec
            // 单帧最大步长，不再放大 1.3 倍，直接按理论位移限制，进一步压制“某一帧跳太多”
            val maxStep = baseSpeed * deltaSec
            val diff = targetY - danmaku.y
            val step = when {
                diff > maxStep -> maxStep
                diff < -maxStep -> -maxStep
                else -> diff
            }
            danmaku.y += step
//            danmaku.y = danmaku.startY - baseSpeed * elapsedSec

            // 计算透明度：靠近顶部时逐渐变淡
            val fadeStartY = height * 0.3f
            if (danmaku.y <= fadeStartY) {
                // 从 fadeStartY 开始到完全离开屏幕这一段做线性透明
                val totalFadeDistance = fadeStartY + danmaku.height
                val traveled = (fadeStartY - danmaku.y).coerceAtLeast(0f)
                val progress = (traveled / totalFadeDistance).coerceIn(0f, 1f)
                danmaku.alpha = 1f - progress
            } else {
                danmaku.alpha = 1f
            }

            // 移除超出屏幕的弹幕
            if (danmaku.y < -danmaku.height) {
                danmaku.isVisible = false
                // 回收到对象池
                recycleDanmaku(danmaku)
            }
        }

        // 清理隐藏的弹幕
        danmakuQueue.removeAll { !it.isVisible }
    }

    /**
     * 绘制所有弹幕
     */
    private fun drawDanmakus(canvas: Canvas) {
        danmakuQueue.forEach { danmaku ->
            if (!danmaku.isVisible) return@forEach

            drawSingleDanmaku(canvas, danmaku)
        }
    }

    /**
     * 绘制单条弹幕
     */
    private fun drawSingleDanmaku(canvas: Canvas, danmaku: DanmakuItem) {
        val x = 16f * resources.displayMetrics.density // 左边距
        val y = danmaku.y

        // 1. 绘制背景
        backgroundPaint.color = danmaku.backgroundColor
        backgroundPaint.alpha = (danmaku.alpha * 255).toInt()
        rect.set(x, y, x + danmaku.width, y + danmaku.height)
        canvas.drawRoundRect(
            rect, 8f * resources.displayMetrics.density,
            8f * resources.displayMetrics.density, backgroundPaint
        )

        // 2. 绘制头像（如果有）
        var textX = x + danmakuPadding
        if (danmaku.avatarBitmap != null) {
            val avatarSize = danmaku.height - danmakuPadding * 2
            val avatarRect = RectF(
                x + danmakuPadding / 2,
                y + danmakuPadding / 2,
                x + danmakuPadding / 2 + avatarSize,
                y + danmakuPadding / 2 + avatarSize
            )
            canvas.drawBitmap(danmaku.avatarBitmap!!, null, avatarRect, paint)
            textX += avatarSize + danmakuPadding / 2
        }

        // 3. 绘制文字
        textPaint.color = danmaku.textColor
        textPaint.textSize = danmaku.textSize * resources.displayMetrics.density
        textPaint.alpha = (danmaku.alpha * 255).toInt()

        val textY = y + danmaku.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(danmaku.content, textX, textY, textPaint)

        // 4. 绘制文字后图片（本地或远程 Bitmap，大小可配置）
        val trailingBitmap = danmaku.trailingImageBitmap
        if (trailingBitmap != null && !trailingBitmap.isRecycled) {
            val density = resources.displayMetrics.density
            val gapPx = 4f * density
            val imgW = (if (danmaku.trailingImageWidthDp > 0) danmaku.trailingImageWidthDp else 24f) * density
            val imgH = (if (danmaku.trailingImageHeightDp > 0) danmaku.trailingImageHeightDp else 24f) * density
            val textWidth = textPaint.measureText(danmaku.content)
            val imgLeft = textX + textWidth + gapPx
            val imgTop = y + (danmaku.height - imgH) / 2f
            rect.set(imgLeft, imgTop, imgLeft + imgW, imgTop + imgH)
            paint.alpha = (danmaku.alpha * 255).toInt()
            canvas.drawBitmap(trailingBitmap, null, rect, paint)
        }
    }

    /**
     * 回收弹幕到对象池
     */
    private fun recycleDanmaku(danmaku: DanmakuItem) {
        danmaku.trailingImageBitmap = null
        danmaku.iconResId = null
        if (danmakuPool.size < 20) {
            danmakuPool.offer(danmaku)
        }
    }

    /**
     * 更新 FPS 统计
     */
    private fun updateFPS() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFpsTime >= 1000) {
            currentFPS = frameCount
            frameCount = 0
            lastFpsTime = currentTime
            // Log.d("Danmaku", "FPS: $currentFPS")
        }
    }

    /**
     * 清空所有弹幕
     */
    fun clearAll() {
        danmakuQueue.forEach { recycleDanmaku(it) }
        danmakuQueue.clear()
        pendingQueue.clear()
    }

    /**
     * 设置最大显示数量
     */
    fun setMaxVisibleCount(count: Int) {
//        maxVisibleCount = count
    }

    /**
     * 根据设备性能动态调整
     */
    fun adaptToDevicePerformance() {
        // 检测设备性能
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val memoryMB = Runtime.getRuntime().maxMemory() / 1024 / 1024

        // 根据性能调整参数
        when {
            cpuCores >= 8 && memoryMB >= 4096 -> {
                // 高性能设备
                targetFPS = 60
//                maxVisibleCount = 10
            }

            cpuCores >= 4 && memoryMB >= 2048 -> {
                // 中等性能
                targetFPS = 60
//                maxVisibleCount = 6
            }

            else -> {
                // 低性能设备
                targetFPS = 30
//                maxVisibleCount = 4
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRendering()
        clearAll()
        danmakuPool.clear()
    }
}