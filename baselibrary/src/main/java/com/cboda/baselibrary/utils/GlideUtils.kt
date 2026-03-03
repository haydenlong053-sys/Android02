package com.cboda.baselibrary.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.blankj.utilcode.util.FileUtils.getFileExtension
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cboda.baselibrary.R

fun imageScale(bitmap: Bitmap, dstW: Int, dstH: Int): Bitmap {
    val srcW = bitmap.width
    val srcH = bitmap.height
    val scaleW = dstW / srcW
    val scaleH = dstH / srcH
    val matrix = Matrix()
    matrix.postScale(scaleW.toFloat(), scaleH.toFloat())
    return Bitmap.createBitmap(
        bitmap, 0, 0, srcW, srcH, matrix,
        true
    )
}


fun ImageView.loadDiyImg2(
    url: Any?,
    mWidth: Int = 0,
    mHeight: Int = 0,
    bitmapWidth: Int?,
    bitmapHeight: Int?,
    thumbnail: Bitmap?,
    backInvoke: () -> Unit = {}
) {
    if (null != thumbnail) {
        val thumbnailRequest = Glide
            .with(this.context)
            .load(thumbnail)
        this@loadDiyImg2.loadImg(url, thumbnailRequest)
        if (null != bitmapWidth && null != bitmapHeight && bitmapWidth > 0 && bitmapHeight > 0) {
            val (lpWidth, lpHeight) =
                if (mWidth == 0 && mHeight == 0) {
                    bitmapWidth to bitmapHeight
                } else if (bitmapWidth > mWidth && mWidth != 0 && bitmapHeight > mHeight && mHeight != 0) {
                    val height1 = mWidth * bitmapHeight / bitmapWidth
                    if (height1 > mHeight) {
                        val width1 = mHeight * bitmapWidth / bitmapHeight
                        width1 to mHeight
                    } else {
                        mWidth to height1
                    }
                } else if (bitmapWidth > mWidth && mWidth != 0) {
                    mWidth to mWidth * bitmapHeight / bitmapWidth
                } else if (bitmapHeight > mHeight && mHeight != 0) {
                    mHeight * bitmapWidth / bitmapHeight to mHeight
                } else {
                    if (bitmapWidth > bitmapHeight) {
                        mWidth to bitmapHeight * mWidth / bitmapWidth
                    } else {
                        bitmapWidth * mHeight / bitmapHeight to mHeight
                    }
//                    bitmapWidth to bitmapHeight
                }
            val lp = this@loadDiyImg2.layoutParams
            if (lpWidth != lp.width || lpHeight != lp.height) {
                lp.width = lpWidth
                lp.height = lpHeight
                this@loadDiyImg2.layoutParams = lp
            }
        } else {
            Glide.with(this)
                .load(url)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        val (bitmapWidth2, bitmapHeight2) = if (resource is BitmapDrawable) {
                            resource.bitmap.width to resource.bitmap.height
                        } else {
                            resource.intrinsicWidth to resource.intrinsicHeight
                        }
                        if (bitmapWidth2 > 0 && bitmapHeight2 > 0) {
                            val (lpWidth, lpHeight) =
                                if (mWidth == 0 && mHeight == 0) {
                                    bitmapWidth2 to bitmapHeight2
                                } else if (bitmapWidth2 > mWidth && mWidth != 0 && bitmapHeight2 > mHeight && mHeight != 0) {
                                    val height1 = mWidth * bitmapHeight2 / bitmapWidth2
                                    if (height1 > mHeight) {
                                        val width1 = mHeight * bitmapWidth2 / bitmapHeight2
                                        width1 to mHeight
                                    } else {
                                        mWidth to height1
                                    }
                                } else if (bitmapWidth2 > mWidth && mWidth != 0) {
                                    mWidth to mWidth * bitmapHeight2 / bitmapWidth2
                                } else if (bitmapHeight2 > mHeight && mHeight != 0) {
                                    mHeight * bitmapWidth2 / bitmapHeight2 to mHeight
                                } else {
                                    bitmapWidth2 to bitmapHeight2
                                }
                            val lp = this@loadDiyImg2.layoutParams
                            if (lpWidth != lp.width || lpHeight != lp.height) {
                                lp.width = lpWidth
                                lp.height = lpHeight
                                this@loadDiyImg2.layoutParams = lp
                            }
                        }
                        backInvoke.invoke()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        backInvoke.invoke()
                    }
                })
        }
    } else {
        Glide.with(this)
            .load(url)
            .thumbnail(0.1f)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    val (bitmapWidth2, bitmapHeight2) = if (resource is BitmapDrawable) {
                        resource.bitmap.width to resource.bitmap.height
                    } else {
                        resource.intrinsicWidth to resource.intrinsicHeight
                    }
                    if (bitmapWidth2 > 0 && bitmapHeight2 > 0) {
                        val (lpWidth, lpHeight) =
                            if (mWidth == 0 && mHeight == 0) {
                                bitmapWidth2 to bitmapHeight2
                            } else if (bitmapWidth2 > mWidth && mWidth != 0 && bitmapHeight2 > mHeight && mHeight != 0) {
                                val height1 = mWidth * bitmapHeight2 / bitmapWidth2
                                if (height1 > mHeight) {
                                    val width1 = mHeight * bitmapWidth2 / bitmapHeight2
                                    width1 to mHeight
                                } else {
                                    mWidth to height1
                                }
                            } else if (bitmapWidth2 > mWidth && mWidth != 0) {
                                mWidth to mWidth * bitmapHeight2 / bitmapWidth2
                            } else if (bitmapHeight2 > mHeight && mHeight != 0) {
                                mHeight * bitmapWidth2 / bitmapHeight2 to mHeight
                            } else {
                                bitmapWidth2 to bitmapHeight2
                            }
                        val lp = this@loadDiyImg2.layoutParams
                        if (lpWidth != lp.width || lpHeight != lp.height) {
                            lp.width = lpWidth
                            lp.height = lpHeight
                            this@loadDiyImg2.layoutParams = lp
                        }
                    }
                    this@loadDiyImg2.loadImg(url)
                    backInvoke.invoke()
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    backInvoke.invoke()
                }
            })
    }

}

