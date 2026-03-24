package com.cboda.intelligent.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setIsNumAndEng
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.cboda.baselibrary.utils.setTxt
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.LoginVerificationActivityBinding
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

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

        checkLoginBean()

        if (loginBean.type == "scanLogin") {
//            viewModel.getScanLoginDetail(linkId).observe(this) {
//
//            }
        }

//        mBinding?.loginVerificationSecurityQuestionInput?.setEdTextChange {
//            mBinding?.loginVerificationBtn?.isSubmitCanGo(true)
//        }
    }


    private fun checkLoginBean() {
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