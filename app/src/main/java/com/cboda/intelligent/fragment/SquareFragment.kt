package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.SquareFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.widge.TextIndicatorAdapter

class SquareFragment : BaseBroadFragment<SquareFragmentBinding, CleverBodyViewBroadModel>() {

    override fun initView() {
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(SquareChildFragment())
        vp2FragmentAdapter.addFragment(SquareChildFragment())
        vp2FragmentAdapter.addFragment(SquareChildFragment())
        mBinding?.sqViewPager?.viewPager2?.adapter = vp2FragmentAdapter
        mBinding?.sqViewPager?.viewPager2?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding?.sqViewPager?.viewPager2?.isUserInputEnabled = false
        mBinding?.sqViewPager?.viewPager2?.offscreenPageLimit = 1//vp2FragmentAdapter.getData().size
        mBinding?.sqViewPager?.viewPager2?.isSaveEnabled = false
        val mResIds =
            intArrayOf(com.cboda.baselibrary.R.string.square_world, com.cboda.baselibrary.R.string.square_hot, com.cboda.baselibrary.R.string.square_my_attention)
        val commonNavigator = CommonBroadNavigator(requireContext())
        commonNavigator.isAdjustMode = false
        commonNavigator.isSkimOver = true
        val indicatorAdapt = TextIndicatorAdapter(requireContext(), mResIds, com.cboda.baselibrary.R.dimen.dp_14, R.layout.text_indicator_tv, 24.0)
        commonNavigator.adapter = indicatorAdapt
        mBinding?.sqMagicIndicator?.navigator = commonNavigator
        mBinding?.sqMagicIndicator?.also { magicIndicator ->
            mBinding?.sqViewPager?.viewPager2?.also { viewPager ->
                bindIndicatorAndViewPager(magicIndicator, viewPager, indicatorAdapt, magicIndicatorNeedOnPageScrolled = true)
            }
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = SquareFragmentBinding.inflate(inflater, parent, false)
}