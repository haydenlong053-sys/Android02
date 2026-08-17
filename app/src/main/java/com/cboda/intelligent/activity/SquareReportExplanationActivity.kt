package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.SquareReportExplanationActivityBinding
import com.cboda.baselibrary.utils.getAppNamedStr

class SquareReportExplanationActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareReportExplanationActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareReportExplanationActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        initView()
    }

    private fun initView() {
        mBinding?.closeView?.setOnClickListener { finish() }
        mBinding?.contentTv?.text = getAppNamedStr(com.cboda.baselibrary.R.string.square_report_explanation_content, this)
    }

    override fun getViewBindingClass() = SquareReportExplanationActivityBinding::class.java
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SquareReportExplanationActivity::class.java))
        }
    }
}
