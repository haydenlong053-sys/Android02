package com.cboda.intelligent.widge

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.VideoPlayOnlyBroadActivity
import com.cboda.intelligent.utils.GlideBroadUtil
import com.makeramen.roundedimageview.RoundedImageView
import cc.shinichi.library.ImagePreview
import java.util.WeakHashMap

/**
 * 广场帖子图片九宫格布局：
 * - 图片必须为正方形
 * - 1 张：宽度 = 容器宽度
 * - 2 张：两张 + 间距 = 容器宽度
 * - 3 张：三张 + 间距 = 容器宽度
 * - 4+ 张：按 3 列网格分行，末行按 1/2/3 张规则排布（与 UI 稿一致）
 */
object SquarePostImageLayoutHelper {

    private val imageGapPx = SizeUtils.dp2px(8f)
    private val rowGapPx = SizeUtils.dp2px(8f)
    private val cornerRadiusPx = SizeUtils.dp2px(12f).toFloat()
    /** item 左右 margin + padding，各 16dp */
    private val itemHorizontalInsetPx = SizeUtils.dp2px(16f) * 4

    private data class BindState(
        var generation: Int = 0,
        var pendingRunnable: Runnable? = null,
        var layoutListener: View.OnLayoutChangeListener? = null,
        var lastBindKey: String? = null,
        var lastContainerWidth: Int = 0,
    )

    private val bindStates = WeakHashMap<LinearLayout, BindState>()

    fun bindPostMedia(
        container: LinearLayout,
        images: Array<String>?,
        videoUrl: String?,
        enableImagePreview: Boolean = false,
    ) {
        val video = videoUrl?.trim().orEmpty()
        if (video.isNotEmpty()) {
            bindVideo(container, video)
        } else {
            bind(container, images, enableImagePreview)
        }
    }

    fun bindVideo(container: LinearLayout, videoUrl: String) {
        val video = videoUrl.trim()
        if (video.isEmpty()) {
            unbind(container)
            return
        }
        val state = bindState(container)
        cancelPending(container, state)
        container.isVisible = true
        val bindKey = "video|$video"
        val generation = state.generation + 1
        state.generation = generation

        fun tryRender(force: Boolean = false) {
            if (state.generation != generation) return
            val width = resolveContainerWidth(container)
            if (width <= 0) return
            if (!force &&
                state.lastBindKey == bindKey &&
                state.lastContainerWidth == width &&
                container.childCount > 0
            ) {
                return
            }
            state.lastBindKey = bindKey
            state.lastContainerWidth = width
            renderVideo(container, video, width)
        }

        val width = resolveContainerWidth(container)
        if (width > 0) {
            tryRender(force = true)
            return
        }

        val layoutListener = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            if (state.generation != generation) return@OnLayoutChangeListener
            val resolvedWidth = resolveContainerWidth(view)
            if (resolvedWidth <= 0) return@OnLayoutChangeListener
            cancelLayoutListener(container, state)
            tryRender(force = true)
        }
        state.layoutListener = layoutListener
        container.addOnLayoutChangeListener(layoutListener)

