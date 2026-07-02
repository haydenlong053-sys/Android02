package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.AbsentKickRulesBean
import com.cboda.intelligent.databinding.ActivityAbsentKickRulesBroadBinding
import com.cboda.intelligent.viewmodel.AbsentKickRulesViewBroadModel
import kotlinx.coroutines.launch

/** 缺席自动移出规则说明（对齐设计稿，参考 {@link SafeCenterBroadActivity}）。 */
class AbsentKickRulesBroadActivity : BaseSimple2BroadActivity() {

    private val viewModel: AbsentKickRulesViewBroadModel by viewModels()
    private var contentBinding: ActivityAbsentKickRulesBroadBinding? = null

    override fun getLayoutId(): Int = R.layout.activity_absent_kick_rules_broad

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityAbsentKickRulesBroadBinding::class.java
    }

    override fun useViewBinding(): Boolean = true

    override fun showBgTopTitleLayout(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        contentBinding = binding as? ActivityAbsentKickRulesBroadBinding
        getToolbarTitle()?.text = getString(R.string.absent_kick_rules_title)
        loadRules()
    }

    private fun loadRules() {
        lifecycleScope.launch {
            viewModel.loadRules().observe(this@AbsentKickRulesBroadActivity) { rules ->
                bindRules(rules)
            }
        }
    }

    private fun bindRules(rules: AbsentKickRulesBean?) {
        val binding = contentBinding ?: return
        binding.tvRuleTitle.text = rules?.ruleTitle?.takeIf { it.isNotBlank() }
            ?: getString(R.string.absent_kick_rule_title_fallback)

        binding.llDescriptions.removeAllViews()
        val descriptions = rules?.descriptions?.filter { it.isNotBlank() }.orEmpty()
        if (descriptions.isEmpty()) {
            addDescriptionItem(binding.llDescriptions, getString(R.string.absent_kick_rule_empty))
            return
        }
        descriptions.forEach { text ->
            addDescriptionItem(binding.llDescriptions, text)
        }
    }

    private fun addDescriptionItem(parent: LinearLayout, text: String) {
        val item = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = if (parent.childCount == 0) 0 else dp(10)
            }
            this.text = "• $text"
            setTextColor(getColor(R.color.black))
            textSize = 16f
            gravity = Gravity.START
        }
        parent.addView(item)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
