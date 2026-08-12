package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.viewModels
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setInputTypeKeepingCursor
import com.cboda.baselibrary.utils.setIsNumAndEngAndSymbol
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.UpdatePwdActivityBinding
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class UpdatePwdActivity : BaseSimple2BroadActivity() {

    private val match1 = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,16}$"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? UpdatePwdActivityBinding)
        setRootBgColor(R.color.white)
        // 标题在内容区展示，工具栏仅保留返回
        getToolbarTitle()?.text = ""
        mBinding?.oldPwdEt?.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        mBinding?.newPwdEt?.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        mBinding?.againPwdEt?.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_E5E5E5)
        mBinding?.newPwdEt?.setIsNumAndEngAndSymbol(16)
        mBinding?.oldPwdEt?.setEdTextChange { checkPwd(false) }
        mBinding?.newPwdEt?.setEdTextChange { checkPwd(false) }
        mBinding?.againPwdEt?.setEdTextChange { checkPwd(false) }
        mBinding?.sureBtn?.click { checkPwd(true) }
        mBinding?.cancelBtn?.click { finish() }
        bindPwdLookTouch(mBinding?.oldPwdLook, mBinding?.oldPwdEt)
        bindPwdLookTouch(mBinding?.newPwdLook, mBinding?.newPwdEt)
        bindPwdLookTouch(mBinding?.againPwdLook, mBinding?.againPwdEt)
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

    private fun checkPwd(isUpdate: Boolean) {
        val oldPwd = mBinding?.oldPwdEt?.text?.toString() ?: ""
        val pwd = mBinding?.newPwdEt?.text?.toString() ?: ""
        val againPwd = mBinding?.againPwdEt?.text?.toString() ?: ""
        if (TextUtils.isEmpty(oldPwd)
            || TextUtils.isEmpty(pwd)
            || TextUtils.isEmpty(againPwd)
            || pwd != againPwd
            || !pwd.matches(Regex(match1))
        ) {
            mBinding?.sureBtn?.alpha = 0.3f
        } else {
            mBinding?.sureBtn?.alpha = 1f
        }
        if (!isUpdate) {
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
        if (!pwd.matches(Regex(match1))) {
            showToastMain(com.cboda.baselibrary.R.string.password_not_is_num)
            return
        }
        // 接口目前仅提交新密码；原密码用于表单校验展示对齐设计稿
        viewModel.updatePwd(pwd).observe(this) {
            finish()
            showToastMain(com.cboda.baselibrary.R.string.password_update_success)
        }
    }

    private var mBinding: UpdatePwdActivityBinding? = null

    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = UpdatePwdActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true

    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = false
    override fun showOrHideTitleLayout() = true
}
