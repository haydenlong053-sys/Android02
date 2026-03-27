package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.LoginQrCodeActivityBinding
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import kotlin.getValue

class LoginQrCodeActivity : BaseSimple2BroadActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? LoginQrCodeActivityBinding)
//        setRootBgRes()
    }

    private var mBinding: LoginQrCodeActivityBinding? = null

    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = LoginQrCodeActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = true
}