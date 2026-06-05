package com.cboda.intelligent.activity

import android.os.Bundle
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.SquareBlackActivityBinding

class SquareBlackActivity : BaseSimple2BroadActivity() {
    private var mBinding: SquareBlackActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? SquareBlackActivityBinding)
        setRootBgColor(R.color.white)

    }

    override fun getViewBindingClass() = SquareBlackActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true
}