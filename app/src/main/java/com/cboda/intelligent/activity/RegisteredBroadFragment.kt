package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.getUuIdCreate
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setInputTypeKeepingCursor
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.blankj.utilcode.util.EncryptUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.RegisteredBroadFragmentBinding
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class RegisteredBroadFragment : BaseBroadFragment<RegisteredBroadFragmentBinding, LoginViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = RegisteredBroadFragmentBinding.inflate(inflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.updatePwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginBtn.isSubmitCanGo(
            false,
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
        val uuIdCreate = getUuIdCreate()
        binding.content.text =
            (uuIdCreate + EncryptUtils.encryptMD5ToString(uuIdCreate)).uppercase()
        viewModel.getMnemonicWords().observe(this) {
            if (!TextUtils.isEmpty(it)) {
                binding.content.text = it
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.loginAccount.setEdTextChange {
            refreshSubmitEnabled()
            binding.loginLookPwd.isGone = TextUtils.isEmpty(it)
        }
        binding.loginPwd.setEdTextChange { refreshSubmitEnabled() }
        binding.updatePwd.setEdTextChange { refreshSubmitEnabled() }
        binding.loginBtn.click {
            viewModel.registerAccount(
                binding.loginAccount.text.toString(),
                binding.loginPwd.text.toString(),
                binding.updatePwd.text.toString()
            )
        }
        binding.loginLookPwd.click {
            binding.loginAccount.setText("")
        }
        bindPwdLookTouch(binding.loginPwdLook, binding.loginPwd)
        bindPwdLookTouch(binding.updatePwdLook, binding.updatePwd)
    }

    private fun refreshSubmitEnabled() {
        val account = binding.loginAccount.text.toString()
        val pwd = binding.loginPwd.text.toString()
        val secondPwd = binding.updatePwd.text.toString()
        binding.loginBtn.isSubmitCanGo(
            !TextUtils.isEmpty(account) && !TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(secondPwd),
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
    }

    private fun bindPwdLookTouch(
        lookBtn: android.widget.ImageButton,
        editText: android.widget.EditText,
    ) {
        lookBtn.setOnTouchListener { _, event ->
            lookBtn.setImageResource(
                if (event.action == MotionEvent.ACTION_UP) R.mipmap.icon_pwd_broad_close
                else R.mipmap.icon_pwd_broad_open
            )
            editText.setInputTypeKeepingCursor(
                if (event.action == MotionEvent.ACTION_UP) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }
            )
            true
        }
    }
}
