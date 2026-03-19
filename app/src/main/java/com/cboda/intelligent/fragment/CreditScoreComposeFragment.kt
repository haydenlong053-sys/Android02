package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.FragmentCreditScoreComposeBinding
import com.cboda.intelligent.databinding.ItemCreditScoreComposeBinding

class CreditScoreComposeFragment : Fragment() {

    companion object {
        fun newInstance() = CreditScoreComposeFragment()
    }

    private var bind: FragmentCreditScoreComposeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentCreditScoreComposeBinding.inflate(inflater, container, false)
        return bind!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = bind!!.recyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ComposeAdapter(buildMockData())
        rv.itemAnimator = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun buildMockData(): List<ComposeItem> {
        return listOf(
//            ComposeItem(R.mipmap.credit_score4, "在线时长", "45/150", "基础：+0.2/天  会员+10%  靓号+10%", 0.30f, com.cboda.baselibrary.R.color.color_A855F7),
//            ComposeItem(R.mipmap.credit_score5, "注册天数", "45/150", "基础：+0.2/天  会员+10%  靓号+10%", 0.30f, com.cboda.baselibrary.R.color.color_2563EB),
//            ComposeItem(R.mipmap.credit_score6, "担保交易", "45/150", "基础：+0.2/USDT  会员+10%  靓号+10%", 0.30f, com.cboda.baselibrary.R.color.color_22C55E),
//            ComposeItem(R.mipmap.credit_score7, "邀请好友", "45/150", "基础：+2/人  会员+10%  靓号+10%", 0.30f, com.cboda.baselibrary.R.color.color_06B6D4),
//            ComposeItem(R.mipmap.credit_score8, "获得背书", "50/150", "基础：+2/人  会员+10%  靓号+10%", 0.34f, com.cboda.baselibrary.R.color.color_8B5CF6),
//            ComposeItem(R.mipmap.credit_score9, "特殊贡献", "45/150", "基础：+2/人  会员+10%  靓号+10%", 0.30f, com.cboda.baselibrary.R.color.color_F59E0B),
//            ComposeItem(R.mipmap.credit_score10, "持有ODIC", "45/150", "基础：+2/人  会员+10%  靓号+10%", 0.30f, com.cboda.baselibrary.R.color.color_E11D48)
        )
    }

    private data class ComposeItem(
        val iconRes: Int,
        val title: String,
        val scoreText: String,
        val desc: String,
        val progress: Float,
        val barColorRes: Int
    )

    private class ComposeAdapter(
        private val items: List<ComposeItem>
    ) : RecyclerView.Adapter<ComposeAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemCreditScoreComposeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return VH(binding)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        class VH(private val binding: ItemCreditScoreComposeBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ComposeItem) {
                binding.ivIcon.setImageResource(item.iconRes)
                binding.tvTitle.text = item.title
                binding.tvScore.text = item.scoreText
                binding.tvDesc.text = item.desc
                binding.progressBar.progress = (item.progress * 100).toInt().coerceIn(0, 100)
                binding.progressBar.progressTintList =
                    androidx.core.content.ContextCompat.getColorStateList(binding.root.context, item.barColorRes)
            }
        }
    }
}

