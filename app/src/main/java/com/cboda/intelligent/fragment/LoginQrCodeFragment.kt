package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.LoginQrCodeFragmentBinding
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class LoginQrCodeFragment : BaseBroadFragment<LoginQrCodeFragmentBinding, LoginViewBroadModel>() {
    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = LoginQrCodeFragmentBinding.inflate(inflater, parent, false)


}