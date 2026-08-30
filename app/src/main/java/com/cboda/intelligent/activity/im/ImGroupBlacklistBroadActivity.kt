package com.cboda.intelligent.activity.im

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.im.GroupBlacklistMemberAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.bean.GroupBlacklistItem
import kotlinx.coroutines.launch

/**
 * 群黑名单：仅展示已拉黑列表；添加成员跳转选择群成员页，返回后调用加黑接口。
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

    private val memberAdapter = GroupBlacklistMemberAdapter()

    private var allBlacklist: List<GroupBlacklistItem> = emptyList()
    private var blacklistTotalCount = 0
    private var searchKeyword = ""

    private val selectMemberLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val ids = result.data?.getStringArrayListExtra(ImGroupIntent.EXTRA_SELECTED_USER_IDS)
            ?.mapNotNull { it.trim().takeIf { id -> id.isNotEmpty() } }
            ?.distinct()
            .orEmpty()
        if (ids.isEmpty()) return@registerForActivityResult
        addSelectedToBlacklist(ids)
    }

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
        }
        viewModel?.load(qunId)
    }

    private fun initViews() {
        tvBlacklistSectionTitle = findViewById(R.id.tvBlacklistSectionTitle)
        tvAddMember = findViewById(R.id.tvAddMember)
        etSearchBlacklist = findViewById(R.id.etSearchBlacklist)
        rvBlacklist = findViewById(R.id.rvBlacklist)
        tvBlacklistEmpty = findViewById(R.id.tvBlacklistEmpty)
        tvNoMore = findViewById(R.id.tvNoMore)
    }

    private fun bindList() {
        rvBlacklist.layoutManager = LinearLayoutManager(this)
        rvBlacklist.adapter = memberAdapter

        memberAdapter.setOnItemChildClickListener { _, view, position ->
            if (view.id != R.id.btnRemove) return@setOnItemChildClickListener
            val item = memberAdapter.getItem(position)
            val userId = item.userId?.takeIf { it.isNotBlank() } ?: item.id ?: return@setOnItemChildClickListener
            removeMember(userId)
        }
        etSearchBlacklist.doAfterTextChanged { editable ->
            searchKeyword = editable?.toString()?.trim().orEmpty()
            renderBlacklist()
        }
    }

    private fun bindClicks() {
        tvAddMember.setOnClickListener { openSelectMembers() }
    }

    private fun openSelectMembers() {
        val excludeIds = allBlacklist.mapNotNull {
            it.userId?.takeIf { id -> id.isNotBlank() } ?: it.id?.takeIf { id -> id.isNotBlank() }
        }
        val intent = Intent(this, ImQunMemberListBroadActivity::class.java).apply {
            putExtra("qunid", qunId)
            putExtra("fromPage", ImGroupIntent.FROM_PAGE_SELECT_BLACKLIST)
            putStringArrayListExtra(ImGroupIntent.EXTRA_EXCLUDE_USER_IDS, ArrayList(excludeIds))
        }
        selectMemberLauncher.launch(intent)
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

    private fun removeMember(userId: String) {
        lifecycleScope.launch {
            viewModel?.removeBlacklist(qunId, userId)
                ?.observe(this@ImGroupBlacklistBroadActivity) { ok ->
                    if (ok == true) {
                        ToastBroadUtils.show(
                            getString(com.cboda.baselibrary.R.string.group_blacklist_remove_success),
                        )
                        viewModel?.load(qunId)
                    }
                }
        }
    }

    private fun addSelectedToBlacklist(userIds: List<String>) {
        val uid = userIds.joinToString(",")
        if (uid.isEmpty()) return
        lifecycleScope.launch {
            viewModel?.addBlacklist(qunId, uid)
                ?.observe(this@ImGroupBlacklistBroadActivity) { ok ->
                    if (ok == true) {
                        ToastBroadUtils.show(
                            getString(com.cboda.baselibrary.R.string.group_blacklist_add_success),
                        )
                        viewModel?.load(qunId)
                    }
                }
        }
    }
}
