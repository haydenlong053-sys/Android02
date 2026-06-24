package com.freddy.chat.mention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.utils.AvatarViews
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.GroupMemberItem

/**
 * 群聊 @ 成员选择页（自研 IM）。
 */
class ImMentionMemberActivity : AppCompatActivity() {

    private lateinit var adapter: MentionMemberAdapter
    private var groupId: String = ""
    private var showMentionAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_im_mention_member)
        groupId = intent.getStringExtra(ImMentionContract.EXTRA_GROUP_ID).orEmpty()
        showMentionAll = intent.getBooleanExtra(ImMentionContract.EXTRA_SHOW_MENTION_ALL, false)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.im_mention_pick_title)
        toolbar.setNavigationOnClickListener { finish() }

        adapter = MentionMemberAdapter(showMentionAll) { item ->
            when (item) {
                is MentionRow.All -> finishWithMentionAll()
                is MentionRow.Member -> finishWithMember(item.member)
            }
        }
        findViewById<RecyclerView>(R.id.rv_members).apply {
            layoutManager = LinearLayoutManager(this@ImMentionMemberActivity)
            adapter = this@ImMentionMemberActivity.adapter
        }
        loadMembers()
    }

    private fun loadMembers() {
        if (TextUtils.isEmpty(groupId)) {
            return
        }
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        ImLocalStore.loadGroupMembersMap(groupId) { map ->
            val members = map.values
                .filter { member ->
                    member != null
                            && !member.isDeleted
                            && !TextUtils.isEmpty(member.userId)
                            && member.userId != currentUserId
                }
                .sortedBy { it.nickname?.lowercase().orEmpty() }
            runOnUiThread {
                adapter.submit(members)
            }
        }
    }

    private fun finishWithMentionAll() {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(ImMentionContract.EXTRA_MENTION_ALL, true)
        )
        finish()
    }

    private fun finishWithMember(member: GroupMemberItem) {
        val displayName = member.nickname?.trim().orEmpty().ifEmpty { member.userId }
        setResult(
            Activity.RESULT_OK,
            Intent()
                .putExtra(ImMentionContract.EXTRA_USER_ID, member.userId)
                .putExtra(ImMentionContract.EXTRA_DISPLAY_NAME, displayName)
        )
        finish()
    }

    private sealed class MentionRow {
        object All : MentionRow()
        data class Member(val member: GroupMemberItem) : MentionRow()
    }

    private class MentionMemberAdapter(
        private val showMentionAll: Boolean,
        private val onItemClick: (MentionRow) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_ALL = 0
            private const val TYPE_MEMBER = 1
        }

        private val rows = ArrayList<MentionRow>()

        fun submit(members: List<GroupMemberItem>) {
            rows.clear()
            if (showMentionAll) {
                rows.add(MentionRow.All)
            }
            members.forEach { rows.add(MentionRow.Member(it)) }
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (rows[position] is MentionRow.All) TYPE_ALL else TYPE_MEMBER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TYPE_ALL) {
                val view = inflater.inflate(
                    com.cboda.chatui.R.layout.conversation_header_mention_broad_all,
                    parent,
                    false
                )
                AllVH(view)
            } else {
                MemberVH(inflater.inflate(R.layout.item_group_member, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val row = rows[position]
            when (holder) {
                is AllVH -> holder.itemView.setOnClickListener { onItemClick(row) }
                is MemberVH -> holder.bind((row as MentionRow.Member).member) {
                    onItemClick(row)
                }
            }
        }

        override fun getItemCount(): Int = rows.size

        private class AllVH(itemView: View) : RecyclerView.ViewHolder(itemView)

        private class MemberVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val avatarRoot: View = itemView.findViewById(R.id.avatar)
            private val tvName: TextView = itemView.findViewById(R.id.tv_member_name)
            private val tvRole: TextView = itemView.findViewById(R.id.tv_member_role)
            private val btnAction: TextView = itemView.findViewById(R.id.btn_member_action)

            fun bind(member: GroupMemberItem, onClick: () -> Unit) {
                val name = member.nickname?.trim().orEmpty().ifEmpty { member.userId }
                tvName.text = name
                tvRole.visibility = View.GONE
                btnAction.visibility = View.GONE
                AvatarViews.bind(avatarRoot, member.headPortrait, name, member.userId)
                itemView.setOnClickListener { onClick() }
            }
        }
    }
}