        val pendingRunnable = Runnable {
            if (state.generation != generation) return@Runnable
            tryRender(force = true)
        }
        state.pendingRunnable = pendingRunnable
        container.post(pendingRunnable)
    }

    fun openVideoPlayer(context: Context, videoUrl: String) {
        val video = videoUrl.trim()
        if (video.isEmpty()) return
        context.startActivity(
            Intent(context, VideoPlayOnlyBroadActivity::class.java).apply {
                putExtra("video", video)
                putExtra("videoImage", video)
            },
        )
    }

    fun openImagePreview(context: Context, images: List<String>, index: Int) {
        if (images.isEmpty()) return
        val safeIndex = index.coerceIn(0, images.lastIndex)
        ImagePreview.getInstance()
            .setContext(context)
            .setIndex(safeIndex)
            .setImageList(ArrayList(images))
            .start()
    }

    fun bind(
        container: LinearLayout,
        imgS: Array<String>?,
        enableImagePreview: Boolean = false,
    ) {
        val state = bindState(container)
        cancelPending(container, state)

        val validImages = imgS?.filter { it.isNotBlank() }.orEmpty()
        if (validImages.isEmpty()) {
            container.removeAllViews()
            container.isGone = true
            state.lastBindKey = null
            state.lastContainerWidth = 0
            return
        }

        container.isVisible = true
        val bindKey = validImages.joinToString("|")
        val generation = state.generation + 1
        state.generation = generation

        fun tryRender(force: Boolean = false) {
            if (state.generation != generation) return
            val width = resolveContainerWidth(container)
            if (width <= 0) return
            if (!force &&
                state.lastBindKey == bindKey &&
                state.lastContainerWidth == width &&
                container.childCount > 0
            ) {
                return
            }
            state.lastBindKey = bindKey
            state.lastContainerWidth = width
            render(container, validImages, width, enableImagePreview)
        }

        val width = resolveContainerWidth(container)
        if (width > 0) {
            tryRender(force = true)
            return
        }

        val layoutListener = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            if (state.generation != generation) return@OnLayoutChangeListener
            val resolvedWidth = resolveContainerWidth(view)
            if (resolvedWidth <= 0) return@OnLayoutChangeListener
            cancelLayoutListener(container, state)
            tryRender(force = true)
        }
        state.layoutListener = layoutListener
        container.addOnLayoutChangeListener(layoutListener)

        val pendingRunnable = Runnable {
            if (state.generation != generation) return@Runnable
            tryRender(force = true)
        }
        state.pendingRunnable = pendingRunnable
        container.post(pendingRunnable)
    }

    fun unbind(container: LinearLayout) {
        val state = bindStates[container] ?: return
        cancelPending(container, state)
        container.removeAllViews()
        container.isGone = true
        state.lastBindKey = null
        state.lastContainerWidth = 0
    }

    private fun bindState(container: LinearLayout): BindState {
        return bindStates.getOrPut(container) { BindState() }
    }

    private fun cancelPending(container: LinearLayout, state: BindState) {
        state.pendingRunnable?.let { container.removeCallbacks(it) }
        state.pendingRunnable = null
        cancelLayoutListener(container, state)
        state.generation++
    }

    private fun cancelLayoutListener(container: LinearLayout, state: BindState) {
        state.layoutListener?.let { container.removeOnLayoutChangeListener(it) }
        state.layoutListener = null
    }

    /** 优先用已测量宽度；容器 GONE 时仍可从父布局推算内容区宽度 */
    private fun resolveContainerWidth(container: View): Int {
        if (container.width > 0) return container.width
        val parent = container.parent as? View
        if (parent != null && parent.width > 0) {
            return parent.width - parent.paddingLeft - parent.paddingRight
        }
        return ScreenUtils.getScreenWidth() - itemHorizontalInsetPx
    }

    private fun render(
        container: LinearLayout,
        images: List<String>,
        containerWidth: Int,
        enableImagePreview: Boolean = false,
    ) {
        container.removeAllViews()
        val rows = splitImagesByRows(images)
        var imageIndex = 0
        rows.forEachIndexed { rowIndex, rowImages ->
            val columnCount = rowImages.size
            val itemSize = calculateItemSize(containerWidth, columnCount)
            val rowLayout = LinearLayout(container.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    if (rowIndex > 0) {
                        topMargin = rowGapPx
                    }
                }
            }
            rowImages.forEachIndexed { index, url ->
                val imageView = createImageView(container.context)
                val layoutParams = LinearLayout.LayoutParams(itemSize, itemSize).apply {
                    if (index > 0) {
                        marginStart = imageGapPx
                    }
                }
                rowLayout.addView(imageView, layoutParams)
                GlideBroadUtil.setImagOverSize(
                    container.context,
                    url,
                    imageView,
                    itemSize,
                    itemSize,
                )
                if (enableImagePreview) {
                    val previewIndex = imageIndex
                    imageView.setOnClickListener {
                        openImagePreview(container.context, images, previewIndex)
                    }
                }
                imageIndex++
            }
            container.addView(rowLayout)
        }
    }

    /** 每行图片数量：1 / 2 / 3 */
    internal fun buildRowSpecs(imageCount: Int): List<Int> {
        if (imageCount <= 0) return emptyList()
        if (imageCount <= 9) {
            return when (imageCount) {
                1 -> listOf(1)
                2 -> listOf(2)
                3 -> listOf(3)
                4 -> listOf(3, 1)
                5 -> listOf(3, 2)
                6 -> listOf(3, 3)
                7 -> listOf(3, 3, 1)
                8 -> listOf(3, 3, 2)
                else -> listOf(3, 3, 3)
            }
        }
        val specs = mutableListOf<Int>()
        var remaining = imageCount
        while (remaining > 0) {
            when {
                remaining == 4 -> {
                    specs.add(3)
                    specs.add(1)
                    remaining = 0
                }

                remaining == 2 -> {
                    specs.add(2)
                    remaining = 0
                }

                remaining == 1 -> {
                    specs.add(1)
                    remaining = 0
                }

                else -> {
                    specs.add(3)
                    remaining -= 3
                }
            }
        }
        return specs
    }

    private fun splitImagesByRows(images: List<String>): List<List<String>> {
        val rowSpecs = buildRowSpecs(images.size)
        val rows = mutableListOf<List<String>>()
        var start = 0
        rowSpecs.forEach { columnCount ->
            rows.add(images.subList(start, start + columnCount))
            start += columnCount
        }
        return rows
    }

    private fun renderVideo(container: LinearLayout, videoUrl: String, containerWidth: Int) {
        container.removeAllViews()
        val itemSize = containerWidth
        val frameLayout = FrameLayout(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setOnClickListener {
                openVideoPlayer(container.context, videoUrl)
            }
        }
        val coverView = createImageView(container.context)
        frameLayout.addView(
            coverView,
            FrameLayout.LayoutParams(itemSize, itemSize),
        )
        val playIconSize = SizeUtils.dp2px(40f)
        val playIcon = ImageView(container.context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.mipmap.ic_broad_video)
        }
        frameLayout.addView(
            playIcon,
            FrameLayout.LayoutParams(playIconSize, playIconSize, Gravity.CENTER),
        )
        container.addView(frameLayout)
        GlideBroadUtil.setImagOverSize(
            container.context,
            videoUrl,
            coverView,
            itemSize,
            itemSize,
        )
    }

    private fun calculateItemSize(containerWidth: Int, columnCount: Int): Int {
        if (columnCount <= 1) return containerWidth
        return (containerWidth - imageGapPx * (columnCount - 1)) / columnCount
    }

    private fun createImageView(context: Context): ImageView {
        return RoundedImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setCornerRadius(cornerRadiusPx)
        }
    }
}
