package com.cboda.intelligent.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.cboda.baselibrary.bean.DanmakuItem
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.TestActivityBinding
import com.cboda.intelligent.utils.showToastMain

class TestActivity : BaseSimple2BroadActivity() {
    private var mBinding: TestActivityBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? TestActivityBinding)
        mBinding?.verticalDanmakuView?.imageLoader = { imageView, url ->
            if (url.isNullOrBlank()) {
                imageView.visibility = View.GONE
            } else {
                imageView.visibility = View.VISIBLE
                imageView.loadImg(url)
            }
        }
        testDanmaku()
    }

    private fun testDanmaku() {
        val handler = android.os.Handler()
        var count = 0

        handler.post(object : Runnable {
            override fun run() {
                count++


                // 模拟礼物图标弹幕
//                if (count % 3 == 0) {
//                    mBinding?.verticalDanmakuView?.addDanmaku(
//                        DanmakuItem(
//                            avatarUrl = "",
//                            text = "送出 🎁 礼物！",
//                            iconResId = com.cboda.baselibrary.R.mipmap.ic_head_broad_defaut, // 本地礼物图标
//                            textColor = Color.CYAN
//                        )
//                    )
//                } else {
                    mBinding?.verticalDanmakuView?.addDanmaku(
                        DanmakuItem(
                            text = "用户 ${count}: 这条弹幕带头像",
                            avatarUrl = "https://img0.baidu.com/it/u=3582218749,1515921929&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=750",
                            showAvatar = true,
                            textColor = if (count % 5 == 0) Color.YELLOW else Color.WHITE,
                            onClick = {
                                showToastMain("点击了第 $count 条弹幕")
                            }
                        ))
//                }
                if (count <= 16) {
                    handler.postDelayed(this, 500)
                }
            }
        })
    }

    override fun getViewBindingClass() = TestActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
}