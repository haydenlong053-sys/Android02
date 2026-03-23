package com.cboda.intelligent.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setTxt
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.LoginVerificationActivityBinding

class LoginVerificationActivity : BaseSimple2BroadActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? LoginVerificationActivityBinding)
        mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.color_111B40)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout()
        backIcon.imageTintList = ColorStateList.valueOf(getAppColor(this@LoginVerificationActivity, R.color.white))
        getToolbarTitle()?.setTxt(com.cboda.baselibrary.R.string.security_verification)
        getToolbarTitle()?.setTxtColor(R.color.white)
    }

    private var mBinding: LoginVerificationActivityBinding? = null

    //    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = LoginVerificationActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
}