package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.AiAntiFraudBroadActivityBinding
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import kotlin.getValue

class AiAntiFraudBroadActivity : BaseSimple2BroadActivity() {
    private var commonNavigator: CommonBroadNavigator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? AiAntiFraudBroadActivityBinding)
        mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.white)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout() // 或直接调用 setLayoutParams 会自动触发
    }


    private var mBinding: AiAntiFraudBroadActivityBinding? = null
    private val viewModel: CommViewBroadModel by viewModels()
    override fun getViewBindingClass() = AiAntiFraudBroadActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
}