package com.freddy.chat.mention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.utils.AvatarViews
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.GroupMemberItem

/**
 * 群聊 @ 成员选择页（自研 IM）。
 */
class ImMentionMemberActivity : BaseSimple2BroadActivity() {

    private lateinit var adapter: MentionMemberAdapter
    private lateinit var edtSousuo: EditText
    private lateinit var recyclerView: RecyclerView

    private var groupId: String = ""
    private var showMentionAll = false
    private val allMembers = ArrayList<GroupMemberItem>()
    private val searchHandler by lazy { Handler(Looper.getMainLooper()) }
    private var searchRunnable: Runnable? = null

    override fun showBgTopTitleLayout(): Boolean = true

    override fun getLayoutId(): Int = R.layout.activity_im_mention_member

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        groupId = intent.getStringExtra(ImMentionContract.EXTRA_GROUP_ID).orEmpty()
        showMentionAll = intent.getBooleanExtra(ImMentionContract.EXTRA_SHOW_MENTION_ALL, false)

        getToolbarTitle()?.text = getString(com.cboda.baselibrary.R.string.im_mention_pick_title)

        edtSousuo = findViewById(R.id.edtSousuo)
        recyclerView = findViewById(R.id.rv_members)

        adapter = MentionMemberAdapter(showMentionAll) { item ->
            when (item) {
                is MentionRow.All -> finishWithMentionAll()
                is MentionRow.Member -> finishWithMember(item.member)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        edtSousuo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch()
                true
            } else {
                false
            }
        }
        edtSousuo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
                searchRunnable = Runnable { triggerSearch() }.also { runnable ->
                    searchHandler.postDelayed(runnable, SEARCH_DEBOUNCE_MS)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        loadMembers()
    }

    override fun onDestroy() {
        searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
        super.onDestroy()
    }

    private fun loadMembers() {
        if (TextUtils.isEmpty(groupId)) {
            applyFilter()
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
                .sortedBy { it.displayName.lowercase() }
            runOnUiThread {
                allMembers.clear()
                allMembers.addAll(members)
                applyFilter()
            }
        }
    }

    private fun currentSearchKey(): String = edtSousuo.text?.toString()?.trim().orEmpty()

    private fun triggerSearch() {
        searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
        applyFilter()
    }

    private fun applyFilter() {
        val key = currentSearchKey().lowercase()
        val filtered = if (key.isEmpty()) {
            allMembers
        } else {
            allMembers.filter { member ->
                matchesKeyword(member, key)
            }
        }
        adapter.submit(filtered, key.isNotEmpty())
    }

    private fun matchesKeyword(member: GroupMemberItem, key: String): Boolean {
        val name = member.displayName.trim().lowercase()
        return name.contains(key)
    }

    private fun finishWithMentionAll() {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(ImMentionContract.EXTRA_MENTION_ALL, true)
        )
        finish()
    }

    private fun finishWithMember(member: GroupMemberItem) {
        val displayName = member.displayName.ifEmpty { member.userId }
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
            private const val TYPE_EMPTY = 2
        }

        private val rows = ArrayList<MentionRow>()
        private var showEmptyView = false

        fun submit(members: List<GroupMemberItem>, showEmpty: Boolean) {
            rows.clear()
            val hideAllOnSearch = showEmpty
            if (showMentionAll && !hideAllOnSearch) {
                rows.add(MentionRow.All)
            }
            members.forEach { rows.add(MentionRow.Member(it)) }
            showEmptyView = showEmpty && members.isEmpty()
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            if (showEmptyView) {
                return TYPE_EMPTY
            }
            return if (rows[position] is MentionRow.All) TYPE_ALL else TYPE_MEMBER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_ALL -> AllVH(
                    inflater.inflate(
                        com.cboda.chatui.R.layout.conversation_header_mention_broad_all,
                        parent,
                        false
                    )
                )
                TYPE_EMPTY -> EmptyVH(
                    inflater.inflate(R.layout.list_empty_broad_layout, parent, false)
                )
                else -> MemberVH(inflater.inflate(R.layout.item_group_member, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is EmptyVH -> Unit
                is AllVH -> holder.itemView.setOnClickListener { onItemClick(rows[position]) }
                is MemberVH -> {
                    val row = rows[position] as MentionRow.Member
                    holder.bind(row.member) { onItemClick(row) }
                }
            }
        }

        override fun getItemCount(): Int = if (showEmptyView) 1 else rows.size

        private class AllVH(itemView: View) : RecyclerView.ViewHolder(itemView)

        private class EmptyVH(itemView: View) : RecyclerView.ViewHolder(itemView)

        private class MemberVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val avatarRoot: View = itemView.findViewById(R.id.avatar)
            private val tvName: TextView = itemView.findViewById(R.id.tv_member_name)
            private val tvRole: TextView = itemView.findViewById(R.id.tv_member_role)
            private val btnAction: TextView = itemView.findViewById(R.id.btn_member_action)

            fun bind(member: GroupMemberItem, onClick: () -> Unit) {
                val name = member.displayName.ifEmpty { member.userId }
                tvName.text = name
                tvRole.visibility = View.GONE
                btnAction.visibility = View.GONE
                AvatarViews.bind(avatarRoot, member.headPortrait, name, member.userId)
                itemView.setOnClickListener { onClick() }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 500L
    }
}
