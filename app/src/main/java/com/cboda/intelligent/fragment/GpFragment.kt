package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.MagicBroadIndicator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.SquareNoticeActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.GpFragmentBinding
import com.cboda.intelligent.dialog.pop.SquarePublishPopup
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.widge.TextBackIndicatorAdapter

class GpFragment : BaseBroadFragment<GpFragmentBinding, CleverBodyViewBroadModel>() {
    private var marginBottom = 0
    override fun initView() {
        addHeight(binding.shapeView)
        addHeight(binding.userImg)
        addHeight(binding.shapeRightView)
        initViewPagerAndIndicator()
        setBottomViewHeight()
        binding.userImg.loadImg(MMKVBroadUtils.getUserImg())
    }

    override fun initListener() {
        mBinding?.shapeRightView?.click {
            viewModel.startAct<SquareNoticeActivity>()
        }
        mBinding?.gpAddView?.click {
            val squarePublishPopup = SquarePublishPopup(requireContext())
            squarePublishPopup.showPopupWindow()
        }
    }

    fun setBottomHeight(height: Int) {
        marginBottom = height
        setBottomViewHeight()
    }

    private fun setBottomViewHeight() {
        mBinding?.gpView1?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = marginBottom
            it.layoutParams = layoutParams
        }

    }

    private fun initViewPagerAndIndicator() {
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(SquareFragment())
        vp2FragmentAdapter.addFragment(FriendCircleFragment())
        mBinding?.gpViewPagerLayout?.viewPager2?.adapter = vp2FragmentAdapter
        mBinding?.gpViewPagerLayout?.viewPager2?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding?.gpViewPagerLayout?.viewPager2?.isUserInputEnabled = false
        mBinding?.gpViewPagerLayout?.viewPager2?.offscreenPageLimit = 1//vp2FragmentAdapter.getData().size
        mBinding?.gpViewPagerLayout?.viewPager2?.isSaveEnabled = false
        val mResIds =
            intArrayOf(com.cboda.baselibrary.R.string.bottom_bar_gc, com.cboda.baselibrary.R.string.circle_friend)
        val commonNavigator = CommonBroadNavigator(requireContext())
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = TextBackIndicatorAdapter(
            requireContext(), mResIds,
            com.cboda.baselibrary.R.color.color_3B47DD, com.cboda.baselibrary.R.color.black_85,
            com.cboda.baselibrary.R.color.color_3B47DD_12, 30.0, false, false
        )
        commonNavigator.adapter = indicatorAdapt
        mBinding?.gpMagicIndicator?.navigator = commonNavigator
        mBinding?.gpMagicIndicator?.also { magicIndicator ->
            mBinding?.gpViewPagerLayout?.viewPager2?.also { viewPager ->
                bindIndicatorAndViewPager(magicIndicator, viewPager, indicatorAdapt, magicIndicatorNeedOnPageScrolled = true)
            }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = GpFragmentBinding.inflate(inflater, parent, false)
}