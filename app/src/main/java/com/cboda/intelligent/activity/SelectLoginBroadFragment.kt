package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getIsDebug
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.SelectLoginFragmentBinding
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class SelectLoginBroadFragment : BaseBroadFragment<SelectLoginFragmentBinding, LoginViewBroadModel>() {
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
            (activity as NewEquipmentBroadActivity).toLogin()
        }
        binding.registerTv.click {
            (activity as NewEquipmentBroadActivity).toRegistered(true)
        }
        if (getIsDebug()) {
            binding.selectLoginLayout.click {
                HttpsBroadConfig.showSwitchDialog()
            }
        }
        binding.scanImg.setOnClickListener(scanClick)
        binding.scanView.setOnClickListener(scanClick)
        binding.scanTv.setOnClickListener(scanClick)
    }

    private val scanClick = object : OnSingleClickBroadListener() {
        override fun onSingleClick(v: View?) {
            val bundle = Bundle()
            bundle.putInt("jumpSource", ScanJumpBroadEnum.Login.sourceType)
            viewModel.startAct<ScanBroadActivity>(bundle)
        }
    }
}