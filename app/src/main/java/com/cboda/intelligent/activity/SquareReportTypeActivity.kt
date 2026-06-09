package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SquareReportTypeAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.SquareReportTypeActivityBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import kotlinx.coroutines.launch

class SquareReportTypeActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareReportTypeActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private val adapter = SquareReportTypeAdapter()
    private var targetType: String = TARGET_TYPE_SQUARE
    private var targetId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareReportTypeActivityBinding
        setRootBgColor(R.color.white)
        targetType = intent.getStringExtra(EXTRA_TARGET_TYPE).orEmpty().ifBlank { TARGET_TYPE_SQUARE }
        targetId = intent.getStringExtra(EXTRA_TARGET_ID).orEmpty()
        initView()
        loadReportTypes()
    }

    override fun getViewBindingClass() = SquareReportTypeActivityBinding::class.java
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true

    private fun initView() {
        mBinding?.squareReportClose?.setOnClickListener { finish() }
        mBinding?.squareReportTypeList?.apply {
            layoutManager = LinearLayoutManager(this@SquareReportTypeActivity)
            adapter = this@SquareReportTypeActivity.adapter
        }
        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.getItem(position)
            SquareReportSubmitActivity.start(
                context = this,
                targetType = targetType,
                targetId = targetId,
                reportConfigId = item.id.toString(),
            )
        }
        mBinding?.squareReportExplanation?.setOnClickListener {
            SquareReportExplanationActivity.start(this)
        }
    }

    private fun loadReportTypes() {
        lifecycleScope.launch {
            viewModel.getReportConfigList(targetType).observe(this@SquareReportTypeActivity) { list ->
                adapter.setList(list)
            }
        }
    }

    companion object {
        const val TARGET_TYPE_SQUARE = "square"
        const val TARGET_TYPE_MOMENT = "moment"

        private const val EXTRA_TARGET_TYPE = "targetType"
        private const val EXTRA_TARGET_ID = "targetId"

        fun start(context: Context, targetType: String, targetId: String) {
            context.startActivity(
                Intent(context, SquareReportTypeActivity::class.java)
                    .putExtra(EXTRA_TARGET_TYPE, targetType)
                    .putExtra(EXTRA_TARGET_ID, targetId),
            )
        }
    }
}
