package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.utils.click
import com.cboda.intelligent.activity.UpdatePwdActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.AccountDetailFragmentBinding
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class AccountDetailFragment : BaseBroadFragment<AccountDetailFragmentBinding, LoginViewBroadModel>() {


    private var userInfoBean: UserInfoBean? = null

    fun setData(data: UserInfoBean) {
        userInfoBean = data
        setData()
    }

    private fun setData() {
        mBinding?.let {
            userInfoBean?.let { userInfo ->
                binding.accountEt.text = userInfo.loginAccount
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.updatePwdBtn.click {
            viewModel.startAct<UpdatePwdActivity>()
        }
    }

    override fun initView() {
        super.initView()
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = AccountDetailFragmentBinding.inflate(inflater, parent, false)
}