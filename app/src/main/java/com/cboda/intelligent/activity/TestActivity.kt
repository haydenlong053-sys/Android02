package com.cboda.intelligent.activity

import android.os.Bundle
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.DanmakuItem
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.TestActivityBinding

class TestActivity : BaseSimple2BroadActivity() {
    private var mBinding: TestActivityBinding? = null
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? TestActivityBinding)
//        mBinding?.verticalDanmakuView?.imageLoader = { imageView, url ->
//            if (url.isNullOrBlank()) {
//                imageView.visibility = View.GONE
//            } else {
//                imageView.visibility = View.VISIBLE
//                imageView.loadImg(url)
//            }
//        }
//        testDanmaku()
//        for (i in 1..10) { // 包含结束值
//            val danmakuItem = DanmakuItem()
//            danmakuItem.content = "用户000$i: "
//            danmakuItem.isMeSend = true
//            danmakuItem.iconResId = if (i % 2 == 0) R.mipmap.icon_danma_xh else R.mipmap.icon_danma_gz
//            mBinding?.verticalDanmakuView?.addDanmaku(danmakuItem)
//        }
        mBinding?.addData?.setOnClickListener {
// 传统for循环

//            for (i in 1..3) { // 包含结束值
//                val danmakuItem = DanmakuItem()
//                danmakuItem.content = "用户000$i: "
//                danmakuItem.isMeSend = i % 2 == 0
//                danmakuItem.iconResId = if (i % 2 == 0) R.mipmap.icon_danma_xh else R.mipmap.icon_danma_gz
//                mBinding?.verticalDanmakuView?.addDanmaku(danmakuItem)
//            }
            count++
            val danmakuItem = DanmakuItem()
            danmakuItem.content = "用户1111$count: "
            danmakuItem.isMeSend = count % 2 == 0
            danmakuItem.iconResId = if (count % 2 == 0) R.mipmap.icon_danma_xh else R.mipmap.icon_danma_gz
            mBinding?.verticalDanmakuView?.addDanmaku(danmakuItem)
//            mBinding?.verticalDanmakuView?.addDanmaku(
//                DanmakuItem(
//                    text = "用户 ${count}: 这条弹幕带头像",
//                    avatarUrl = "",
////                    showAvatar = true,
////                    textColor = if (count % 5 == 0) Color.YELLOW else Color.WHITE,
//                    isMeSend = count % 5 == 0,
//                    onClick = {
//                        showToastMain("点击了第 $count 条弹幕")
//                    }
//                ))
        }
    }

//    private fun testDanmaku() {
////        val handler = android.os.Handler()
//        // 传统for循环
//        for (count in 1..1000) { // 包含结束值
//
//        }
//    }

    override fun finish() {
        super.finish()
        mBinding?.verticalDanmakuView?.clearAll()
    }

    override fun onResume() {
        super.onResume()
        mBinding?.verticalDanmakuView?.danMaResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding?.verticalDanmakuView?.danMaPause()
    }

    override fun getViewBindingClass() = TestActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
}