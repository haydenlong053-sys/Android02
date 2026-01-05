package com.cboda.chatui.utils

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.cboda.chatui.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.util.concurrent.Executors

object GroupAvatarLoader {

    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun  loadToImageView( context: Context,
                          urls: List<String>,
                          imageView: ImageView,
                          groupId: String,){
        load(context,urls,imageView,groupId)
    }

    /**
     * @param context 上下文
     * @param urls 群成员头像（最多9个）
     * @param imageView 显示目标
     * @param groupId 群id，用作缓存key
     * @param size 合成头像尺寸（px）
     * @param cornerRadius 圆角大小（px）
     * @param spacing 小头像间距（px）
     * @param placeholder 占位图资源id
     */
    fun load(
        context: Context,
        urls: List<String>,
        imageView: ImageView,
        groupId: String,
        size: Int = 240,
        cornerRadius: Float = 24f,
        spacing: Int = 6,
        placeholder: Int = R.mipmap.ic_group_chat
    ) {
        if (urls.isEmpty()) {
            imageView.setImageResource(placeholder)
            return
        }

        val cacheKey = "group_avatar_$groupId"
        val cacheFile = Glide.with(context)
            .asFile()
            .load(cacheKey)
            .getOrNull()

        // 第一次显示上次缓存（防闪烁）
        Glide.with(context)
            .load(cacheFile ?: placeholder)
            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(cornerRadius.toInt())))
            .into(imageView)

        executor.execute {
            try {
                // 异步加载小头像 Bitmap
                val bitmaps = urls.take(9).mapNotNull { url ->
                    try {
                        Glide.with(context)
                            .asBitmap()
                            .load(url)
                            .submit(size / 3, size / 3)
                            .get()
                    } catch (e: Exception) {
                        null
                    }
                }

                if (bitmaps.isEmpty()) return@execute

                val combined = combine(bitmaps, size, spacing)

                // 应用圆角
                val rounded = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(rounded)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(combined, 0f, 0f, paint)

                mainHandler.post {
                    Glide.with(context)
                        .load(rounded)
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(imageView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 拼接群头像（带灰底与间距）
     */
    private fun combine(bitmaps: List<Bitmap>, size: Int, spacing: Int): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.LTGRAY)

        val count = bitmaps.size
        val grid = when {
            count <= 1 -> 1
            count <= 4 -> 2
            else -> 3
        }

        val cellSize = (size - (grid + 1) * spacing) / grid
        var index = 0

        for (row in 0 until grid) {
            for (col in 0 until grid) {
                if (index >= count) break
                val left = spacing + col * (cellSize + spacing)
                val top = spacing + row * (cellSize + spacing)
                val bmp = Bitmap.createScaledBitmap(bitmaps[index], cellSize, cellSize, true)
                canvas.drawBitmap(bmp, left.toFloat(), top.toFloat(), paint)
                index++
            }
        }
        return result
    }

    private fun <T> com.bumptech.glide.RequestBuilder<T>.getOrNull(): T? {
        return try {
            submit().get()
        } catch (e: Exception) {
            null
        }
    }
}

