package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.baselibrary.utils.click
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.AccountDetailFragmentBinding
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class AccountDetailFragment : BaseBroadFragment<AccountDetailFragmentBinding, LoginViewBroadModel>() {

    override fun initListener() {
        super.initListener()
        binding.updatePwdBtn.click {

        }
    }

    override fun initView() {
        super.initView()
    }
    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?)= AccountDetailFragmentBinding.inflate(inflater, parent, false)
}