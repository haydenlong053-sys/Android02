package com.lxkj.aiim.lj

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
import com.lxkj.aiim.base.BaseFragment
import com.cboda.intelligent.databinding.ForgetPwdFragmentBinding
import com.lxkj.aiim.dialog.pop.AuthWayPop
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.utils.isSubmitCanGo
import com.lxkj.aiim.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class ForgetPwdFragment : BaseFragment<ForgetPwdFragmentBinding, LoginViewModel>() {

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
//            (activity as NewEquipmentActivity).toUpdatePwd()
            lifecycleScope.launch {
                viewModel.checkAccount(binding.loginAccount.text.toString()).observe(this@ForgetPwdFragment){
                    if (it.twoFactorCode.isNullOrEmpty() && it.securityId.isNullOrEmpty() && it.payPassword.isNullOrEmpty()){
                        ToastBroadUtils.show(R.string.m_query_fail)
                    }else{
                        val authWayPop = AuthWayPop(requireContext(), SafeJumpEnum.LoginForgetPassword.sourceType, true,it,binding.loginAccount.text.toString())
                        authWayPop.transferInvoke = {
                            (activity as NewEquipmentActivity).toUpdatePwd()
                        }
                        authWayPop.showPopupWindow()
                    }
                }
            }

        }
        binding.loginAccount.setEdTextChange {
            (activity as NewEquipmentActivity).updatePwdAccount(it)
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
    ) = ForgetPwdFragmentBinding.inflate(inflater, parent, false)

}