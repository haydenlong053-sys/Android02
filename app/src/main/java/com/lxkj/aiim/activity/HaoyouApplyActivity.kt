package com.lxkj.aiim.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.lxkj.aiim.MainActivity
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.model.SocialCircle
import com.lxkj.aiim.utils.StringUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.viewmodel.FriendViewModel
import com.lzy.okgo.utils.HttpUtils
import kotlinx.coroutines.launch

class HaoyouApplyActivity : BaseSimple2Activity() {

    private var userName = ""
    private var uid = ""
    private var qunid = ""
    private var viewModel: CommViewModel? = null
    private val friendViewModel: FriendViewModel by viewModels()
    private var source = "" //来源 1：群聊 2：名片 3：二维码 4：搜索添加
    private lateinit var tvPhoneLogin: TextView
    private lateinit var tvCancel: TextView
    private lateinit var circleRecyclerView: RecyclerView
    private lateinit var circleAdapter: CircleTagAdapter

    //    private lateinit var edtmiaoshu: EditText
    private lateinit var edtbeizhu: EditText
    private lateinit var edtshenqing:
            EditText


    /** 当前选中的圈子标签：1 亲密圈 2 社交圈 3 工作圈 4 公关圈，0 表示未选择 */
//    private var selectedCircleType: Int = 0

    override fun showBgTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return false
    }

    private fun initViews() {
        edtshenqing = findViewById(R.id.edtshenqing)
        edtbeizhu = findViewById(R.id.edtbeizhu)
//        edtmiaoshu = findViewById(R.id.edtmiaoshu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
        tvCancel = findViewById(R.id.tv_cancel)
        circleRecyclerView = findViewById(R.id.layout_circle_row1)

        // 2 列网格，和设计图一致
        circleRecyclerView.layoutManager = GridLayoutManager(this, 2)
        circleAdapter = CircleTagAdapter { circle, isSelected ->
            // 选中时记录圈子类型 id；未选中则清零
        }
        circleRecyclerView.adapter = circleAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunid = intent?.getStringExtra("qunid") ?: ""
        source = intent?.getStringExtra("source") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        userName = intent?.getStringExtra("userName") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        getToolbarTitle()?.text = getString(R.string.shenqingtianjaihaoyou)
        lifecycleScope.launch {
            // 通过接口获取圈子标签数据并展示
            friendViewModel.getFriendshipCharacter().observe(this@HaoyouApplyActivity) { circles ->
                if (circles != null) {
                    runOnUiThread {
                        circleAdapter.submitList(circles)
                    }

                }
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "sehnqingsuc" -> {
                    showToastMain("申请添加好友成功！")
                    finish()
                }
            }
        }
        edtshenqing?.setText(
            StringUtils.format(
                getString(R.string.woshishenqing),
                MainActivity.currtentLoginUser?.name ?: ""
            )
        )
        edtbeizhu?.setText(userName)


        tvPhoneLogin?.setOnClickListener {

            if (edtshenqing?.text?.trim().isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.fasonghaouyoushenqingtips))
                return@setOnClickListener
            }

            if (edtbeizhu?.text?.trim().isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.shezhibneizhutispa))
                return@setOnClickListener
            }



            viewModel?.fasonghaoyoushenqing(
                this,
                uid,
                edtshenqing?.text?.trim()?.toString() ?: "",
                edtbeizhu?.text?.trim()?.toString() ?: "",
                circleAdapter.getSelectedId().toString(),
                source,
                qunid
            )
        }

        tvCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * 初始化圈子标签点击高亮逻辑
     */

    /**
     * 更新圈子标签选中状态，高亮当前项描边，其他还原
     */


    override fun getLayoutId(): Int {
        return R.layout.activity_haoyou_apply
    }

    /**
     * 圈子标签适配器：使用服务器返回的 SocialCircle 列表渲染 2x2 卡片
     */
    private class CircleTagAdapter(
        private val onItemClick: (SocialCircle, Boolean) -> Unit
    ) : RecyclerView.Adapter<CircleTagAdapter.CircleViewHolder>() {

        private val items = mutableListOf<SocialCircle>()
        private var selectedId: Int = -1

        fun getSelectedId() = selectedId
        fun submitList(list: List<SocialCircle>) {
            items.clear()
            items.addAll(list)
            if (list.isNotEmpty()) {
                selectedId = list[0].id
            }
            // 刷新选中状态
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): CircleViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_circle_tag, parent, false)
            return CircleViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: CircleViewHolder, position: Int) {
            val item = items[position]
            val isSelected = item.id == selectedId
            holder.bind(item, isSelected)
            holder.itemView.setOnClickListener {
                val newSelected = if (selectedId == item.id) 0 else item.id
                selectedId = newSelected
                notifyDataSetChanged()
                onItemClick(item, newSelected == item.id)
            }
        }

        class CircleViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvCircleTitle)
            private val tvDesc: TextView = itemView.findViewById(R.id.tvCircleDesc)

            fun bind(circle: SocialCircle, selected: Boolean) {
                tvTitle.text = circle.characterTags
                tvDesc.text = circle.characterExplanation
                tvTitle.setTextColor(tvTitle.context.resources.getColor(if (selected) R.color.color_3B47DDk else R.color.color_text0))
                tvDesc.setTextColor(tvDesc.context.resources.getColor(if (selected) R.color.color_3B47DDk else R.color.color_text_content))
                itemView.background = androidx.core.content.ContextCompat.getDrawable(
                    itemView.context,
                    if (selected) R.drawable.bg_rect_mine_3b47dd_10dp else R.drawable.bg_rect_mine_10dp
                )
            }
        }
    }
}