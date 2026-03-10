package com.cboda.intelligent.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.cboda.baselibrary.bean.DanmakuItem
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.TestActivityBinding
import com.cboda.intelligent.utils.showToastMain

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
        mBinding?.addData?.setOnClickListener {
            count++
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