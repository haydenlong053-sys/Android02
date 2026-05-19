package com.cboda.intelligent.activity

import android.graphics.Color
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.AiActivityBinding
import com.cboda.intelligent.fragment.AiAllFragment
import com.cboda.intelligent.fragment.AiAllFragment.Companion.DOCUMENT_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.IMAGE_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.VIDEO_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.VOICE_TYPE
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.widge.ImgIndicatorBroadAdapter

class AiActivity : BaseSimple2BroadActivity() {
    private var mBinding: AiActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? AiActivityBinding)
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(DOCUMENT_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(IMAGE_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(VOICE_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(VIDEO_TYPE))

        mBinding?.viewPager?.adapter = vp2FragmentAdapter
        mBinding?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding?.viewPager?.isUserInputEnabled = true
        mBinding?.viewPager?.offscreenPageLimit = vp2FragmentAdapter.getData().size
        mBinding?.viewPager?.isSaveEnabled = false
        val mResIds =
            intArrayOf(R.string.ai_document, R.string.ai_img, R.string.ai_audio, R.string.ai_video)
        val mResIds2 = intArrayOf(
            R.mipmap.icon_ai_broad_document,
            R.mipmap.icon_ai_broad_img,
            R.mipmap.icon_ai_broad_audio,
            R.mipmap.icon_ai_broad_video
        )
        mBinding?.aiMagicIndicator?.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(this@AiActivity)
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = ImgIndicatorBroadAdapter(this@AiActivity, mResIds, mResIds2)
        commonNavigator.adapter = indicatorAdapt
        mBinding?.aiMagicIndicator?.navigator = commonNavigator
        mBinding?.aiMagicIndicator?.let { aiMagicIndicator ->
            mBinding?.viewPager?.let { viewPager ->
                bindIndicatorAndViewPager(
                    aiMagicIndicator,
                    viewPager,
                    indicatorAdapt,
                    commonNavigatorAdapterCanResult = true
                )
            }
        }
        mBinding?.rightView?.click {
            JumpBroadUtils.toAiCheckRecordAct(this@AiActivity)
        }
        mBinding?.leftView?.click {
            finish()
        }
    }

    override fun getViewBindingClass() = AiActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
}