package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.baselibrary.utils.click
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.LoginQrCodeFragmentBinding
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class LoginQrCodeFragment : BaseBroadFragment<LoginQrCodeFragmentBinding, LoginViewBroadModel>() {

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = LoginQrCodeFragmentBinding.inflate(inflater, parent, false)


    override fun initView() {
        super.initView()
    }

    override fun initData() {
        super.initData()

    }

    override fun initListener() {
        super.initListener()
        binding.showQrcodeTv.click {
            setCloseEyesIsGone(false)
        }
        binding.closeEyes.click {
            setCloseEyesIsGone(true)
        }
    }


    private fun setCloseEyesIsGone(isGone: Boolean) {
        binding.closeEyes.isGone = isGone
        binding.qrCode.isGone = isGone
        binding.mViewDash.isInvisible = isGone
        binding.qrCodeBlur.isGone = !isGone
        binding.showQrcodeTv.isGone = !isGone
    }


}