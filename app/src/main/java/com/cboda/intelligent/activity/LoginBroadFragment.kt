package com.cboda.intelligent.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setInputTypeKeepingCursor
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.LoginBroadFragmentBinding

class LoginBroadFragment : BaseBroadFragment<LoginBroadFragmentBinding, LoginViewBroadModel>() {

    private var captchaId: String = ""

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LoginBroadFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginCaptcha.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginBtn.isSubmitCanGo(
            false,
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
        binding.loginLookPwd.isGone = false
        refreshCaptcha()
    }

    override fun initListener() {
        super.initListener()
        binding.loginAccount.setEdTextChange {
            (activity as NewEquipmentBroadActivity).updateLoginAccount(it)
            refreshSubmitEnabled()
        }
        binding.loginPwd.setEdTextChange { refreshSubmitEnabled() }
        binding.loginCaptcha.setEdTextChange { refreshSubmitEnabled() }
        binding.loginCaptchaImage.click { refreshCaptcha() }
        binding.loginCaptchaRefresh.click { refreshCaptcha() }
        binding.loginForgetPwd.click {
            (activity as NewEquipmentBroadActivity).toForgetPwd(true)
        }
        binding.loginRegisterAccount.click {
            (activity as NewEquipmentBroadActivity).toRegistered()
        }
        binding.loginBtn.click {
            viewModel.pwdLogin(
                account = binding.loginAccount.text.toString(),
                pwd = binding.loginPwd.text.toString(),
                captchaId = captchaId,
                captcha = binding.loginCaptcha.text.toString(),
                requireCaptcha = true,
                onCaptchaInvalid = { refreshCaptcha() },
            )
        }
        binding.oneLogin.click {
            viewModel.oneClickLogin()
        }
        binding.loginLookPwd.setOnTouchListener { _, event ->
            binding.loginLookPwd.setImageResource(
                if (event.action == MotionEvent.ACTION_UP) R.mipmap.icon_pwd_broad_close
                else R.mipmap.icon_pwd_broad_open
            )
            binding.loginPwd.setInputTypeKeepingCursor(
                if (event.action == MotionEvent.ACTION_UP) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }
            )
            true
        }
    }

    private fun refreshSubmitEnabled() {
        val account = binding.loginAccount.text.toString()
        val pwd = binding.loginPwd.text.toString()
        val captcha = binding.loginCaptcha.text.toString()
        binding.loginBtn.isSubmitCanGo(
            !TextUtils.isEmpty(account) && !TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(captcha),
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
    }

    private fun refreshCaptcha() {
        binding.loginCaptcha.setText("")
        captchaId = ""
        viewModel.fetchCaptcha("login").observe(viewLifecycleOwner) { bean ->
            captchaId = bean?.captchaId.orEmpty()
            val raw = bean?.imageBase64.orEmpty()
            if (raw.isBlank()) {
                binding.loginCaptchaImage.setImageDrawable(null)
                return@observe
            }
            try {
                val payload = raw.substringAfter("base64,", raw).trim()
                val bytes = Base64.decode(payload, Base64.DEFAULT)
                binding.loginCaptchaImage.setImageBitmap(
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                )
            } catch (_: Exception) {
                binding.loginCaptchaImage.setImageDrawable(null)
            }
        }
        refreshSubmitEnabled()
    }
}
