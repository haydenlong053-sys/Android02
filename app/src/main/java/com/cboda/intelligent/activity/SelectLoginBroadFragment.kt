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
import com.cboda.intelligent.databinding.SelectLoginBroadFragmentBinding
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class SelectLoginBroadFragment : BaseBroadFragment<SelectLoginBroadFragmentBinding, LoginViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = SelectLoginBroadFragmentBinding.inflate(inflater, parent, false)

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
//            viewModel.startAct<LoginQrCodeActivity>()
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
//            val bundle = Bundle()
//            bundle.putInt("jumpSource", ScanJumpBroadEnum.Login.sourceType)
//            viewModel.startAct<ScanBroadActivity>(bundle)
            (activity as NewEquipmentBroadActivity).toScan()

//            val loginBean = LoginBean()
//            loginBean.twoFactorCode = true
//            loginBean.existPayPassword = true
//            loginBean.existMbName = true
//            loginBean.securityQuestion = "你的警号"
//            val securityVerificationPopup = SecurityVerificationPopup(activity ?: requireContext(), loginBean)
//            securityVerificationPopup.showPopupWindow()


//            val list = arrayListOf<ComBottomListBean>()
//
//            list.add(ComBottomListBean().apply {
//                iconRes = R.mipmap.ic_broad_head
//                itemContent = "测试数据1"
//            })
//            list.add(ComBottomListBean().apply {
//                iconRes = R.mipmap.ic_broad_head
//                itemContent = "测试数据2"
//            })
//            showCommonBottomListPop(list, activity, R.color.transparent) { _, _ -> }


//            showCommonStrTip(
//                "新设备登录", "需在旧设备获取设备验证码", "取消", "好的", activity, R.color.color_3B47DD_20, R.color.color_3B47DD, R.color.white, R.color.color_A5A5A5, R.color.white, R.color.color_A5A5A5,
//                R.color.color_3B47DD, R.color.color_3B47DD, R.color.transparent, R.color.transparent, R.color.black_FF4D4F, R.color.colorAccent
//            ) {
//            }
        }
    }
}