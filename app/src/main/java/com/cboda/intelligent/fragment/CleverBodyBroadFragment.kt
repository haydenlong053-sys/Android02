package com.cboda.intelligent.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.fragment.AiAllFragment.Companion.DOCUMENT_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.IMAGE_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.VIDEO_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.VOICE_TYPE
import com.cboda.intelligent.activity.AiCheckRecordBroadActivity
import com.cboda.intelligent.databinding.FragmentCleverBroadBodyBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.widge.ImgIndicatorBroadAdapter

class CleverBodyBroadFragment : BaseBroadFragment<FragmentCleverBroadBodyBinding, CleverBodyViewBroadModel>() {
    private var oldIndex = -1
//    private lateinit var documentFragment: AiAllFragment
//    private lateinit var imgFragment: AiAllFragment
//    private lateinit var voiceFragment: AiAllFragment
//    private lateinit var videoFragment: AiAllFragment

    private fun initViewPagerAndIndicator() {
//        documentFragment = AiAllFragment.newInstance(DOCUMENT_TYPE)
//        imgFragment = AiAllFragment.newInstance(IMAGE_TYPE)
//        voiceFragment = AiAllFragment.newInstance(VOICE_TYPE)
//        videoFragment = AiAllFragment.newInstance(VIDEO_TYPE)
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(DOCUMENT_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(IMAGE_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(VOICE_TYPE))
        vp2FragmentAdapter.addFragment(AiAllFragment.newInstance(VIDEO_TYPE))

        binding.viewPagerLayout.viewPager2.adapter = vp2FragmentAdapter
        binding.viewPagerLayout.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPagerLayout.viewPager2.isUserInputEnabled = false
        binding.viewPagerLayout.viewPager2.offscreenPageLimit = vp2FragmentAdapter.getData().size
        binding.viewPagerLayout.viewPager2.isSaveEnabled = false
        val mResIds =
            intArrayOf(R.string.ai_document, R.string.ai_img, R.string.ai_audio, R.string.ai_video)
        val mResIds2 = intArrayOf(
            R.mipmap.icon_ai_broad_document,
            R.mipmap.icon_ai_broad_img,
            R.mipmap.icon_ai_broad_audio,
            R.mipmap.icon_ai_broad_video
        )
        binding.aiMagicIndicator.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(requireContext())
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = ImgIndicatorBroadAdapter(requireContext(), mResIds, mResIds2)
        commonNavigator.adapter = indicatorAdapt
        binding.aiMagicIndicator.navigator = commonNavigator

//        childFragmentManager.beginTransaction()
//            .add(R.id.viewPagerLayout, documentFragment, DOCUMENT_TYPE)
//            .commit()
//        indicatorAdapt.setOnIndicatorTapClickListener { index ->
//            val currentFragment = when (oldIndex) {
//                0 -> documentFragment
//                1 -> imgFragment
//                2 -> voiceFragment
//                3 -> videoFragment
//                else -> documentFragment
//            }
//            if (oldIndex != index) {
//                currentFragment.checkDataAndClear {
//                    switchPage(index, currentFragment)
//                }
//            } else {
//                switchPage(index, currentFragment)
//            }
//        }
        bindIndicatorAndViewPager(
            binding.aiMagicIndicator,
            binding.viewPagerLayout.viewPager2,
            indicatorAdapt,
            commonNavigatorAdapterCanResult = false
        ) { _, _, _, _, index ->
            if (oldIndex != index && index != null && vp2FragmentAdapter.getData().size > index && index > -1) {
                val fragment = vp2FragmentAdapter.getData()[index]
                if (fragment is AiAllFragment) {
                    fragment.checkDataAndClear {
                        switchPage(index, null)
                    }
                }
            }
        }
    }


    private fun switchPage(index: Int?, aiFragment: AiAllFragment?) {
        index?.let {
            if (oldIndex != it) {
                oldIndex = it
//                val (newFragment, newTag) = when (oldIndex) {
//                    0 -> documentFragment to DOCUMENT_TYPE
//                    1 -> imgFragment to IMAGE_TYPE
//                    2 -> voiceFragment to VOICE_TYPE
//                    3 -> videoFragment to VIDEO_TYPE
//                    else -> videoFragment to DOCUMENT_TYPE
//                }
//                binding.aiMagicIndicator.onPageSelected(oldIndex)
//                childFragmentManager.beginTransaction().apply {
//                    // 隐藏当前 Fragment
//                    aiFragment?.let { aiFragment -> hide(aiFragment) }
//                    // 如果新 Fragment 没有添加过，先 add
//                    if (!newFragment.isAdded) {
//                        add(R.id.viewPagerLayout, newFragment, newTag)
//                    } else {
//                        // 已添加，直接显示
//                        show(newFragment)
//                    }
//                    commit()
//                }
                binding.viewPagerLayout.viewPager2.setCurrentItem(oldIndex, false)
            }
        }
    }


    override fun initView() {
        addHeight(binding.aiTime)
        initViewPagerAndIndicator()
    }

    override fun initListener() {
        super.initListener()
        binding.aiTime.click {
            viewModel.startAct<AiCheckRecordBroadActivity>()
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentCleverBroadBodyBinding {
        return FragmentCleverBroadBodyBinding.inflate(inflater, parent, false)
    }

}