package com.cboda.intelligent.activity

import android.os.Bundle
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.SquareUserActivityBinding

class SquareUserActivity : BaseSimple2BroadActivity() {
    private var mBinding: SquareUserActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? SquareUserActivityBinding)
        setRootBgColor(R.color.white)

    }

    override fun getViewBindingClass() = SquareUserActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true
}