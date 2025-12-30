package com.lxkj.aiim.lj

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isGone
import com.base.commonlibrary.utils.click
import com.base.commonlibrary.utils.setEdTextChange
import com.base.commonlibrary.utils.setInputTypeKeepingCursor
import com.base.commonlibrary.utils.setStrokeColorAndStartAlpha
import com.lxkj.aiim.base.BaseFragment
import com.lxkj.aiim.databinding.LoginFragmentBinding
import com.lxkj.aiim.lj.utils.isSubmitCanGo
import com.lxkj.aiim.viewmodel.LoginViewModel
import com.lxkj.aiim.R

class LoginFragment : BaseFragment<LoginFragmentBinding, LoginViewModel>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LoginFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = com.base.commonlibrary.R.color.color_080E1E)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = com.base.commonlibrary.R.color.color_080E1E)
        binding.loginBtn.isSubmitCanGo(false)
        binding.loginLookPwd.isGone = false
    }

    override fun initListener() {
        super.initListener()
        binding.loginAccount.setEdTextChange {
            (activity as NewEquipmentActivity).updateLoginAccount(it)
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
            (activity as NewEquipmentActivity).toForgetPwd(true)
        }
        binding.loginRegisterAccount.click {
            (activity as NewEquipmentActivity).toRegistered()
        }
        binding.loginBtn.click {
            viewModel.pwdLogin(
                binding.loginAccount.text.toString(),
                binding.loginPwd.text.toString()
            )
        }
        binding.oneLogin.click {
//            viewModel.startAct<EquipmentActivity>(canFinish = true)
            viewModel.oneClickLogin()
        }
        binding.loginLookPwd.setOnTouchListener { _, event ->
            binding.loginLookPwd.setImageResource(if (event.action == MotionEvent.ACTION_UP) R.mipmap.icon_pwd_close else R.mipmap.icon_pwd_open)
            binding.loginPwd.setInputTypeKeepingCursor(if (event.action == MotionEvent.ACTION_UP) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            true
        }
    }
}