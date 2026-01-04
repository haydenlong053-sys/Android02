package com.lxkj.aiim.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.cboda.baselibrary.utils.CompressionBroadUtil.imgCompression
import com.luck.picture.lib.entity.LocalMedia
import com.lxkj.aiim.R
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelUtils
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.ArrayList

class SinglePicSelectHolder(
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
        val itemView = View.inflate(context, R.layout.item_single_pic_layout, null)
        ivSinglePic = itemView.findViewById(R.id.ivSinglePic)
        val params = ivSinglePic?.layoutParams
        params?.width = PixelUtils.dip2px(context, width)
        params?.height = PixelUtils.dip2px(context, height)
        ivSinglePic?.layoutParams = params

        if (red != 0) {
            ivSinglePic?.setImageResource(red)
        }

        ivSinglePic?.setOnClickListener {
            openGallery()
        }
        paraent?.addView(itemView)
    }

    private var imagePickerHolder: ImagePickerHolder? = null
    var card1File: File? = null
    private fun openGallery() {
        if (imagePickerHolder == null) {
            imagePickerHolder = ImagePickerHolder(context)
            imagePickerHolder?.callBack = object : ImagePickerHolder.OnResultCallback {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    val resultList: MutableList<LocalMedia>? = result
                    if (resultList?.isNotEmpty() == true) {
                        if (isCircle) {
                            GlideLoaderHelper.loadCircleUrl(
                                context,
                                "file://${resultList[0].realPath}",
                                ivSinglePic!!,
                            )
                        } else {
                            GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                                context,
                                "file://${resultList[0].realPath}",
                                ivSinglePic!!,
                                PixelUtils.dip2px(context, conner)
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
            GlideLoaderHelper.loadCircleUrl(
                context,
                url,
                ivSinglePic!!,
            )
        } else {
            GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                context,
                url,
                ivSinglePic!!,
                PixelUtils.dip2px(context, conner)
            )
        }
    }

}