package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.im.GroupBlacklistCandidateAdapter
import com.cboda.intelligent.adapter.im.GroupBlacklistCandidateUi
import com.cboda.intelligent.adapter.im.GroupBlacklistMemberAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.bean.GroupBlacklistItem
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.http.model.im.GroupMemberItem
import kotlinx.coroutines.launch

/**
 * 群黑名单：已拉黑列表 + 选择群成员加入黑名单。
 */
class ImGroupBlacklistBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var viewModel: ImGroupBlacklistViewModel? = null

    private lateinit var tvBlacklistSectionTitle: TextView
    private lateinit var tvAddMember: TextView
    private lateinit var etSearchBlacklist: EditText
    private lateinit var rvBlacklist: RecyclerView
    private lateinit var tvBlacklistEmpty: TextView
    private lateinit var tvNoMore: TextView
    private lateinit var addSection: View
    private lateinit var rvCandidates: RecyclerView
    private lateinit var tvCandidatesEmpty: TextView
    private lateinit var btnConfirmAdd: TextView
    private lateinit var scrollContent: NestedScrollView

    private val memberAdapter = GroupBlacklistMemberAdapter()
    private val candidateAdapter = GroupBlacklistCandidateAdapter()

    private var allBlacklist: List<GroupBlacklistItem> = emptyList()
    private var blacklistTotalCount = 0
    private var searchKeyword = ""

    override fun getLayoutId(): Int = R.layout.activity_im_group_blacklist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        if (qunId.isEmpty()) {
            finish()
            return
        }
        getToolbarTitle()?.text = getString(com.cboda.baselibrary.R.string.group_blacklist_title)
        initViews()
        bindList()
        bindClicks()
        viewModel = ViewModelProvider(this)[ImGroupBlacklistViewModel::class.java]
        viewModel?.state?.observe(this) { state ->
            if (state.loading) {
                showLoading()
            } else {
                dismissLoading()
            }
            allBlacklist = state.blacklist
            blacklistTotalCount = state.totalCount
            renderBlacklist()
            renderCandidates(state.candidates, state.blacklist)
        }
        viewModel?.load(qunId, IMSClientBootstrap.getInstance().currentUserId)
    }

    private fun initViews() {
        tvBlacklistSectionTitle = findViewById(R.id.tvBlacklistSectionTitle)
        tvAddMember = findViewById(R.id.tvAddMember)
        etSearchBlacklist = findViewById(R.id.etSearchBlacklist)
        rvBlacklist = findViewById(R.id.rvBlacklist)
        tvBlacklistEmpty = findViewById(R.id.tvBlacklistEmpty)
        tvNoMore = findViewById(R.id.tvNoMore)
        addSection = findViewById(R.id.addSection)
        rvCandidates = findViewById(R.id.rvCandidates)
        tvCandidatesEmpty = findViewById(R.id.tvCandidatesEmpty)
        btnConfirmAdd = findViewById(R.id.btnConfirmAdd)
        scrollContent = findViewById(R.id.scrollContent)
    }

    private fun bindList() {
        rvBlacklist.layoutManager = LinearLayoutManager(this)
        rvBlacklist.adapter = memberAdapter
        rvCandidates.layoutManager = LinearLayoutManager(this)
        rvCandidates.adapter = candidateAdapter

        memberAdapter.setOnItemChildClickListener { _, view, position ->
            if (view.id != R.id.btnRemove) return@setOnItemChildClickListener
            val item = memberAdapter.getItem(position)
            val userId = item.userId?.takeIf { it.isNotBlank() } ?: item.id ?: return@setOnItemChildClickListener
            removeMember(userId)
        }
        candidateAdapter.setOnItemChildClickListener { _, _, position ->
            if (position !in candidateAdapter.data.indices) return@setOnItemChildClickListener
            val item = candidateAdapter.data[position]
            item.selected = !item.selected
            if (!item.selected) {
                item.reason = ""
            }
            candidateAdapter.notifyItemChanged(position)
        }
        etSearchBlacklist.doAfterTextChanged { editable ->
            searchKeyword = editable?.toString()?.trim().orEmpty()
            renderBlacklist()
        }
    }

    private fun bindClicks() {
        tvAddMember.setOnClickListener {
            scrollContent.post {
                scrollContent.smoothScrollTo(0, addSection.top)
            }
        }
        btnConfirmAdd.setOnClickListener { confirmAdd() }
    }

    private fun renderBlacklist() {
        val filtered = if (searchKeyword.isEmpty()) {
            allBlacklist
        } else {
            allBlacklist.filter { item ->
                item.displayName().contains(searchKeyword, ignoreCase = true)
                    || item.displayId().contains(searchKeyword, ignoreCase = true)
                    || item.userId.orEmpty().contains(searchKeyword, ignoreCase = true)
            }
        }
        memberAdapter.setList(filtered)
        tvBlacklistSectionTitle.text = getString(
            com.cboda.baselibrary.R.string.group_blacklist_section_title,
            blacklistTotalCount.coerceAtLeast(allBlacklist.size),
        )
        val empty = filtered.isEmpty()
        tvBlacklistEmpty.isVisible = empty
        tvNoMore.isVisible = !empty
    }

    private fun renderCandidates(
        members: List<GroupMemberItem>,
        blacklist: List<GroupBlacklistItem>,
    ) {
        val blackIds = blacklist.mapNotNull { it.userId?.takeIf { id -> id.isNotBlank() } ?: it.id }
            .toHashSet()
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        val previous = candidateAdapter.data.associateBy { it.member.userId }
        val list = members
            .filter { it.userId != currentUserId && it.userId !in blackIds }
            .map { member ->
                val old = previous[member.userId]
                GroupBlacklistCandidateUi(
                    member = member,
                    selected = old?.selected == true,
                    reason = old?.reason.orEmpty(),
                )
            }
        candidateAdapter.setList(list)
        tvCandidatesEmpty.isVisible = list.isEmpty()
    }

    private fun removeMember(userId: String) {
        lifecycleScope.launch {
            viewModel?.removeBlacklist(qunId, userId)
                ?.observe(this@ImGroupBlacklistBroadActivity) { ok ->
                    if (ok == true) {
                        ToastBroadUtils.show(
                            getString(com.cboda.baselibrary.R.string.group_blacklist_remove_success),
                        )
                        viewModel?.load(qunId, IMSClientBootstrap.getInstance().currentUserId)
                    }
                }
        }
    }

    private fun confirmAdd() {
        val selected = candidateAdapter.data.filter { it.selected }
        if (selected.isEmpty()) {
            ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_blacklist_select_tip))
            return
        }
        val uid = selected.mapNotNull { it.member.userId?.takeIf { id -> id.isNotBlank() } }
            .distinct()
            .joinToString(",")
        if (uid.isEmpty()) return
        lifecycleScope.launch {
            viewModel?.addBlacklist(qunId, uid)
                ?.observe(this@ImGroupBlacklistBroadActivity) { ok ->
                    if (ok == true) {
                        ToastBroadUtils.show(
                            getString(com.cboda.baselibrary.R.string.group_blacklist_add_success),
                        )
                        viewModel?.load(qunId, IMSClientBootstrap.getInstance().currentUserId)
                    }
                }
        }
    }
}
