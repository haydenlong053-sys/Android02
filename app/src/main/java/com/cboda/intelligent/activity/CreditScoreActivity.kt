package com.cboda.intelligent.activity

import android.graphics.Color
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.CreditScoreActivityBinding
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.fragment.CreditScoreComposeFragment
import com.cboda.intelligent.fragment.CreditScoreRecordFragment
import com.cboda.intelligent.widge.BadgeIndicatorAdapter

class CreditScoreActivity : BaseSimple2BroadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = (binding as? CreditScoreActivityBinding)
        getToolbarTitle()?.text = getString(R.string.credit_score_title)
        initHeader()
        initViewPagerAndIndicator()
    }

    private var mBind: CreditScoreActivityBinding? = null
    override fun getViewBindingClass() = CreditScoreActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true

    private fun initHeader() {
        mIvBgTop.setImageResource(R.mipmap.credit_score_back)
        // 示例数据（后续接接口时替换）
        mBind?.tvName?.text = "HI,JOY Follow"
        mBind?.tvSubTitle?.text = "信用不错，再接再厉！"
        mBind?.tvScore?.text = "330.2"
        // CircleProgressBar：进度范围 0-100（示例按 330.2/500 ≈ 66%）
        mBind?.circleProgress?.progress = 66
    }

    private fun initViewPagerAndIndicator() {
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(CreditScoreComposeFragment.newInstance())
        vp2FragmentAdapter.addFragment(CreditScoreRecordFragment.newInstance())

        mBind?.viewPager?.adapter = vp2FragmentAdapter
        mBind?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBind?.viewPager?.isUserInputEnabled = true
        mBind?.viewPager?.offscreenPageLimit = vp2FragmentAdapter.getData().size
        mBind?.viewPager?.isSaveEnabled = false

        val titleResIds = intArrayOf(
            R.string.credit_score_tab_compose,
            R.string.credit_score_tab_record
        )
        val badgeNums = intArrayOf(0, 0)
        mBind?.magicIndicator?.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapter = BadgeIndicatorAdapter(
            this,
            titleResIds,
            badgeNums,
            com.cboda.baselibrary.R.dimen.dp_16
        )
        commonNavigator.adapter = indicatorAdapter
        mBind?.magicIndicator?.navigator = commonNavigator
        bindIndicatorAndViewPager(mBind!!.magicIndicator, mBind!!.viewPager, indicatorAdapter)
    }
}