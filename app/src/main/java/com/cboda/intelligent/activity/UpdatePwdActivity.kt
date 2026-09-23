package com.cboda.intelligent.activity

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.viewModels
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setInputTypeKeepingCursor
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.UpdatePwdActivityBinding
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

/**
 * 修改登录密码：对齐登录页表单风格，对接 POST /im/user/changePassword。
 * 展示账号取自 GET /im/user/account。
 */
class UpdatePwdActivity : BaseSimple2BroadActivity() {

    private var mBinding: UpdatePwdActivityBinding? = null
    private val viewModel: LoginViewBroadModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? UpdatePwdActivityBinding
        setRootBgColor(R.color.color_msg_page_bg)
        getToolbarTitle()?.text = getString(com.cboda.baselibrary.R.string.update_login_pwd)
        styleToolbarLikeSettings()
        setIvBgTopImg(R.drawable.ic_login_big_bg)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout() // 或直接调用 setLayoutParams 会自动触发

        mBinding?.oldPwdEt?.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        mBinding?.newPwdEt?.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        mBinding?.againPwdEt?.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)

        mBinding?.oldPwdEt?.setEdTextChange { refreshSubmitEnabled() }
        mBinding?.newPwdEt?.setEdTextChange { refreshSubmitEnabled() }
        mBinding?.againPwdEt?.setEdTextChange { refreshSubmitEnabled() }
        mBinding?.sureBtn?.click { submitChange() }

        bindPwdLookTouch(mBinding?.oldPwdLook, mBinding?.oldPwdEt)
        bindPwdLookTouch(mBinding?.newPwdLook, mBinding?.newPwdEt)
        bindPwdLookTouch(mBinding?.againPwdLook, mBinding?.againPwdEt)

        loadAccount()
        refreshSubmitEnabled()
    }

    private fun loadAccount() {
        viewModel.fetchLoginAccount().observe(this) { account ->
            if (!account.isNullOrEmpty()) {
                mBinding?.accountEt?.setText(account)
            }
        }
    }

    private fun styleToolbarLikeSettings() {
        if (backIcon != null) {
            val density = resources.displayMetrics.density
            val size = (density * 36f).toInt()
            val padding = (density * 10f).toInt()
            val marginStart = (density * 12f).toInt()
            val lp = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setMargins(marginStart, 0, 0, 0)
            }
            backIcon.layoutParams = lp
            backIcon.setPadding(padding, padding, padding, padding)
            backIcon.setBackgroundResource(R.drawable.bg_circle_white_shadow)
            backIcon.setImageResource(R.mipmap.ic_broad_back)
            backIcon.clearColorFilter()
            backIcon.setColorFilter(0xFF333333.toInt(), PorterDuff.Mode.SRC_IN)
        }
        getToolbarTitle()?.setTextColor(0xFF1A1A1C.toInt())
    }

    private fun bindPwdLookTouch(lookBtn: ImageButton?, editText: EditText?) {
        if (lookBtn == null || editText == null) {
            return
        }
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

    private fun refreshSubmitEnabled() {
        val oldPwd = mBinding?.oldPwdEt?.text?.toString().orEmpty()
        val pwd = mBinding?.newPwdEt?.text?.toString().orEmpty()
        val againPwd = mBinding?.againPwdEt?.text?.toString().orEmpty()
        val ok = oldPwd.isNotEmpty()
            && pwd.isNotEmpty()
            && againPwd.isNotEmpty()
            && pwd == againPwd
        mBinding?.sureBtn?.alpha = if (ok) 1f else 0.7f
    }

    private fun submitChange() {
        val oldPwd = mBinding?.oldPwdEt?.text?.toString().orEmpty()
        val pwd = mBinding?.newPwdEt?.text?.toString().orEmpty()
        val againPwd = mBinding?.againPwdEt?.text?.toString().orEmpty()
        val account = mBinding?.accountEt?.text?.toString().orEmpty()

        if (TextUtils.isEmpty(account)) {
            showToastMain(R.string.please_enter_account)
            return
        }
        if (TextUtils.isEmpty(oldPwd)) {
            showToastMain(R.string.请输入原密码)
            return
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain(com.cboda.baselibrary.R.string.please_input_new_pwd)
            return
        }
        if (TextUtils.isEmpty(againPwd)) {
            showToastMain(R.string.请再次输入密码)
            return
        }
        if (pwd != againPwd) {
            showToastMain(com.cboda.baselibrary.R.string.again_pwd_inconsistent)
            return
        }

        viewModel.changeLoginPassword(account, oldPwd, pwd).observe(this) { bean ->
            if (bean == null) {
                return@observe
            }
            val revoked = bean.revokedOtherSessions
            if (revoked > 0) {
                showToastMain(
                    getString(R.string.change_pwd_revoked_sessions, revoked)
                )
            } else {
                showToastMain(com.cboda.baselibrary.R.string.password_update_success)
            }
            finish()
        }
    }

    override fun getViewBindingClass() = UpdatePwdActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = false
    override fun showOrHideTitleLayout() = true
}
