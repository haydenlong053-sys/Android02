package com.lxkj.aiim.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.AiAntiFraudActivityBinding
import com.lxkj.aiim.viewmodel.CommViewModel
import kotlin.getValue

class AiAntiFraudActivity : BaseSimple2Activity() {
    private var commonNavigator: CommonBroadNavigator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? AiAntiFraudActivityBinding)
        mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.white)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout() // 或直接调用 setLayoutParams 会自动触发
    }


    private var mBinding: AiAntiFraudActivityBinding? = null
    private val viewModel: CommViewModel by viewModels()
    override fun getViewBindingClass() = AiAntiFraudActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
}