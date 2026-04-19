package com.cboda.intelligent.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.TimeUtils
import com.cboda.baselibrary.base.BaseBroadAdapter
import com.cboda.baselibrary.common.CreditScoreTypeEnum
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.utils.subZeroAndDot
import com.cboda.baselibrary.widge.RoundedProgressBar
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.ComposeItem
import com.cboda.intelligent.databinding.FragmentCreditScoreComposeBinding
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

class CreditScoreComposeFragment : BaseBroadFragment<FragmentCreditScoreComposeBinding, NewMirrorViewBroadModel>() {
    private var page = 1
    private var totalPage: Int = 1
    private var typeAction = 0
    private val mAdapter by lazy {
        if (typeAction == 1) {
            ComposeAdapter(typeAction, R.layout.item_credit_score_compose_log)
        } else {
            ComposeAdapter(typeAction, R.layout.item_credit_score_compose)
        }

    }

    companion object {
        fun newInstance(type: Int = 0) = CreditScoreComposeFragment().apply {
            val args = Bundle()
            args.putInt("type", type)
            arguments = args
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = FragmentCreditScoreComposeBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
//        val rv = binding.mRvList
//        rv.adapter = composeAdapter
//        composeAdapter.setData(buildTestData())
        typeAction = arguments?.getInt("type") ?: 0
        initAdapter()
    }

    private fun initAdapter() {
        binding.mRvList.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->

        }

        binding.smart.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                page = 1
                initMyData()
                refreshLayout.setNoMoreData(false)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (page >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    return
                }
                page++
                initMyData()
            }
        })
        initMyData()
    }

    private fun initMyData() {
        if (typeAction == 1) {
            lifecycleScope.launch {
                viewModel.getLogPageList(page)
                    .observe(viewLifecycleOwner) { resultBean ->
                        showData(resultBean)
                    }

            }
        } else {
            binding.smart.isEnabled = false
            lifecycleScope.launch {
                viewModel.getCreditConstituteList()
                    .observe(viewLifecycleOwner) { resultBean ->
                        showData(resultBean)
                    }

            }
        }

    }

    private fun showData(resultBean: NetworkListBroadResult<ComposeItem>) {
        totalPage = resultBean.totalPage
        binding?.smart?.finishLoadMore()
        binding?.smart?.finishRefresh()

        if (page == 1) {
            mAdapter.setList(resultBean.list)
        } else {
            resultBean.list?.let {
                mAdapter.addData(it)
            }
        }
        if (resultBean.list?.isEmpty() == true) {
            binding?.llNoData?.visibility = View.VISIBLE
        } else {
            binding?.llNoData?.visibility = View.INVISIBLE
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


    private class ComposeAdapter(val typeAction: Int, layoutResId: Int) : BaseBroadAdapter<ComposeItem>(layoutResId) {

        @SuppressLint("SetTextI18n")
        override fun onBindData(holder: BaseViewHolder, bean: ComposeItem) {
            if (typeAction == 1) {
                //变化记录列表
                holder.apply {
                    when (bean.type) {
                        CreditScoreTypeEnum.ONLINE_TIME.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score1)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_online_duration_met)
                            getView<TextView>(R.id.itemContent).text =
                                String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), "+" + bean.baseScore)
                        }

                        CreditScoreTypeEnum.REGISTER_DAY.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score2)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_register_days)
                            getView<TextView>(R.id.itemContent).text =
                                String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), "+" + bean.baseScore)

                        }

                        CreditScoreTypeEnum.CONTRIBUTION.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score3)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_platform_contribution)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), bean.baseScore)
                        }

                        CreditScoreTypeEnum.VIOLATION.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score12)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_violation_deduct)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), bean.baseScore)
                        }

                        CreditScoreTypeEnum.SYSTEM_ADD.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score5)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_system_add)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), bean.baseScore)
                        }

                        CreditScoreTypeEnum.SYSTEM_DEDUCT.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score6)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_system_deduct)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), bean.baseScore)
                        }

                        CreditScoreTypeEnum.INVITE_USER.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score4)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_invite_friend)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_p), bean.baseScore)
                        }

                        CreditScoreTypeEnum.HOLD_ODIC.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score7)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_hold)
                            getView<TextView>(R.id.itemContent).text =
                                String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base), "+" + bean.baseScore)

                        }

                        CreditScoreTypeEnum.GUARANTEED_TRANSACTION.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score3)
                            getView<TextView>(R.id.itemTitle).text =
                                this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_guaranteed_transaction)
                            getView<TextView>(R.id.itemContent).text =
                                String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_usdt), "+" + bean.baseScore)
                        }

                        CreditScoreTypeEnum.ENDORSEMENT_GAINED.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score5)
                            getView<TextView>(R.id.itemTitle).text =
                                this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_endorsement_gained)
                            getView<TextView>(R.id.itemContent).text =
                                String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_p), "+" + bean.baseScore)
                        }

                    }
                    if (bean.score > 0) {
                        getView<TextView>(R.id.itemPointTv).setTextColor("#00AC56".toColorInt())
                        getView<TextView>(R.id.itemPointTv).text = "+${bean.score}"
                    } else {
                        getView<TextView>(R.id.itemPointTv).setTextColor("#DF0000".toColorInt())
                        getView<TextView>(R.id.itemPointTv).text = "${bean.score}"
                    }
                    getView<TextView>(R.id.itemDesc).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_vip_bonus), (bean.vipBonusRate * 100).toString() + "%")
                    getView<TextView>(R.id.itemDesc2).text =
                        String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_nice_id_bonus), (bean.lianghaoBonusRate * 100).toString() + "%")
                    getView<TextView>(R.id.itemDesc).isVisible = (bean.vipBonusRate > 0)
                    getView<TextView>(R.id.itemDesc2).isVisible = (bean.lianghaoBonusRate > 0)
                    if (bean.createTimestamp > 0) {
                        getView<TextView>(R.id.itemTimeTv).text = TimeUtils.millis2String(bean.createTimestamp, "yyyy-MM-dd")
                    } else {
                        getView<TextView>(R.id.itemTimeTv).text = ""
                    }

                    setGone(R.id.lineView, layoutPosition == 0)
                }
            } else {
                //信用构成
                holder.apply {
                    when (bean.type) {
                        CreditScoreTypeEnum.ONLINE_TIME.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score1)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_online_duration)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_day), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_B73BDD)
                        }

                        CreditScoreTypeEnum.REGISTER_DAY.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score2)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_register_days)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_day), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_6A76FF)
                        }

                        CreditScoreTypeEnum.CONTRIBUTION.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score6)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_platform_contribution)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_fen), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_53CC90)
                        }

                        CreditScoreTypeEnum.VIOLATION.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score12)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_violation_deduct)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_usdt), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_45BEDD)
                        }

                        CreditScoreTypeEnum.SYSTEM_ADD.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score5)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_system_add)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_usdt), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_AA68FF)
                        }

                        CreditScoreTypeEnum.SYSTEM_DEDUCT.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score6)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_system_deduct)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_usdt), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_F4A32B)
                        }

                        CreditScoreTypeEnum.INVITE_USER.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score4)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_invite_friend)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_p), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_EE56D4)
                        }

                        CreditScoreTypeEnum.HOLD_ODIC.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score7)
                            getView<TextView>(R.id.itemTitle).text = this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_hold)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_mei), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_EE56D4)
                        }

                        CreditScoreTypeEnum.GUARANTEED_TRANSACTION.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score3)
                            getView<TextView>(R.id.itemTitle).text =
                                this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_guaranteed_transaction)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_usdt), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_53CC90)
                        }

                        CreditScoreTypeEnum.ENDORSEMENT_GAINED.code -> {
                            getView<ImageView>(R.id.itemImg).setImageResource(R.mipmap.credit_score5)
                            getView<TextView>(R.id.itemTitle).text =
                                this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_label_endorsement_gained)
                            getView<TextView>(R.id.itemContent).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_base_p), bean.baseScore)
                            getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, com.cboda.baselibrary.R.color.color_AA68FF)
                        }

                    }


                    getView<TextView>(R.id.itemProTv).text = subZeroAndDot(bean.currentScore)
                    getView<TextView>(R.id.itemMaxProTv).text = "/${subZeroAndDot(bean.maxLimit)}"


                    getView<TextView>(R.id.itemDesc).text = String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_vip_bonus), (bean.vipBonusRate * 100).toString() + "%")
                    getView<TextView>(R.id.itemDesc2).text =
                        String.format(this.itemView.resources.getString(com.cboda.baselibrary.R.string.credit_score_fmt_breakdown_nice_id_bonus), (bean.lianghaoBonusRate * 100).toString() + "%")
                    getView<TextView>(R.id.itemDesc).isVisible = (bean.vipBonusRate > 0)
                    getView<TextView>(R.id.itemDesc2).isVisible = (bean.lianghaoBonusRate > 0)
                    setGone(R.id.lineView, layoutPosition == 0)
                }
            }

        }


    }
}

