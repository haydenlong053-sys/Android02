package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setInputTypeKeepingCursor
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.baselibrary.utils.toJson2
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.cboda.intelligent.R
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.databinding.LoginBroadFragmentBinding

class LoginBroadFragment : BaseBroadFragment<LoginBroadFragmentBinding, LoginViewBroadModel>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LoginBroadFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.loginBtn.isSubmitCanGo(false)
        binding.loginLookPwd.isGone = false
    }

    override fun initListener() {
        super.initListener()
        binding.loginAccount.setEdTextChange {
            (activity as NewEquipmentBroadActivity).updateLoginAccount(it)
            val pwd = binding.loginPwd.text.toString()
            binding.loginBtn.isSubmitCanGo(!TextUtils.isEmpty(it) && !TextUtils.isEmpty(pwd))
        }
        binding.loginPwd.setEdTextChange {
            val account = binding.loginAccount.text.toString()
            binding.loginBtn.isSubmitCanGo(
                !TextUtils.isEmpty(it) && !TextUtils.isEmpty(
                    account
                )
            )
        }
        binding.loginForgetPwd.click {
            (activity as NewEquipmentBroadActivity).toForgetPwd(true)
        }
        binding.loginRegisterAccount.click {
            (activity as NewEquipmentBroadActivity).toRegistered()
        }
        binding.loginBtn.click {
            val loginAccount = binding.loginAccount.text.toString()
            val loginPwd = binding.loginPwd.text.toString()
            //82环境使用老登录
//            if (HttpsBroadConfig.currentHostHttpUrlType == HttpsBroadConfig.RELEASE_TYPE) {
            if (HttpsBroadConfig.currentHostHttpUrlType > 1000) {
                viewModel.pwdLogin(
                    loginAccount,
                    loginPwd
                )
            } else {
                viewModel.getAccountLoginDetail(
                    loginAccount,
                    loginPwd
                ).observe(this) { loginBean ->
                    if (null != loginBean) {
                        loginBean.loginAccount = loginAccount
                        loginBean.loginPwd = loginPwd
                        (activity as NewEquipmentBroadActivity).openSecurityDialog(loginBean)
//                        val bundle = Bundle()
//                        bundle.putString("loginBean", toJson2(loginBean))
//                        viewModel.startAct<LoginVerificationActivity>(bundle)
                    }
                }
            }
        }
        binding.oneLogin.click {
//            viewModel.startAct<EquipmentBroadActivity>(canFinish = true)
            viewModel.oneClickLogin()
        }
        binding.loginLookPwd.setOnTouchListener { _, event ->
            binding.loginLookPwd.setImageResource(if (event.action == MotionEvent.ACTION_UP) R.mipmap.icon_pwd_broad_close else R.mipmap.icon_pwd_broad_open)
            binding.loginPwd.setInputTypeKeepingCursor(if (event.action == MotionEvent.ACTION_UP) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            true
        }
    }
}