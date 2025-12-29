package com.base.commonlibrary.utils


import android.content.Context
import android.widget.ImageView
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.utils.ActivityCompatHelper

class GlideEngine private constructor() : ImageEngine {
    /**
     * 加载图片
     *
     * @param context   上下文
     * @param url       资源url
     * @param imageView 图片承载控件
     */
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.loadImg(url)
    }

    override fun loadImage(
        context: Context,
        imageView: ImageView,
        url: String,
        maxWidth: Int,
        maxHeight: Int
    ) {
        imageView.loadImg(url, maxWidth, maxHeight)
    }

    /**
     * 加载指定url并返回bitmap
     *
     * @param context   上下文
     * @param url       资源url
     * @param maxWidth  资源最大加载尺寸
     * @param maxHeight 资源最大加载尺寸
     * @param call      回调接口
     */
//    fun loadImageBitmap(
//        context: Context,
//        url: String,
//        maxWidth: Int,
//        maxHeight: Int,
//        call: OnCallbackListener<Bitmap?>?
//    ) {
//        if (!ActivityCompatHelper.assertValidRequest(context)) {
//            return
//        }
//        GlideApp.with(context)
//            .asBitmap()
//            .override(maxWidth, maxHeight)
//            .load(url)
//            .into(object : CustomTarget<Bitmap?>() {
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    transition: Transition<in Bitmap?>?
//                ) {
//                    call?.onCall(resource)
//                }
//
//                override fun onLoadFailed(errorDrawable: Drawable?) {
//                    call?.onCall(null)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {}
//
//            })
//    }

    /**
     * 加载相册目录封面
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        imageView.loadBitmap(url, 180, 180, 0.5f, 8f)
    }

    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
//        GlideApp.with(context)
//            .load(url)
//            .override(200, 200)
//            .centerCrop()
//            .into(imageView)
        imageView.loadImg(url, 200, 200)
    }

    override fun pauseRequests(context: Context) {
        glidePauseRequests(context)
    }

    override fun resumeRequests(context: Context) {
        glideResumeRequests(context)
    }

    companion object {
        private var instance: GlideEngine? = null
        fun createGlideEngine(): GlideEngine? {
            if (null == instance) {
                synchronized(GlideEngine::class.java) {
                    if (null == instance) {
                        instance = GlideEngine()
                    }
                }
            }
            return instance
        }
    }
}
