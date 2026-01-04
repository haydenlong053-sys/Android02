package com.lxkj.aiim.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
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
    private var oldIndex = -1
    private lateinit var documentFragment: AiAllFragment
    private lateinit var imgFragment: AiAllFragment
    private lateinit var voiceFragment: AiAllFragment
    private lateinit var videoFragment: AiAllFragment

    private fun initViewPagerAndIndicator() {
        documentFragment = AiAllFragment(DOCUMENT_TYPE)
        imgFragment = AiAllFragment(IMAGE_TYPE)
        voiceFragment = AiAllFragment(VOICE_TYPE)
        videoFragment = AiAllFragment(VIDEO_TYPE)
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
        val indicatorAdapt = ImgIndicatorAdapter(requireContext(), mResIds, mResIds2)
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
//                if (fragment is AiAllFragment) {
//                    fragment.checkDataAndClear {
//                        switchPage(index, null)
//                    }
//                }
//            }
//        }
    }


    private fun switchPage(index: Int?, aiFragment: AiAllFragment?) {
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
            viewModel.startAct<AiCheckRecordActivity>()
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