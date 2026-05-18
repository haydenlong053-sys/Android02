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
import com.cboda.intelligent.databinding.UpdatePwdBroadFragmentBinding
import com.cboda.intelligent.utils.isSubmitCanGo2
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import kotlin.toString

class UpdatePwdBroadFragment : BaseBroadFragment<UpdatePwdBroadFragmentBinding, LoginViewBroadModel>() {
    private var inputAccount = ""
    private val match1 = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,16}$"
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = UpdatePwdBroadFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.loginAccount.setDrawableStartAlpha(start = true)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.updatePwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.loginBtn.isSubmitCanGo2(false)
        setAccountData()
    }


    override fun initListener() {
        super.initListener()
        binding.loginPwd.setEdTextChange {
            checkPwd(false)
        }
        binding.updatePwd.setEdTextChange {
            checkPwd(false)
        }
        binding.loginBtn.click {
            checkPwd(true)
        }
    }

    private fun checkPwd(isUpdate: Boolean) {
        if (TextUtils.isEmpty(inputAccount)) {
            binding.loginBtn.isSubmitCanGo2(false)
            return
        }
        val pwd = mBinding?.loginPwd?.text?.toString()?.trim() ?: ""
        val againPwd = mBinding?.updatePwd?.text?.toString()?.trim() ?: ""
        if (!TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(againPwd)) {
            if (pwd != againPwd) {
                binding.loginBtn.isSubmitCanGo2(false)
                if (isUpdate) {
                    showToastMain(com.cboda.baselibrary.R.string.again_pwd_inconsistent)
                    return
                }
            } else if (!pwd.matches(Regex(match1))) {
                binding.loginBtn.isSubmitCanGo2(false)
                if (isUpdate) {
                    showToastMain(com.cboda.baselibrary.R.string.password_not_is_num)
                    return
                }
            } else {
                binding.loginBtn.isSubmitCanGo2(true)
                if (isUpdate) {
                    viewModel.updatePassword(
                        inputAccount,
                        pwd,
                        againPwd
                    ).observe(this) {
                        (activity as NewEquipmentBroadActivity).toLogin(false)
                        showToastMain(getString(R.string.password_reset_success))
                    }
                }
            }
        } else if (TextUtils.isEmpty(pwd)) {
            binding.loginBtn.isSubmitCanGo2(false)
            if (isUpdate) {
                showToastMain(com.cboda.baselibrary.R.string.please_input_new_pwd)
                return
            }
        } else if (TextUtils.isEmpty(againPwd)) {
            binding.loginBtn.isSubmitCanGo2(false)
            if (isUpdate) {
                showToastMain(R.string.请再次输入密码)
                return
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