package com.lxkj.aiim.lj

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cboda.baselibrary.utils.HttpsConfig
import com.cboda.baselibrary.utils.OnSingleClickListener
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getIsDebug
import com.lxkj.aiim.base.BaseFragment
import com.lxkj.aiim.databinding.SelectLoginFragmentBinding
import com.lxkj.aiim.lj.configuration.ScanJumpEnum
import com.lxkj.aiim.viewmodel.LoginViewModel

class SelectLoginFragment : BaseFragment<SelectLoginFragmentBinding, LoginViewModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = SelectLoginFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.titleLottie.imageAssetsFolder = "seekwebimages"
    }

    override fun initListener() {
        super.initListener()
        binding.loginTv.click {
            (activity as NewEquipmentActivity).toLogin()
        }
        binding.registerTv.click {
            (activity as NewEquipmentActivity).toRegistered(true)
        }
        if (getIsDebug()) {
            binding.selectLoginLayout.click {
                HttpsConfig.showSwitchDialog()
            }
        }
        binding.scanImg.setOnClickListener(scanClick)
        binding.scanView.setOnClickListener(scanClick)
        binding.scanTv.setOnClickListener(scanClick)
    }

    private val scanClick = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            val bundle = Bundle()
            bundle.putInt("jumpSource", ScanJumpEnum.Login.sourceType)
            viewModel.startAct<ScanActivity>(bundle)
        }
    }
}