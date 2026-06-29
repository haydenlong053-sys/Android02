package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.holder.SelectTypeBroadHolder
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.im.MessageProcessor
import com.freddy.chat.utils.AvatarViews
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupMemberItem
import com.freddy.im.listener.OnHttpImListener

/** 选择成员并设置禁言时长。 */
class ImXuanQunJinYanBroadActivity : BaseSimple2BroadActivity() {

    private var qunId = ""
    private val selectedIds = linkedSetOf<String>()
    private val displayMembers = ArrayList<GroupMemberItem>()
    private lateinit var adapter: MutePickAdapter
    private lateinit var tvAgree: ShapeTextBroadView
    private lateinit var tvjinyan: TextView
    private var durationType = DURATION_DAY

    override fun getLayoutId(): Int = R.layout.activity_im_mute_pick_member

    override fun showTransparentTopTitleLayout(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        qunId = intent?.getStringExtra("qunid") ?: ""
        getToolbarTitle()?.text = getString(R.string.xuanquanyuanjinyanle)

        tvAgree = findViewById(R.id.tvAgree)
        tvjinyan = findViewById(R.id.tvjinyan)
        tvjinyan.text = getString(R.string.jinyanyitian)

        val durationHolder = SelectTypeBroadHolder(this, findViewById(R.id.jinyantian), gravity = Gravity.START)
        val durationLabels = listOf(
            getString(R.string.jinyanyitian),
            getString(R.string.jinyanyizhou),
            getString(R.string.jinyanyigeyue),
            getString(R.string.jinyanyongjiu)
        )
        durationHolder.setCateList(durationLabels)
        durationHolder.itemListener = object : SelectTypeBroadHolder.OnItemClickListener {
            override fun onItemClick(content: String) {
                tvjinyan.text = content
                durationType = when (content) {
                    getString(R.string.jinyanyizhou) -> DURATION_WEEK
                    getString(R.string.jinyanyigeyue) -> DURATION_MONTH
                    getString(R.string.jinyanyongjiu) -> DURATION_FOREVER
                    else -> DURATION_DAY
                }
            }
        }
        findViewById<View>(R.id.jinyantian).setOnClickListener { durationHolder.show() }

        adapter = MutePickAdapter(selectedIds) { userId, selected ->
            if (selected) selectedIds.add(userId) else selectedIds.remove(userId)
        }
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@ImXuanQunJinYanBroadActivity)
            adapter = this@ImXuanQunJinYanBroadActivity.adapter
        }

        tvAgree.setOnClickListener {
            if (selectedIds.isEmpty()) {
                ToastBroadUtils.show(getString(R.string.xuanquanyuanjinyanle))
                return@setOnClickListener
            }
            val durationSec = resolveDurationSec(durationType)
            ImGroupHttpExample.requestSetMembersMute(
                qunId, selectedIds.toList(), true, durationSec,
                object : OnHttpImListener<Boolean> {
                    override fun onHttpImSuccess(result: Boolean) {
                        runOnUiThread {
                            ToastBroadUtils.show("禁言成功")
//                            MessageProcessor.getInstance().notifyGroupMuteUpdated(qunId)
                            finish()
                        }
                    }
                }, {
                    runOnUiThread {
                        ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                    }
                })
        }

        loadMembers()
    }

    private fun loadMembers() {
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        ImLocalStore.loadGroupMembersMap(qunId) { map ->
            displayMembers.clear()
            map.values.forEach { member ->
                if (member.userId == currentUserId) return@forEach
                if (GroupRole.isOwnerOrAdmin(member.role)) return@forEach
                displayMembers.add(member)
            }
            displayMembers.sortBy { it.nickname?.lowercase() ?: it.userId }
            runOnUiThread { adapter.submit(displayMembers) }
        }
    }

    private fun resolveDurationSec(type: Int): Long? = when (type) {
        DURATION_DAY -> 86_400L
        DURATION_WEEK -> 604_800L
        DURATION_MONTH -> 2_592_000L
        else -> null
    }

    private class MutePickAdapter(
        private val selected: Set<String>,
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<MutePickAdapter.VH>() {

        private val items = ArrayList<GroupMemberItem>()

        fun submit(list: List<GroupMemberItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_im_mute_pick_member, parent, false)
            return VH(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            val userId = item.userId ?: return
            val name = item.nickname?.takeIf { it.isNotBlank() } ?: userId
            holder.name.text = name
            AvatarViews.bind(holder.avatar, item.headPortrait, name, name)
            holder.check.setImageResource(
                if (selected.contains(userId)) R.drawable.ic_broad_yixuanz
                else R.drawable.ic_broad_weixuanz
            )
            holder.itemView.setOnClickListener {
                val nowSelected = !selected.contains(userId)
                onToggle(userId, nowSelected)
                notifyItemChanged(position)
            }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val avatar: View = view.findViewById(R.id.avatar)
            val name: TextView = view.findViewById(R.id.tv_name)
            val check: android.widget.ImageView = view.findViewById(R.id.iv_check)
        }
    }

    companion object {
        private const val DURATION_DAY = 1
        private const val DURATION_WEEK = 2
        private const val DURATION_MONTH = 3
        private const val DURATION_FOREVER = 4
    }
}
