package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Base64
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

    private var captchaId: String = ""

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = RegisteredBroadFragmentBinding.inflate(inflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        binding.title2.text = getString(
            com.cboda.baselibrary.R.string.register_welcome_subtitle,
            getString(com.cboda.baselibrary.R.string.new_app_name_use),
        )
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginNickname.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        // 二次确认密码：暂时隐藏
        // binding.updatePwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginCaptcha.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        binding.loginBtn.isSubmitCanGo(
            false,
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
        val uuIdCreate = getUuIdCreate()
        binding.content.text =
            (uuIdCreate + EncryptUtils.encryptMD5ToString(uuIdCreate)).uppercase()
        refreshCaptcha()
    }

    override fun initListener() {
        super.initListener()
        binding.loginAccount.setEdTextChange {
            refreshSubmitEnabled()
            binding.loginLookPwd.isGone = TextUtils.isEmpty(it)
        }
        binding.loginNickname.setEdTextChange { refreshSubmitEnabled() }
        binding.loginPwd.setEdTextChange { refreshSubmitEnabled() }
        // 二次确认密码：暂时隐藏
        // binding.updatePwd.setEdTextChange { refreshSubmitEnabled() }
        binding.loginCaptcha.setEdTextChange { refreshSubmitEnabled() }
        binding.loginCaptchaImage.click { refreshCaptcha() }
        binding.loginCaptchaRefresh.click { refreshCaptcha() }
        binding.loginBtn.click {
            val pwd = binding.loginPwd.text.toString()
            viewModel.registerAccount(
                account = binding.loginAccount.text.toString(),
                nickname = binding.loginNickname.text.toString(),
                pwd = pwd,
                // 二次确认密码暂时关闭：与密码保持一致，恢复输入框后改回 binding.updatePwd
                secondPwd = pwd,
                // secondPwd = binding.updatePwd.text.toString(),
                captchaId = captchaId,
                captcha = binding.loginCaptcha.text.toString(),
                onCaptchaInvalid = { refreshCaptcha() },
            )
        }
        binding.loginLookPwd.click {
            binding.loginAccount.setText("")
        }
        bindPwdLookTouch(binding.loginPwdLook, binding.loginPwd)
        // 二次确认密码：暂时隐藏
        // bindPwdLookTouch(binding.updatePwdLook, binding.updatePwd)
    }

    private fun refreshSubmitEnabled() {
        val account = binding.loginAccount.text.toString()
        val nickname = binding.loginNickname.text.toString().trim()
        val pwd = binding.loginPwd.text.toString()
        // val secondPwd = binding.updatePwd.text.toString()
        val captcha = binding.loginCaptcha.text.toString()
        binding.loginBtn.isSubmitCanGo(
            !TextUtils.isEmpty(account)
                && !TextUtils.isEmpty(nickname)
                && !TextUtils.isEmpty(pwd)
                // && !TextUtils.isEmpty(secondPwd)
                && !TextUtils.isEmpty(captcha),
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
    }

    private fun refreshCaptcha() {
        binding.loginCaptcha.setText("")
        captchaId = ""
        viewModel.fetchCaptcha("register").observe(viewLifecycleOwner) { bean ->
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
