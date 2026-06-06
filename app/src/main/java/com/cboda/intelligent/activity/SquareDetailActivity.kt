package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.SquareDetailActivityBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import kotlin.getValue

class SquareDetailActivity : BaseSimple2BroadActivity() {
    private var mBinding: SquareDetailActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? SquareDetailActivityBinding)
        setRootBgColor(R.color.white)
        setTitle(R.string.详情)
    }

    override fun getViewBindingClass() = SquareDetailActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true
}