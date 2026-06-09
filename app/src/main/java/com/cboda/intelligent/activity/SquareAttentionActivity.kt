package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SquareFollowingUserBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.SquareFollowerUserBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.SquareAttentionActivityBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SquareAttentionActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareAttentionActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private val adapter = SquareFollowingUserBroadAdapter()
    private var targetUserId: String? = null
    private var keyword = ""
    private var pageNo = 1
    private var totalPage = 1
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareAttentionActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        targetUserId = intent.getStringExtra(EXTRA_USER_ID)?.takeIf { it.isNotBlank() }
        initTitleBar()
        initRecyclerView()
        initSearch()
        initRefreshLayout()
        loadFollowingList()
    }

    private fun initTitleBar() {
        val isSelf = targetUserId.isNullOrBlank() ||
            targetUserId == MMKVBroadUtils.getRealUserId()
        getToolbarTitle()?.text = getString(
            if (isSelf) R.string.square_following_list_title else R.string.square_following_list_title_other,
        )
        getRightView()?.visibility = View.GONE
        getRightImg()?.visibility = View.GONE
    }

    private fun initRecyclerView() {
        mBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(this@SquareAttentionActivity)
            adapter = this@SquareAttentionActivity.adapter
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            val item = adapter.getItem(position)
            when (view.id) {
                R.id.followingAvatar -> openUserProfile(item.userId)
                R.id.followBtn -> toggleFollow(item)
            }
        }
    }

    private fun initSearch() {
        val binding = mBinding ?: return
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                refreshList()
                hideKeyboard()
                true
            } else {
                false
            }
        }
        binding.searchInput.doAfterTextChanged { text ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                keyword = text?.toString().orEmpty().trim()
                refreshList()
            }
        }
    }

    private fun initRefreshLayout() {
        mBinding?.smart?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshList()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (pageNo >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    mBinding?.smart?.finishLoadMore()
                    return
                }
                pageNo++
                loadFollowingList()
            }
        })
        mBinding?.smart?.setEnableRefresh(true)
        mBinding?.smart?.setEnableLoadMore(true)
    }

    private fun refreshList() {
        pageNo = 1
        totalPage = 1
        mBinding?.smart?.setEnableLoadMore(true)
        mBinding?.smart?.setNoMoreData(false)
        loadFollowingList()
    }

    private fun loadFollowingList() {
        lifecycleScope.launch {
            viewModel.getFollowingList(
                userId = targetUserId,
                keyword = keyword,
                pageNo = pageNo,
                pageSize = CommonBroadConfig.pageSize,
            ).observe(this@SquareAttentionActivity) { result ->
                showData(result)
            }
        }
    }

    private fun showData(result: NetworkListBroadResult<SquareFollowerUserBean>) {
        mBinding?.smart?.finishRefresh()
        mBinding?.smart?.finishLoadMore()
        totalPage = result.totalPage.coerceAtLeast(1)

        val list = result.list.orEmpty()
        if (pageNo == 1) {
            adapter.setList(list)
        } else {
            adapter.addData(list)
        }
        mBinding?.llNoData?.visibility = if (adapter.data.isEmpty()) View.VISIBLE else View.GONE
        if (pageNo >= totalPage) {
            mBinding?.smart?.setEnableLoadMore(false)
        }
    }

    private fun openUserProfile(userId: String) {
        val id = userId.trim()
        if (id.isNotEmpty()) {
            SquareUserActivity.start(this, id)
        }
    }

    private fun toggleFollow(item: SquareFollowerUserBean) {
        val userId = item.userId.trim()
        if (userId.isEmpty()) return
        val follow = item.isFollow != "1"
        lifecycleScope.launch {
            viewModel.followUser(userId, follow).observe(this@SquareAttentionActivity) { success ->
                if (success == true) {
                    adapter.updateFollowState(userId, if (follow) "1" else "0")
                }
            }
        }
    }

    private fun hideKeyboard() {
        mBinding?.searchInput?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun getViewBindingClass() = SquareAttentionActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true

    companion object {
        private const val EXTRA_USER_ID = "userId"
        private const val SEARCH_DEBOUNCE_MS = 300L

        fun start(context: Context, userId: String? = null) {
            context.startActivity(
                Intent(context, SquareAttentionActivity::class.java).apply {
                    if (!userId.isNullOrBlank()) {
                        putExtra(EXTRA_USER_ID, userId)
                    }
                },
            )
        }
    }
}