//适配动态宽高图片
fun ImageView.loadDiyImg(
    url: Any?,
    mWidth: Int = 0,
    mHeight: Int = 0,
    backInvoke: () -> Unit = {}
) {
//    eLogD("loadDiyImgClass", "outWidth:${mWidth} ,outHeight:${mHeight}")
    Glide.with(getAppContext(this.context)).asBitmap().load(url ?: "")
        .into(object : CustomTarget<Bitmap?>() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                backInvoke.invoke()
            }

            override fun onResourceReady(
                resource: Bitmap,
                transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
            ) {
                val bitmapWidth = resource.width
                val bitmapHeight = resource.height
//                eLogD("loadDiyImgClass", "bitmapWidth-------$bitmapWidth")//3024
//                eLogD("loadDiyImgClass", "bitmapHeight-------$bitmapHeight")//4032
                if (bitmapWidth > 0 && bitmapHeight > 0) {
                    val lp = this@loadDiyImg.layoutParams
                    if (mWidth == 0 && mHeight == 0) {
                        lp.width = bitmapWidth
                        lp.height = bitmapHeight
                    } else if (bitmapWidth > mWidth && mWidth != 0 && bitmapHeight > mHeight && mHeight != 0) {
                        val height1 = mWidth * bitmapHeight / bitmapWidth
//                        eLogD("loadDiyImgClass", "height1-------$height1")
                        if (height1 > mHeight) {
                            val width1 = mHeight * bitmapWidth / bitmapHeight
//                            eLogD("loadDiyImgClass", "width1-------$width1")
                            lp.width = width1
                            lp.height = mHeight
                        } else {
                            lp.width = mWidth
                            lp.height = height1
                        }
                    } else if (bitmapWidth > mWidth && mWidth != 0) {
                        lp.width = mWidth
                        lp.height = mWidth * bitmapHeight / bitmapWidth
                    } else if (bitmapHeight > mHeight && mHeight != 0) {
                        lp.width = mHeight * bitmapWidth / bitmapHeight
                        lp.height = mHeight
                    } else {
                        lp.width = bitmapWidth
                        lp.height = bitmapHeight
                    }
                    this@loadDiyImg.layoutParams = lp
                }
                this@loadDiyImg.loadImg(url)
                backInvoke.invoke()
            }


            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}

fun ImageView.loadImage(url: String?, width: Int, height: Int) {
    val fileExt = getFileExtension(url ?: "")
    if (fileExt == "gif" || fileExt == "GIF") {
        this.loadGifImage(url, width, height)
    } else {
        this.loadImg(url, width, height)
    }
}

