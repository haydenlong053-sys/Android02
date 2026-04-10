package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cboda.baselibrary.base.BaseBroadAdapter
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.RoundedProgressBar
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.ComposeItem
import com.cboda.intelligent.databinding.FragmentCreditScoreComposeBinding
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class CreditScoreComposeFragment : BaseBroadFragment<FragmentCreditScoreComposeBinding, NewMirrorViewBroadModel>() {
    private val composeAdapter = ComposeAdapter()

    companion object {
        fun newInstance() = CreditScoreComposeFragment()
    }

    override fun initView() {
        super.initView()
        val rv = binding.recyclerView
        rv.adapter = composeAdapter
        composeAdapter.setData(buildTestData())
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = FragmentCreditScoreComposeBinding.inflate(inflater, parent, false)

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun buildTestData(): ArrayList<ComposeItem> {
        val list = arrayListOf<ComposeItem>()
        for (i in 1..10) { // 包含结束值
            val composeItem = ComposeItem()
            composeItem.title = "在线时长$i"
            composeItem.iconRes = R.mipmap.credit_score7
            composeItem.proColor = com.cboda.baselibrary.R.color.color_F4A32B
            list.add(composeItem)
        }
        return list
    }

    private class ComposeAdapter() : BaseBroadAdapter<ComposeItem>(R.layout.item_credit_score_compose) {

        override fun onBindData(holder: BaseViewHolder, bean: ComposeItem) {
            holder.apply {
                getView<ImageView>(R.id.itemImg).setImageResource(bean.iconRes)
                getView<TextView>(R.id.itemTitle).text = bean.title
                getView<TextView>(R.id.itemProTv).text = "${anyToInt(bean.currentScore)}"
                getView<TextView>(R.id.itemMaxProTv).text = "/${anyToInt(bean.maxLimit)}"
                getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.currentScore, bean.maxLimit, com.cboda.baselibrary.R.color.black_12, bean.proColor)
                getView<TextView>(R.id.itemContent).text = bean.baseScore
                getView<TextView>(R.id.itemDesc).text = bean.vipBonusRate
                setGone(R.id.lineView, layoutPosition == 0)
            }
        }


    }
}

