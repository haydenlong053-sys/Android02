package com.cboda.intelligent.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.cboda.baselibrary.utils.CompressionBroadUtil.imgCompression
import com.cboda.select.picture.entity.LocalMedia
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import java.io.File
import java.util.ArrayList

class SinglePicSelectBroadHolder(
    private val context: Context,
    private val paraent: ViewGroup?,
    private val isCircle: Boolean = true,
    private val conner: Float = 10f,
    private val width: Float = 100f,
    private val height: Float = 100f,
    private val isCompress: Boolean = true,
    private val red: Int = 0
) {


    private var ivSinglePic: ImageView? = null


    init {

        paraent?.removeAllViews()
        val itemView = View.inflate(context, R.layout.item_single_pic_broad_layout, null)
        ivSinglePic = itemView.findViewById(R.id.ivSinglePic)
        val params = ivSinglePic?.layoutParams
        params?.width = PixelBroadBroadUtils.dip2px(context, width)
        params?.height = PixelBroadBroadUtils.dip2px(context, height)
        ivSinglePic?.layoutParams = params

        if (red != 0) {
            ivSinglePic?.setImageResource(red)
        }

        ivSinglePic?.setOnClickListener {
            openGallery()
        }
        paraent?.addView(itemView)
    }

    private var imagePickerHolder: ImagePickerBroadHolder? = null
    var card1File: File? = null
    private fun openGallery() {
        if (imagePickerHolder == null) {
            imagePickerHolder = ImagePickerBroadHolder(context)
            imagePickerHolder?.callBack = object : ImagePickerBroadHolder.OnResultCallback {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    val resultList: MutableList<LocalMedia>? = result
                    if (resultList?.isNotEmpty() == true) {
                        if (isCircle) {
                            GlideLoaderBroadHelper.loadCircleUrl(
                                context,
                                "file://${resultList[0].realPath}",
                                ivSinglePic!!,
                            )
                        } else {
                            GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                                context,
                                "file://${resultList[0].realPath}",
                                ivSinglePic!!,
                                PixelBroadBroadUtils.dip2px(context, conner)
                            )
                        }

                        if (isCompress) {
                            imgCompression(
                                context,
                                File(resultList[0].realPath)
                            ) { _, compressFile ->
                                compressFile?.apply {
                                    card1File = this
                                }
                            }
                        } else {
                            card1File = File(resultList[0].realPath)
                        }

                    }
                }

                override fun onCancel() {
                }

            }
        }
        imagePickerHolder?.show(isVideo = false, isSingle = true, 1, false)

    }

    fun updateImage(url: String) {
        if (isCircle) {
            GlideLoaderBroadHelper.loadCircleUrl(
                context,
                url,
                ivSinglePic!!,
            )
        } else {
            GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                context,
                url,
                ivSinglePic!!,
                PixelBroadBroadUtils.dip2px(context, conner)
            )
        }
    }

}