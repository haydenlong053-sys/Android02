package com.cboda.intelligent.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.utils.click
import com.cboda.intelligent.activity.UpdatePwdActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.AccountDetailFragmentBinding
import com.cboda.intelligent.dialog.pop.AuthCodeBroadPop
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.dialog.pop.AuthQuestionBroadPop
import com.cboda.intelligent.dialog.pop.AuthWayBroadPop
import com.cboda.intelligent.utils.showGuidePasswordTip
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
            userInfoBean?.let { userInfo ->
                if (TextUtils.isEmpty(userInfo.payPwd)) {
                    showGuidePasswordTip(requireContext())
                } else {
                    val haveMb = !TextUtils.isEmpty(userInfo.securityQuestionAnswer)
                    val haveGoogleCode = !TextUtils.isEmpty(userInfo.googleCode)
                    if (haveMb && haveGoogleCode) {
                        val authWayPop = AuthWayBroadPop(requireContext(), SafeJumpBroadEnum.ChangeLoginPassword.sourceType)
                        authWayPop.transferInvoke = {
                            checkPayPwd()
                        }
                        authWayPop.showPopupWindow()
                    } else if (haveMb) {
                        val tipPop = AuthQuestionBroadPop(
                            requireContext(),
                            SafeJumpBroadEnum.ChangeLoginPassword.sourceType
                        )
                        tipPop.backInvoke = {
                            checkPayPwd()
                        }
                        tipPop.showPopupWindow()
                    } else if (haveGoogleCode) {
                        val tipPop = AuthCodeBroadPop(
                            requireContext(),
                            SafeJumpBroadEnum.ChangeLoginPassword.sourceType
                        )
                        tipPop.transferInvoke = {
                            checkPayPwd()
                        }
                        tipPop.showPopupWindow()
                    } else {
                        checkPayPwd()
                    }
                }
            }

        }
    }

    private fun checkPayPwd() {
        val mPop = AuthPasswordBroadPop(
            requireContext(),
            SafeJumpBroadEnum.ChangeLoginPassword.sourceType
        )
        mPop.backInvoke = {
            viewModel.startAct<UpdatePwdActivity>()
        }
        mPop.showPopupWindow()
    }

    override fun initView() {
        super.initView()
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = AccountDetailFragmentBinding.inflate(inflater, parent, false)
}