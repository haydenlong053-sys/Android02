package com.cboda.intelligent.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.base.BaseBroadAdapter
import com.cboda.baselibrary.base.BaseFragment
import com.cboda.baselibrary.utils.anyToFloat
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.widge.RoundedProgressBar
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.FragmentCreditScoreComposeBinding
import com.cboda.intelligent.databinding.ItemCreditScoreComposeBinding
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class CreditScoreComposeFragment : BaseBroadFragment<FragmentCreditScoreComposeBinding, LoginViewBroadModel>() {
    private val composeAdapter = ComposeAdapter()

    companion object {
        fun newInstance() = CreditScoreComposeFragment()
    }

    override fun initView() {
        super.initView()
        val rv = binding.recyclerView
        rv.adapter = composeAdapter
//        composeAdapter.setData(buildTestData())
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

    private class ComposeItem() {
        var iconRes: Int = 0
        var title: String = ""
        var content: String = "基础：+0.2/USDT"
        var desc: String = "会员+10%  靓号+10%"
        var progress: String = "140"
        var maxProgress = "150"
        var proColor: Int = 0
    }

    private class ComposeAdapter() : BaseBroadAdapter<ComposeItem>(R.layout.item_credit_score_compose) {

        override fun onBindData(holder: BaseViewHolder, bean: ComposeItem) {
            holder.apply {
                getView<ImageView>(R.id.itemImg).setImageResource(bean.iconRes)
                getView<TextView>(R.id.itemTitle).text = bean.title
                getView<TextView>(R.id.itemProTv).text = "${anyToInt(bean.progress)}"
                getView<TextView>(R.id.itemMaxProTv).text = "/${anyToInt(bean.maxProgress)}"
                getView<RoundedProgressBar>(R.id.itemPro).setProgress(bean.progress, bean.maxProgress, com.cboda.baselibrary.R.color.black_12, bean.proColor)
                getView<TextView>(R.id.itemContent).text = bean.content
                getView<TextView>(R.id.itemDesc).text = bean.desc
                setGone(R.id.lineView, layoutPosition == 0)
            }
        }


    }
}

