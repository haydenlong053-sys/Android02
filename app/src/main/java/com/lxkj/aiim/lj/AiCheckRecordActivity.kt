package com.lxkj.aiim.lj

import android.graphics.Color
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivityCheckResultRecordBinding
import com.lxkj.aiim.fragment.AiRecordFragment
import com.lxkj.aiim.widge.ImgIndicatorAdapter

class AiCheckRecordActivity : BaseSimple2Activity() {
    private var mBind: ActivityCheckResultRecordBinding? = null

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_check_result_record
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityCheckResultRecordBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = binding as? ActivityCheckResultRecordBinding
        setRootBgColor(getColor(com.cboda.baselibrary.R.color.color_F4F8FD))
        mBind?.toolbar?.mTvTitle?.text = getString(R.string.ai_check_record)
        mBind?.toolbar?.mViewBack?.setOnClickListener {
            finish()
        }
        initViewPagerAndIndicator()
    }
    private fun initViewPagerAndIndicator() {
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(AiRecordFragment(0))
        vp2FragmentAdapter.addFragment(AiRecordFragment(1))
        vp2FragmentAdapter.addFragment(AiRecordFragment(2))
        vp2FragmentAdapter.addFragment(AiRecordFragment(3))
        mBind?.viewPager?.adapter = vp2FragmentAdapter
        mBind?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBind?.viewPager?.isUserInputEnabled = true
        mBind?.viewPager?.offscreenPageLimit = vp2FragmentAdapter.getData().size
        mBind?.viewPager?.isSaveEnabled = false
        val mResIds =
            intArrayOf(R.string.ai_document, R.string.ai_img, R.string.ai_audio, R.string.ai_video)
        val mResIds2 = intArrayOf(
            R.mipmap.icon_ai_document,
            R.mipmap.icon_ai_img,
            R.mipmap.icon_ai_audio,
            R.mipmap.icon_ai_video
        )
        mBind?.aiMagicIndicator?.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = ImgIndicatorAdapter(this, mResIds, mResIds2)
        commonNavigator.adapter = indicatorAdapt
        mBind?.aiMagicIndicator?.navigator = commonNavigator
        bindIndicatorAndViewPager(mBind!!.aiMagicIndicator, mBind!!.viewPager, indicatorAdapt)

    }
}