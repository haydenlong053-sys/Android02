package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.dialog.pop.AuthWayBroadPop
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.ForgetPwdBroadFragmentBinding
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import kotlinx.coroutines.launch

class ForgetPwdBroadFragment : BaseBroadFragment<ForgetPwdBroadFragmentBinding, LoginViewBroadModel>() {

    private var forgetAccount = ""
    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.loginBtn.isSubmitCanGo(false)
        setAccountData()
    }

    override fun initListener() {
        super.initListener()
        binding.loginBtn.click {
//            (activity as NewEquipmentBroadActivity).toUpdatePwd()
            lifecycleScope.launch {
                viewModel.checkAccount(binding.loginAccount.text.toString()).observe(this@ForgetPwdBroadFragment){
                    if (it.twoFactorCode.isNullOrEmpty() && it.securityId.isNullOrEmpty() && it.payPassword.isNullOrEmpty()){
                        ToastBroadUtils.show(R.string.m_query_fail)
                    }else{
                        val authWayPop = AuthWayBroadPop(requireContext(), SafeJumpBroadEnum.LoginForgetPassword.sourceType, true,it,binding.loginAccount.text.toString())
                        authWayPop.transferInvoke = {
                            (activity as NewEquipmentBroadActivity).toUpdatePwd()
                        }
                        authWayPop.showPopupWindow()
                    }
                }
            }

        }
        binding.loginAccount.setEdTextChange {
            (activity as NewEquipmentBroadActivity).updatePwdAccount(it)
            binding.loginBtn.isSubmitCanGo(!TextUtils.isEmpty(it))
        }
    }

    private fun setAccountData() {
        if (null != mBinding) {
            binding.loginAccount.setText(forgetAccount)
        }
    }

    fun updateAccount(account: String) {
        forgetAccount = account
        setAccountData()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ForgetPwdBroadFragmentBinding.inflate(inflater, parent, false)

}