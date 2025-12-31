package com.lxkj.aiim.lj

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.cboda.baselibrary.utils.removeTrailingZeros
import com.cboda.baselibrary.utils.setLeftImg
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivityCheckResultBinding
import com.lxkj.aiim.lj.adapter.CheckResultAdapter
import com.lxkj.aiim.lj.bean.AiCheckResultBean
import com.lxkj.aiim.lj.bean.CheckResultInfo
import com.lxkj.aiim.lj.bean.CheckResultItem
import com.lxkj.aiim.utils.JumpUtils.Companion.startMainActivity
import com.lxkj.aiim.viewmodel.CheckAiViewModel
import kotlinx.coroutines.launch


class AiCheckResultActivity : BaseSimple2Activity() {
    private val viewModel: CheckAiViewModel by viewModels()
    private var mBind: ActivityCheckResultBinding? = null
    private lateinit var adapter: CheckResultAdapter
    private var aiBean: AiCheckResultBean? = null
    private val aiShowBean = CheckResultInfo()
    private var id = 0

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_check_result
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityCheckResultBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = binding as? ActivityCheckResultBinding
        aiBean = intent.getSerializableExtra("ai_data") as? AiCheckResultBean
        id = intent.getIntExtra("id", 0)
        setRootBgColor(getColor(com.cboda.baselibrary.R.color.color_F4F8FD))
        mBind?.toolbar?.mTvTitle?.text = getString(R.string.ai_check_result)
        mBind?.toolbar?.mViewBack?.setOnClickListener {
            finish()
        }

        initRecyclerView()
        initListener()
        initData()
    }

    //AI_PAGE
    private fun initData() {
        aiBean?.let {
            dealWithData(it)
        } ?: run {
            lifecycleScope.launch {
                viewModel.goAiResultInfo(id).observe(this@AiCheckResultActivity) {
                    dealWithData(it)
                }
            }
        }
    }

    private fun dealWithData(bean: AiCheckResultBean) {
        aiShowBean.mList.clear()
        bean.multiModing?.let {
            //多模态
            aiShowBean.riskLevel = it.riskLevel
            aiShowBean.riskValue = it.riskValue?.toDoubleOrNull() ?: 0.0
            it.analysisReport?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.facial_features),
                        bean.description,
                        bean.isNormal,
                        bean.verdict
                    )
                )
            }
            it.voiceprintAnalysis?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.voiceprint_analysis),
                        bean.description,
                        bean.isNormal,
                        bean.verdict
                    )
                )
            }
            it.environmentConsistency?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.environment_consistency),
                        bean.description,
                        bean.isNormal,
                        bean.verdict
                    )
                )
            }
            it.metadata?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.metadata),
                        bean.description,
                        bean.isNormal,
                        bean.verdict
                    )
                )
            }

        }
        bean.document?.projectDocument?.let {
            //项目类
            aiShowBean.riskLevel = it.riskLevel
            aiShowBean.riskValue = it.riskValue?.toDoubleOrNull() ?: 0.0
            it.teamBackground?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.team_background),
                        bean.description,
                        bean.isNormal,
                        bean.status
                    )
                )
            }
            it.businessLogic?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.business_logic),
                        bean.description,
                        bean.isNormal,
                        bean.riskFeature
                    )
                )
            }
            it.complianceStatus?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.compliance),
                        bean.description,
                        bean.isNormal,
                        bean.filingStatus
                    )
                )
            }
            it.capitalFlow?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.fund_flow),
                        bean.description,
                        bean.isNormal,
                        bean.flowStatus
                    )
                )
            }

        }
        bean.document?.contractDocument?.let {
            //合同类
            aiShowBean.riskLevel = it.riskLevel
            aiShowBean.riskValue = it.riskValue?.toDoubleOrNull() ?: 0.0
            it.keyClause?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.key_terms),
                        bean.description,
                        bean.isNormal,
                        bean.riskTag
                    )
                )
            }
            it.hiddenTrap?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.hidden_traps),
                        bean.description,
                        bean.isNormal,
                        bean.clauseType
                    )
                )
            }
            it.responsibility?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.liability_allocation),
                        bean.description,
                        bean.isNormal,
                        bean.allocationStatus
                    )
                )
            }
            it.jurisdictionCourt?.let { bean ->
                aiShowBean.mList.add(
                    CheckResultItem(
                        getString(R.string.jurisdiction_court),
                        bean.description,
                        bean.isNormal,
                        bean.locationTag
                    )
                )
            }

        }
        initRiskWarning(aiShowBean.riskValue)
        adapter.setList(aiShowBean.mList)

    }

    /**
     * 初始化高风险预警卡片
     */
    private fun initRiskWarning(text: Double) {
        // 设置风险百分比（可以从 Intent 或数据中获取，这里使用默认值 92%）
        mBind?.tvRiskPercentage?.text = (text * 100).toString().removeTrailingZeros()
        mBind?.tvRiskPercentage?.text?.toString()?.toIntOrNull()?.let {
            mBind?.viewCircleBg?.setProgress(it)
        }
//        >=90、>=65、>=35、>=10
        val value = (aiShowBean.riskValue * 100)
        if (value >= 0 && value < 10) {
            mBind?.tvRiskWarningTitle?.text = getString(R.string.no_risk_warning)
            mBind?.tvRiskWarningTitle?.setLeftImg(R.mipmap.ic_ai_dangerous_low)
        } else if (value >= 10 && value < 35) {
            mBind?.tvRiskWarningTitle?.text = getString(R.string.low_risk_warning)
            mBind?.tvRiskWarningTitle?.setLeftImg(R.mipmap.ic_ai_dangerous_low)
        } else if (value >= 35 && value < 65) {
            mBind?.tvRiskWarningTitle?.text = getString(R.string.mid_risk_warning)
            mBind?.tvRiskWarningTitle?.setLeftImg(R.mipmap.ic_ai_dangerous_mid)
        } else if (value >= 65 && value < 90) {
            mBind?.tvRiskWarningTitle?.text = getString(R.string.high_risk_warning)
            mBind?.tvRiskWarningTitle?.setLeftImg(R.mipmap.ic_ai_dangerous)
        } else if (value >= 90) {
            mBind?.tvRiskWarningTitle?.text = getString(R.string.ai_risk_warning)
            mBind?.tvRiskWarningTitle?.setLeftImg(R.mipmap.ic_ai_dangerous)
        } else {
            mBind?.tvRiskWarningTitle?.text = getString(R.string.unknow_risk_warning)
            mBind?.tvRiskWarningTitle?.setLeftImg(R.mipmap.ic_ai_dangerous_low)
        }
    }

    /**
     * 初始化 RecyclerView
     */
    private fun initRecyclerView() {
        adapter = CheckResultAdapter()
        mBind?.rvCheckResult?.layoutManager = LinearLayoutManager(this)
        mBind?.rvCheckResult?.adapter = adapter
    }

    /**
     * 初始化监听器
     */
    private fun initListener() {
        // 返回首页按钮点击事件
        mBind?.btnReturnHome?.setOnClickListener {
            startMainActivity(this@AiCheckResultActivity)
        }
    }

    override fun onResume() {
        super.onResume()
//        simulateProgress()
    }

    private fun simulateProgress() {
        mBind?.viewCircleBg?.setProgress(60)
        val animator = ValueAnimator.ofInt(0, 100)
        animator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val progress = animation.getAnimatedValue() as Int
//                mBind?.viewCircleBg?.setProgress(progress)
            }
        })
        animator.setRepeatCount(ValueAnimator.INFINITE)
        animator.setDuration(4000)
        animator.start()
    }

}