package com.cboda.intelligent.activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.cboda.baselibrary.bean.ComBottomListBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setInputTypeKeepingCursor
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.baselibrary.utils.showCommonBottomListPop
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.LoginBroadFragmentBinding

class LoginBroadFragment : BaseBroadFragment<LoginBroadFragmentBinding, LoginViewBroadModel>() {

    private var captchaId: String = ""
    /** 是否已因登录失败要求图形验证码（默认隐藏）。 */
    private var captchaRequired = false

    /** 与「去注册」同色，弹框选中项文字色 */
    private val authLinkColor = Color.parseColor("#6332ed")

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
        hideCaptcha()
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
        binding.loginSwitchLine.click {
            showLineSwitchPop()
        }
        binding.loginBtn.click {
            viewModel.pwdLogin(
                account = binding.loginAccount.text.toString(),
                pwd = binding.loginPwd.text.toString(),
                captchaId = captchaId,
                captcha = binding.loginCaptcha.text.toString(),
                requireCaptcha = captchaRequired,
                onNeedCaptcha = { showCaptchaAndRefresh() },
                onCaptchaInvalid = {
                    if (captchaRequired) {
                        refreshCaptcha()
                    }
                },
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

    private fun showLineSwitchPop() {
        val list = arrayListOf(
            ComBottomListBean().apply {
                itemContent = getString(com.cboda.baselibrary.R.string.line_one)
                isSelected = HttpsBroadConfig.isLineSelected(HttpsBroadConfig.HONGKONG_TYPE)
            },
            ComBottomListBean().apply {
                itemContent = getString(com.cboda.baselibrary.R.string.line_two)
                isSelected = HttpsBroadConfig.isLineSelected(HttpsBroadConfig.LINE_TYPE_2)
            },
        )
        showCommonBottomListPop(
            list = list,
            context = activity,
            selectedItemTextColor = authLinkColor,
        ) { pos, _ ->
            val type = if (pos == 0) HttpsBroadConfig.HONGKONG_TYPE else HttpsBroadConfig.LINE_TYPE_2
            HttpsBroadConfig.switchLine(type)
        }
    }

    private fun hideCaptcha() {
        captchaRequired = false
        captchaId = ""
        binding.loginCaptchaLayout.isGone = true
        binding.loginCaptcha.setText("")
        binding.loginCaptchaImage.setImageDrawable(null)
        refreshSubmitEnabled()
    }

    private fun showCaptchaAndRefresh() {
        captchaRequired = true
        binding.loginCaptchaLayout.isVisible = true
        refreshCaptcha()
    }

    private fun refreshSubmitEnabled() {
        val account = binding.loginAccount.text.toString()
        val pwd = binding.loginPwd.text.toString()
        val captchaOk = !captchaRequired || !TextUtils.isEmpty(binding.loginCaptcha.text.toString())
        binding.loginBtn.isSubmitCanGo(
            !TextUtils.isEmpty(account) && !TextUtils.isEmpty(pwd) && captchaOk,
            R.drawable.bg_auth_btn_disabled,
            R.drawable.bg_auth_btn,
        )
    }

    private fun refreshCaptcha() {
        if (!captchaRequired) {
            return
        }
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
