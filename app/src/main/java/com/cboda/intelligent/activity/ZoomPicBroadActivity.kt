package com.cboda.intelligent.activity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cboda.select.picture.photoview.PhotoView
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.bean.CustomViewsBroadInfo
import com.cboda.intelligent.utils.AppBroadUtils
import com.stx.xhb.xbanner.XBanner

/**
 * 简述：看大图
 * Created by ysh on 2023/3/23.
 */
class ZoomPicBroadActivity : BaseSimpleBroadActivity() {

    private var imgUrl: ArrayList<String>? = null
    private var isLoca = false
    private var position = 0
//    private var dataSouce: DataSource<CloseableReference<CloseableImage>>? = null
    private var imageUrls = arrayListOf<CustomViewsBroadInfo>()
    private lateinit var xBanner:
            XBanner

    private fun initViews() {
        xBanner = findViewById(R.id.xBanner)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_pic_zoom
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun useStatusBarDarkFont(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        position = intent?.getIntExtra("position", 0) ?: 0
        imgUrl = intent?.getStringArrayListExtra("imgUrl")
        isLoca = intent?.getBooleanExtra("isLoca", false) ?: false
        imageUrls.clear()
        imgUrl?.forEachIndexed { _, s ->
            imageUrls.add(CustomViewsBroadInfo(s))
        }
        xBanner?.setBannerData(R.layout.image_photo_view, imageUrls)
        xBanner?.loadImage { _, model, view, _ ->
            try {
                val uri = Uri.parse((model as CustomViewsBroadInfo).xBannerUrl)
                if (model.xBannerUrl.startsWith("file://")){
                    (view as PhotoView).setImageURI(uri)
                } else {
                    loadNet((model as CustomViewsBroadInfo).xBannerUrl,view)
                }
            }catch (e:Exception){
                AppBroadUtils.debug("加载大图异常",e.toString())
            }


        }

        xBanner?.setOnItemClickListener { _, _, _, _ ->
            onBackPressed()
        }

        xBanner?.viewPager?.currentItem = position

    }

    private fun loadNet(uri:String,view:View){
        Glide.with(this).asBitmap().load(uri).into(object : SimpleTarget<Bitmap>(){
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                try {
                    resource?.apply {
                        if (!this.isRecycled){
                            (view as PhotoView).setImageBitmap(this)
                        }
                    }
                } catch (e: Exception) {
                    AppBroadUtils.debug("看大图异常", e.toString())
                }
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
//        dataSouce?.close()
    }
}