package com.cboda.intelligent.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/8/15.
 */
class GlideLoaderBroadHelper {
    companion object {
        fun loadUrl(context: Context?, url: String, imageView: ImageView,placeId:Int = 0,isCenterCrop:Boolean = true) {
            context?.apply {
                if (isCenterCrop){
                    Glide.with(this).load(url).placeholder(placeId).centerCrop().into(imageView)
                } else {
                    Glide.with(this).load(url).placeholder(placeId).into(imageView)
                }
            }
        }

        /**
         * 圆形
         */
        fun loadCircleUrl(context: Context?, url: String, imageView: ImageView,placeId: Int = 0) {
            context?.apply {
                val trans = RequestOptions.circleCropTransform()
                Glide.with(this).load(url).apply(trans).placeholder(placeId).into(imageView)
            }
        }

        /**
         * 圆形边框
         */
        fun loadCircleWithBorderUrl(
            context: Context?,
            url: String,
            imageView: ImageView,
            borderSize: Int,
            borderColor: Int
        ) {
            context?.apply {
                val trans = CropCircleWithBorderTransformation(borderSize, borderColor)
                Glide.with(this).load(url).apply(RequestOptions.bitmapTransform(trans))
                    .into(imageView)
            }
        }

        /**
         * 圆角居中裁剪
         */
        fun loadCenterCopRoundedCornersUrl(
            context: Context?,
            url: String,
            imageView: ImageView,
            radius: Int,
            placeId:Int = 0
        ) {
            context?.apply {
                val trans = MultiTransformation(CenterCrop(), RoundedCorners(radius))
                Glide.with(this).load(url).apply(RequestOptions.bitmapTransform(trans))
                    .placeholder(placeId).into(imageView)
            }
        }

        /**
         * 圆角
         */
        fun loadRoundedCornersUrl(
            context: Context?,
            url: String,
            imageView: ImageView,
            radius: Int, margin: Int, cornerType: RoundedCornersTransformation.CornerType
        ) {
            context?.apply {
                val multi = MultiTransformation(
                    CenterCrop(),
                    RoundedCornersTransformation(radius, margin, cornerType)
                )
                Glide.with(this).load(url).apply(RequestOptions.bitmapTransform(multi))
                    .into(imageView)
            }
        }

//        fun loadWebpImage(draweeView: SimpleDraweeView?, url: String?) {
//            if (draweeView?.tag == url)return
//            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
//                .setUri(url)
//                .setAutoPlayAnimations(true)
//                .setOldController(draweeView?.controller)
//                .setControllerListener(object :
//                    BaseControllerListener<ImageInfo?>() {
//                    override fun onFinalImageSet(
//                        id: String,
//                        imageInfo: ImageInfo?,
//                        animatable: Animatable?
//                    ) {
//                        super.onFinalImageSet(id, imageInfo, animatable)
//                        draweeView?.tag = url
//                    }
//                })
//                .build()
//            draweeView?.controller = controller
//        }
    }
}