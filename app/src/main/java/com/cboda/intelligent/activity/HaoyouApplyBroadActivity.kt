package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.isSubmitCanGo3
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.intelligent.MainBroadActivity
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.showToastMain
import com.cboda.baselibrary.bean.SocialBroadCircle
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.viewmodel.FriendViewBroadModel
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.launch

class HaoyouApplyBroadActivity : BaseSimple2BroadActivity() {

    private var type = ""
    private var userName = ""
    private var uid = ""
    private var qunid = ""
    private var viewModel: CommViewBroadModel? = null
    private val friendViewModel: FriendViewBroadModel by viewModels()
    private var source = "" //来源 1：群聊 2：名片 3：二维码 4：搜索添加
    private lateinit var tvPhoneLogin: ShapeTextView
    private lateinit var tvCancel: TextView
    private lateinit var tvApplyTitle: TextView
    private lateinit var layoutApplyReason: View

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
        setRootBgColor(R.color.white)
        tvApplyTitle = findViewById(R.id.tv_apply_title)
        layoutApplyReason = findViewById(R.id.layout_apply_reason)
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
            checkData()
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
        type = intent?.getStringExtra("type") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        tvApplyTitle.isGone = type != "1"
        layoutApplyReason.isGone = type != "1"
        getToolbarTitle()?.text = getString(if (type == "1") R.string.shenqingtianjaihaoyou else if (type == "2") R.string.social_friend_verification else R.string.social_persona_tag)
        if (type == "3") {
            circleAdapter.setSelectedId(qunid)
        }
        lifecycleScope.launch {
            // 通过接口获取圈子标签数据并展示
            friendViewModel.getFriendshipCharacter().observe(this@HaoyouApplyBroadActivity) { circles ->
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
            StringBroadUtils.format(
                getString(R.string.woshishenqing),
                MainBroadActivity.currentLoginUser?.name ?: ""
            )
        )
        edtbeizhu?.setText(userName)

        edtshenqing.setEdTextChange {
            checkData()
        }
        edtbeizhu.setEdTextChange {
            checkData()
        }
        tvPhoneLogin?.setOnClickListener {
            if (type == "1" && edtshenqing?.text?.trim().isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.fasonghaouyoushenqingtips))
                return@setOnClickListener
            }
//            if (edtbeizhu?.text?.trim().isNullOrEmpty()) {
//                ToastBroadUtils.show(getString(R.string.shezhibneizhutispa))
//                return@setOnClickListener
//            }
//            if (circleAdapter.getSelectedId() == -1) {
//                ToastBroadUtils.show(getString(R.string.social_select_circle))
//                return@setOnClickListener
//            }
            if (type == "1") {
                viewModel?.fasonghaoyoushenqing(
                    this,
                    uid,
                    edtshenqing?.text?.trim()?.toString() ?: "",
                    edtbeizhu?.text?.trim()?.toString() ?: "",
                    "",
                    source,
                    qunid
                )
            } else {
                circleAdapter.getSelectedData()?.let {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("remark", edtbeizhu?.text?.trim()?.toString() ?: "")
                        putExtra("circleTag", it.characterTags)
                        putExtra("circleId", circleAdapter.getSelectedId())
                    })
                    finish()
                }
            }
        }

        tvCancel.setOnClickListener {
            finish()
        }
    }

    private fun checkData() {
        val canGo = if (type == "1" && edtshenqing?.text?.trim().isNullOrEmpty()) {
            false
//        } else if (edtbeizhu?.text?.trim().isNullOrEmpty()) {
//            false
        } else if (circleAdapter.getSelectedId() == -1) {
            true
        } else {
            true
        }
        tvPhoneLogin.isSubmitCanGo3(canGo)
    }

    /**
     * 初始化圈子标签点击高亮逻辑
     */

    /**
     * 更新圈子标签选中状态，高亮当前项描边，其他还原
     */


    override fun getLayoutId(): Int {
        return R.layout.activity_hao_broad_you_apply
    }

    /**
     * 圈子标签适配器：使用服务器返回的 SocialBroadCircle 列表渲染 2x2 卡片
     */
    private class CircleTagAdapter(
        private val onItemClick: (SocialBroadCircle, Boolean) -> Unit
    ) : RecyclerView.Adapter<CircleTagAdapter.CircleViewHolder>() {

        private val items = mutableListOf<SocialBroadCircle>()
        private var selectedId: Int = -1
        private var selectedSocialBroadCircle: SocialBroadCircle? = null

        fun getSelectedData() = selectedSocialBroadCircle
        fun getSelectedId() = selectedId


        fun setSelectedId(id: String?) {
            selectedId = anyToInt(id ?: "-1")
        }

        fun submitList(list: List<SocialBroadCircle>) {
            items.clear()
            items.addAll(list)
            if (selectedId != -1 && list.isNotEmpty()) {
                list.forEach { item ->
                    if (selectedId == item.id) {
                        selectedSocialBroadCircle = item
                    }
                }
            }
            // 刷新选中状态
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): CircleViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_circle_broad_tag, parent, false)
            return CircleViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: CircleViewHolder, position: Int) {
            val item = items[position]
            val isSelected = item.id == selectedId
            holder.bind(item, isSelected)
            holder.itemView.setOnClickListener {
                val newSelected = if (selectedId == item.id) -1 else item.id
                selectedSocialBroadCircle = if (selectedId == item.id) null else item
                setSelectedId(newSelected.toString())
                notifyDataSetChanged()
                onItemClick(item, newSelected == item.id)
            }
        }

        class CircleViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvCircleTitle)
            private val tvDesc: TextView = itemView.findViewById(R.id.tvCircleDesc)

            fun bind(circle: SocialBroadCircle, selected: Boolean) {
                tvTitle.text = circle.characterTags
                tvDesc.text = circle.characterExplanation
                tvTitle.setTextColor(tvTitle.context.resources.getColor(if (selected) R.color.color_3B47DDk else R.color.color_text0))
                tvDesc.setTextColor(tvDesc.context.resources.getColor(if (selected) R.color.color_3B47DDk else R.color.color_text_content))
                itemView.background = androidx.core.content.ContextCompat.getDrawable(
                    itemView.context,
                    if (selected) R.drawable.bg_rect_mine_3b47dd_broad_10dp else R.drawable.bg_rect_mine_broad_10dp
                )
            }
        }
    }
}