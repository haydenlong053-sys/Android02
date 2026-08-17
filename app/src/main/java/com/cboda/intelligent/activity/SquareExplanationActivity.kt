package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.utils.getAppNamedStr
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.SquareHotConfigBean
import com.cboda.intelligent.databinding.SquareExplanationActivityBinding
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import kotlinx.coroutines.launch

class SquareExplanationActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareExplanationActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareExplanationActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        initView()
        loadRules()
    }

    private fun initView() {
        val binding = mBinding ?: return
        binding.returnView.click { finish() }
        binding.backIcon.click { finish() }
        binding.confirmBtn.click { finish() }
    }

    private fun loadRules() {
        lifecycleScope.launch {
            viewModel.getSquareHotConfig().observe(this@SquareExplanationActivity) { config ->
                bindRules(config)
            }
        }
    }

    private fun bindRules(config: SquareHotConfigBean?) {
        val binding = mBinding ?: return
        val stakeRule = config?.publishRules?.stakeRule
        val auditRule = config?.publishRules?.auditRule

        binding.stakeTitle.text = stakeRule?.title?.takeIf { it.isNotBlank() }
            ?: getString(com.cboda.baselibrary.R.string.square_publish_rules_stake_title)
        binding.stakeContent.text = stakeRule?.content?.takeIf { it.isNotBlank() }
            ?: getString(com.cboda.baselibrary.R.string.square_publish_rules_stake_content)

        binding.auditTitle.text = auditRule?.title?.takeIf { it.isNotBlank() }
            ?: getString(com.cboda.baselibrary.R.string.square_publish_rules_audit_title)
        binding.auditContent.text = auditRule?.content?.takeIf { it.isNotBlank() }
            ?: getAppNamedStr(com.cboda.baselibrary.R.string.square_publish_rules_audit_content, this)
    }

    override fun getViewBindingClass() = SquareExplanationActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = false

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SquareExplanationActivity::class.java))
        }
    }
}
