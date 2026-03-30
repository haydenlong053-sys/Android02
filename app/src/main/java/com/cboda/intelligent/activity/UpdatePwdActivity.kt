package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setIsNumAndEngAndSymbol
import com.cboda.baselibrary.utils.setTxt
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
        getToolbarTitle()?.setTxt(com.cboda.baselibrary.R.string.update_login_pwd2)
        mBinding?.newPwdEt?.setIsNumAndEngAndSymbol(16)
        mBinding?.newPwdEt?.setEdTextChange {
            checkPwd(false)
        }
        mBinding?.againPwdEt?.setEdTextChange {
            checkPwd(false)
        }
        mBinding?.sureBtn?.click {
            checkPwd(true)
        }
        mBinding?.cancelBtn?.click {
            finish()
        }
    }


    private fun checkPwd(isUpdate: Boolean) {
        val pwd = mBinding?.newPwdEt?.text?.toString() ?: ""
        val againPwd = mBinding?.againPwdEt?.text?.toString() ?: ""
        if (!TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(againPwd)) {
            if (pwd != againPwd) {
                mBinding?.sureBtn?.alpha = 0.3f
                if (isUpdate) {
                    showToastMain(com.cboda.baselibrary.R.string.again_pwd_inconsistent)
                    return
                }
            } else if (!pwd.matches(Regex(match1))) {
                mBinding?.sureBtn?.alpha = 0.3f
                if (isUpdate) {
                    showToastMain(com.cboda.baselibrary.R.string.password_not_is_num)
                    return
                }
            } else {
                mBinding?.sureBtn?.alpha = 1f
                if (isUpdate) {

                }
            }
        } else if (TextUtils.isEmpty(pwd)) {
            mBinding?.sureBtn?.alpha = 0.3f
            if (isUpdate) {
                showToastMain(com.cboda.baselibrary.R.string.please_input_new_pwd)
                return
            }
        } else if (TextUtils.isEmpty(againPwd)) {
            mBinding?.sureBtn?.alpha = 0.3f
            if (isUpdate) {
                showToastMain(R.string.请再次输入密码)
                return
            }
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