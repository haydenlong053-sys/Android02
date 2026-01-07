package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setDrawableStartAlpha
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.UpdatePwdFragmentBinding
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import kotlin.toString

class UpdatePwdBroadFragment : BaseBroadFragment<UpdatePwdFragmentBinding, LoginViewBroadModel>() {
    private var inputAccount = ""
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = UpdatePwdFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.loginAccount.setDrawableStartAlpha(start = true)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.updatePwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.loginBtn.isSubmitCanGo(false)
        setAccountData()
    }


    override fun initListener() {
        super.initListener()
        binding.loginPwd.setEdTextChange {
            val secondPwd = binding.updatePwd.text.toString()
            binding.loginBtn.isSubmitCanGo(
                !TextUtils.isEmpty(it) && !TextUtils.isEmpty(inputAccount) && !TextUtils.isEmpty(
                    secondPwd
                )
            )
        }
        binding.updatePwd.setEdTextChange {
            val pwd = binding.loginPwd.text.toString()
            binding.loginBtn.isSubmitCanGo(
                !TextUtils.isEmpty(it) && !TextUtils.isEmpty(inputAccount) && !TextUtils.isEmpty(
                    pwd
                )
            )
        }
        binding.loginBtn.click {
            viewModel.updatePassword(
                inputAccount,
                binding.loginPwd.text.toString(),
                binding.updatePwd.text.toString()
            ).observe(this) {
                (activity as NewEquipmentBroadActivity).toLogin(false)
                showToastMain(getString(R.string.password_reset_success))
            }
        }
    }

    private fun setAccountData() {
        if (null != mBinding) {
            binding.loginAccount.text = inputAccount
        }

    }

    fun updateAccount(account: String) {
        inputAccount = account
        setAccountData()
    }
}