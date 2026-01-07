package com.cboda.intelligent.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.FragmentCleverBodyBinding
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.DOCUMENT_TYPE
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.IMAGE_TYPE
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.VIDEO_TYPE
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.VOICE_TYPE
import com.cboda.intelligent.activity.AiCheckRecordBroadActivity
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.widge.ImgIndicatorBroadAdapter

class CleverBodyBroadFragment : BaseBroadFragment<FragmentCleverBodyBinding, CleverBodyViewBroadModel>() {
    private var oldIndex = -1
    private lateinit var documentFragment: AiAllBroadFragment
    private lateinit var imgFragment: AiAllBroadFragment
    private lateinit var voiceFragment: AiAllBroadFragment
    private lateinit var videoFragment: AiAllBroadFragment

    private fun initViewPagerAndIndicator() {
        documentFragment = AiAllBroadFragment().apply { updateResourceType(DOCUMENT_TYPE) }
        imgFragment = AiAllBroadFragment().apply { updateResourceType(IMAGE_TYPE)}
        voiceFragment = AiAllBroadFragment().apply { updateResourceType(VOICE_TYPE)}
        videoFragment = AiAllBroadFragment().apply { updateResourceType(VIDEO_TYPE)}
//        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
//        vp2FragmentAdapter.addFragment(documentFragment)
//        vp2FragmentAdapter.addFragment(imgFragment)
//        vp2FragmentAdapter.addFragment(voiceFragment)
//        vp2FragmentAdapter.addFragment(videoFragment)
//        binding.viewPager.adapter = vp2FragmentAdapter
//        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
//        binding.viewPager.isUserInputEnabled = false
//        binding.viewPager.offscreenPageLimit = vp2FragmentAdapter.getData().size
//        binding.viewPager.isSaveEnabled = false
        val mResIds =
            intArrayOf(R.string.ai_document, R.string.ai_img, R.string.ai_audio, R.string.ai_video)
        val mResIds2 = intArrayOf(
            R.mipmap.icon_ai_document,
            R.mipmap.icon_ai_img,
            R.mipmap.icon_ai_audio,
            R.mipmap.icon_ai_video
        )
        binding.aiMagicIndicator.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(requireContext())
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = ImgIndicatorBroadAdapter(requireContext(), mResIds, mResIds2)
        commonNavigator.adapter = indicatorAdapt
        binding.aiMagicIndicator.navigator = commonNavigator

        childFragmentManager.beginTransaction()
            .add(R.id.viewPagerLayout, documentFragment, DOCUMENT_TYPE)
            .commit()
        indicatorAdapt.setOnIndicatorTapClickListener { index ->
            val currentFragment = when (oldIndex) {
                0 -> documentFragment
                1 -> imgFragment
                2 -> voiceFragment
                3 -> videoFragment
                else -> documentFragment
            }
            if (oldIndex != index) {
                currentFragment.checkDataAndClear {
                    switchPage(index, currentFragment)
                }
            } else {
                switchPage(index, currentFragment)
            }
        }
//        bindIndicatorAndViewPager(
//            binding.aiMagicIndicator,
//            binding.viewPager,
//            indicatorAdapt,
//            commonNavigatorAdapterCanResult = false
//        ) { _, _, _, _, index ->
//            if (oldIndex != index && index != null && vp2FragmentAdapter.getData().size > index && index > -1) {
//                val fragment = vp2FragmentAdapter.getData()[index]
//                if (fragment is AiAllBroadFragment) {
//                    fragment.checkDataAndClear {
//                        switchPage(index, null)
//                    }
//                }
//            }
//        }
    }


    private fun switchPage(index: Int?, aiFragment: AiAllBroadFragment?) {
        index?.let {
            if (oldIndex != it) {
                oldIndex = it
                val (newFragment, newTag) = when (oldIndex) {
                    0 -> documentFragment to DOCUMENT_TYPE
                    1 -> imgFragment to IMAGE_TYPE
                    2 -> voiceFragment to VOICE_TYPE
                    3 -> videoFragment to VIDEO_TYPE
                    else -> videoFragment to DOCUMENT_TYPE
                }
                binding.aiMagicIndicator.onPageSelected(oldIndex)
                childFragmentManager.beginTransaction().apply {
                    // 隐藏当前 Fragment
                    aiFragment?.let { aiFragment -> hide(aiFragment) }
                    // 如果新 Fragment 没有添加过，先 add
                    if (!newFragment.isAdded) {
                        add(R.id.viewPagerLayout, newFragment, newTag)
                    } else {
                        // 已添加，直接显示
                        show(newFragment)
                    }
                    commit()
                }
//                binding.viewPager.setCurrentItem(oldIndex, false)
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
    ): FragmentCleverBodyBinding {
        return FragmentCleverBodyBinding.inflate(inflater, parent, false)
    }

}