package com.cboda.intelligent.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setIsNumAndEng
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.baselibrary.utils.setTxt
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.databinding.LoginVerificationActivityBinding
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import okhttp3.Response
import java.lang.Exception

class LoginVerificationActivity : BaseSimple2BroadActivity() {

    private val loginBean by lazy(LazyThreadSafetyMode.NONE) { fromJson2(intent?.getStringExtra("loginBean") ?: "", LoginBean::class.java) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? LoginVerificationActivityBinding)
        mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.color_111B40)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout()
        backIcon.imageTintList = ColorStateList.valueOf(getAppColor(this@LoginVerificationActivity, R.color.white))
        getToolbarTitle()?.setTxt(com.cboda.baselibrary.R.string.security_verification)
        getToolbarTitle()?.setTxtColor(R.color.white)
        mBinding?.loginVerificationOldCode?.setIsNumAndEng(8)
        mBinding?.loginVerificationSecurityQuestionInput?.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        mBinding?.loginVerificationPayPwd?.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        mBinding?.loginVerificationOldCode?.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        mBinding?.loginVerificationGoogleCode?.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        mBinding?.loginVerificationBtn?.isSubmitCanGo(false)
        mBinding?.loginVerificationSecurityQuestionInput?.setEdTextChange {
            checkSubmit(false)
        }
        mBinding?.loginVerificationPayPwd?.setEdTextChange {
            checkSubmit(false)
        }
        mBinding?.loginVerificationOldCode?.setEdTextChange {
            checkSubmit(false)
        }
        mBinding?.loginVerificationGoogleCode?.setEdTextChange {
            checkSubmit(false)
        }
        mBinding?.loginVerificationBtn?.click {
            checkSubmit(true)
        }
        checkLoginBean()
    }


    private fun checkSubmit(isLogin: Boolean) {
        val securityQuestion = mBinding?.loginVerificationSecurityQuestionInput?.text?.toString()?.trim()
        val payPwd = mBinding?.loginVerificationPayPwd?.text?.toString()?.trim()
        val oldDevCode = mBinding?.loginVerificationOldCode?.text?.toString()?.trim()
        val googleCode = mBinding?.loginVerificationGoogleCode?.text?.toString()?.trim()
        if (loginBean.existMbName && TextUtils.isEmpty(securityQuestion)) {
            checkIsLogin(isLogin)
            return
        }
        if (loginBean.existPayPassword && TextUtils.isEmpty(payPwd)) {
            checkIsLogin(isLogin)
            return
        }
        if (loginBean.haveOldDeviceCode && TextUtils.isEmpty(oldDevCode)) {
            checkIsLogin(isLogin)
            return
        }
        if (loginBean.twoFactorCode && TextUtils.isEmpty(googleCode)) {
            checkIsLogin(isLogin)
            return
        }
        if (isLogin && !TextUtils.isEmpty(loginBean.linkId)) {
//            val loginIdBean = LoginIdBean()
//            loginIdBean.securityQuestion = securityQuestion ?: ""
//            loginIdBean.payPwd = payPwd ?: ""
//            loginIdBean.googleCode = googleCode ?: ""
//            loginIdBean.oldDevCode = oldDevCode ?: ""
//            loginIdBean.linkId = loginBean.linkId
            val params = hashMapOf("id" to loginBean.linkId, "clientId" to ChatManagerBroadHolder.gChatManager.getClientId(), "type" to 2)
            if (!TextUtils.isEmpty(googleCode)) {
                params["googleCode"] = googleCode
            }
            if (!TextUtils.isEmpty(oldDevCode)) {
                params["oldDeviceCode"] = oldDevCode
            }
            if (!TextUtils.isEmpty(payPwd)) {
                params["paywd"] = payPwd
            }
            if (!TextUtils.isEmpty(securityQuestion)) {
                params["mbda"] = securityQuestion
            }

            OkHttpBroadHelper.getInstance().post_json(this, HttpsBroadConfig.appBaseUrl + "/idLogin", params.toMap(), object : SpotsCallBroadBack<ResultBroadBean>(this) {
                override fun onSuccess(response: Response?, resultBean: ResultBroadBean?) {
                    resultBean?.let {
                        AppBroadUtils.imLogin(this@LoginVerificationActivity, resultBean.apptoken, resultBean.userId, resultBean.imtoken)
                    } ?: showToastMain(com.cboda.baselibrary.R.string.device_error)
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {
//
                }
            })
//            viewModel.loginQrCode(loginIdBean).observe(this) { resultBean ->
//                resultBean?.let {
//                    AppBroadUtils.imLogin(this, resultBean.apptoken, resultBean.userId, resultBean.imtoken)
//                }
//            }
        } else if (isLogin && !TextUtils.isEmpty(loginBean.loginPwd) && !TextUtils.isEmpty(loginBean.loginAccount)) {
            viewModel.pwdLogin(loginBean.loginAccount, loginBean.loginPwd, "", payPwd ?: "", googleCode ?: "", securityQuestion ?: "")
        } else {
            mBinding?.loginVerificationBtn?.isSubmitCanGo(true)
        }
    }


    private fun checkIsLogin(isLogin: Boolean) {
        if (isLogin) {
            showToastMain(com.cboda.baselibrary.R.string.device_error)
        } else {
            mBinding?.loginVerificationBtn?.isSubmitCanGo(false)
        }
    }

    private fun checkLoginBean() {
        mBinding?.loginVerificationSecurityQuestion?.text = loginBean.securityQuestion
        mBinding?.loginVerificationSecurityQuestionBefore?.isGone = !loginBean.existMbName
        mBinding?.loginVerificationSecurityQuestion?.isGone = !loginBean.existMbName
        mBinding?.loginVerificationSecurityQuestionInput?.isGone = !loginBean.existMbName
        mBinding?.loginVerificationPayPwdBefore?.isGone = !loginBean.existPayPassword
        mBinding?.loginVerificationPayPwd?.isGone = !loginBean.existPayPassword
        mBinding?.loginVerificationOldCodeBefore?.isGone = !loginBean.haveOldDeviceCode
        mBinding?.loginVerificationOldCode?.isGone = !loginBean.haveOldDeviceCode
        mBinding?.loginVerificationGoogleCodeBefore?.isGone = !loginBean.twoFactorCode
        mBinding?.loginVerificationGoogleCode?.isGone = !loginBean.twoFactorCode
    }

    private var mBinding: LoginVerificationActivityBinding? = null

    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = LoginVerificationActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
}