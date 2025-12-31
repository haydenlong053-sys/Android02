package com.lxkj.aiim.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentAdapter
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.view.magicindicator.CommonNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseFragment
import com.lxkj.aiim.databinding.FragmentCleverBodyBinding
import com.lxkj.aiim.fragment.AiAllFragment.Companion.DOCUMENT_TYPE
import com.lxkj.aiim.fragment.AiAllFragment.Companion.IMAGE_TYPE
import com.lxkj.aiim.fragment.AiAllFragment.Companion.VIDEO_TYPE
import com.lxkj.aiim.fragment.AiAllFragment.Companion.VOICE_TYPE
import com.lxkj.aiim.lj.AiCheckRecordActivity
import com.lxkj.aiim.viewmodel.CleverBodyViewModel
import com.lxkj.aiim.widge.ImgIndicatorAdapter

class CleverBodyFragment : BaseFragment<FragmentCleverBodyBinding, CleverBodyViewModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentCleverBodyBinding {
        return FragmentCleverBodyBinding.inflate(inflater, parent, false)
    }

    override fun initView() {
        addHeight(binding.aiTime)
        initViewPagerAndIndicator()
    }

    override fun initListener() {
        super.initListener()
        binding.aiTime.click {
            viewModel.startAct<AiCheckRecordActivity>()
        }
    }

    private var oldIndex = 0
    private fun initViewPagerAndIndicator() {
        val vp2FragmentAdapter = Vp2FragmentAdapter(this)
        vp2FragmentAdapter.addFragment(AiAllFragment(DOCUMENT_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment(IMAGE_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment(VOICE_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment(VIDEO_TYPE))
        binding.viewPager.adapter = vp2FragmentAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = vp2FragmentAdapter.getData().size
        binding.viewPager.isSaveEnabled = false
        val mResIds =
            intArrayOf(R.string.ai_document, R.string.ai_img, R.string.ai_audio, R.string.ai_video)
        val mResIds2 = intArrayOf(
            R.mipmap.icon_ai_document,
            R.mipmap.icon_ai_img,
            R.mipmap.icon_ai_audio,
            R.mipmap.icon_ai_video
        )
        binding.aiMagicIndicator.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonNavigator(requireContext())
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = ImgIndicatorAdapter(requireContext(), mResIds, mResIds2)
        commonNavigator.adapter = indicatorAdapt
        binding.aiMagicIndicator.navigator = commonNavigator
        bindIndicatorAndViewPager(
            binding.aiMagicIndicator,
            binding.viewPager,
            indicatorAdapt,
            commonNavigatorAdapterCanResult = false
        ) { _, _, _, _, index ->
            if (oldIndex != index) {
                val fragment = vp2FragmentAdapter.getData()[oldIndex]
                if (fragment is AiAllFragment) {
                    fragment.checkDataAndClear {
                        switchPage(index)
                    }
//                    fragment.clearChooseData()
                }
            } else {
                switchPage(index)
            }
        }
    }


    private fun switchPage(index: Int?) {
        index?.let {
            oldIndex = it
            binding.viewPager.setCurrentItem(oldIndex, false)
        }
    }


}