fun ImageView.loadGifImage(any: Any?, width: Int, height: Int) {
    try {
        Glide.with(this.context).asGif().load(any).override(width, height)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .placeholder(defaultPlaceholder).error(defaultErrorImg)
            .into(this)
    } catch (e: Exception) {
//        e.toLog(tAG)
    }
}


fun ImageView.loadImg(url: String?) {
    if (!TextUtils.isEmpty(url)) {
        var mContext = this.context
        if (mContext is Activity) {
            mContext = this.context as Activity
            if (mContext.isFinishing || mContext.isDestroyed) {
                return
            }
        }else if (windowToken == null) {
            return
        }

        if (url!!.endsWith("gif")) {
            // 使用Glide播放gif图片
            Glide.with(mContext)
                .asGif()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        } else {
            Glide.with(mContext).load(url).apply(RequestOptions().error(R.mipmap.ic_head_broad_defaut).placeholder(R.mipmap.ic_head_broad_defaut)).into(this)
        }
    } else {
        this.setImageResource(R.mipmap.ic_head_broad_defaut)
    }
}

fun ImageView.loadImg(any: Any?, width: Int, height: Int) {
    try {
        Glide.with(this.context).load(any).override(width, height)
            .diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(this)
    } catch (e: Exception) {
//        e.toLog(tAG)
    }
}

fun ImageView.loadImg(any: Any?, thumb: RequestBuilder<Drawable>? = null) {
    try {
        any ?: return
        if (any is String && TextUtils.isEmpty(any)) {
            return
        }
        val requestBuilder = Glide.with(this.context).load(any)
        if (null != thumb) {
            requestBuilder.thumbnail(thumb)
        }
        requestBuilder.diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(this)
    } catch (e: Exception) {

    }
}


fun glidePauseRequests(context: Context?) {
    try {
        Glide.with(getAppContext(context)).pauseRequests()
    } catch (e: Exception) {
//        e.toLog(tAG)
    }
}


fun glideResumeRequests(context: Context?) {
    try {
        Glide.with(getAppContext(context)).resumeRequests()
    } catch (e: Exception) {
//        e.toLog(tAG)
    }
}


fun ImageView.loadBitmap(
    any: Any?,
    width: Int,
    height: Int,
    sizeMultiplier: Float,
    cornerRadius: Float,
) {
    try {
        Glide.with(this.context)
            .asBitmap()
            .load(any)
            .override(width, height)
            .centerCrop()
            .sizeMultiplier(sizeMultiplier)
            .into(object : BitmapImageViewTarget(this) {
                override fun setResource(resource: Bitmap?) {
                    try {
                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                            this@loadBitmap.context.resources,
                            resource
                        )
                        circularBitmapDrawable.cornerRadius = cornerRadius
                        this@loadBitmap.setImageDrawable(circularBitmapDrawable)
                    } catch (e: Exception) {
//                        e.toLog(tAG)
                    }
                }
            })
    } catch (e: Exception) {
//        e.toLog(tAG)
    }
}

fun loadMedia(context: Context, thumbnail: Bitmap, imagePath: String?, imageView: ImageView) {
    val placeholderOptions = RequestOptions()
    placeholderOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
    val thumbnailRequest: RequestBuilder<Drawable?>? = Glide
        .with(context)
        .load(thumbnail)
    val request = Glide.with(context)
        .load(imagePath)
        .thumbnail(thumbnailRequest)
        .apply(placeholderOptions)

    request.into(imageView)
}

/**
 * 通过 Glide 获取网络图片链接的 Bitmap（回调版本）
 * @param context 上下文
 * @param imageUrl 图片网络链接
 * @param width 目标宽度（可选，0 表示不限制）
 * @param height 目标高度（可选，0 表示不限制）
 * @param callback 回调函数，成功时返回 Bitmap，失败时返回 null
 */
fun getBitmapFromUrl(
    context: Context,
    imageUrl: String?,
    callback: (Bitmap?) -> Unit
) {
    if (imageUrl.isNullOrEmpty()) {
        callback(null)
        return
    }
// 获取 Bitmap
    Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                // 在这里使用 Bitmap
                // resource 就是获取到的 Bitmap
                callback.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // 当清除时调用
            }
        })
}