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
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SquareFollowerUserBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.SquareFollowerUserBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.SquareFollowerListActivityBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SquareFollowerListActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareFollowerListActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private val adapter = SquareFollowerUserBroadAdapter()
    private var targetUserId: String? = null
    private var keyword = ""
    private var pageNo = 1
    private var totalPage = 1
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareFollowerListActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        targetUserId = intent.getStringExtra(EXTRA_USER_ID)?.takeIf { it.isNotBlank() }
        initTitleBar()
        initRecyclerView()
        initSearch()
        initRefreshLayout()
        loadFollowerList()
    }

    private fun initTitleBar() {
        val isSelf = targetUserId.isNullOrBlank() ||
            targetUserId == MMKVBroadUtils.getRealUserId()
        getToolbarTitle()?.text = getString(
            if (isSelf) R.string.square_follower_list_title else R.string.square_follower_list_title_other,
        )
        getRightImg()?.visibility = View.GONE
        getRightView()?.apply {
            visibility = if (isSelf) View.VISIBLE else View.GONE
            text = getString(R.string.黑名单)
            setTextColor(getAppColor(this@SquareFollowerListActivity, com.cboda.baselibrary.R.color.black_90))
            paint.isFakeBoldText = false
            setOnClickListener {
                startActivity(Intent(this@SquareFollowerListActivity, SquareBlackActivity::class.java))
            }
        }
    }

    private fun initRecyclerView() {
        mBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(this@SquareFollowerListActivity)
            adapter = this@SquareFollowerListActivity.adapter
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            val item = adapter.getItem(position)
            when (view.id) {
                R.id.followerAvatar -> openUserProfile(item.userId)
                R.id.followBtn -> toggleFollow(item)
                R.id.blackBtn -> toggleBlack(item)
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
                loadFollowerList()
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
        loadFollowerList()
    }

    private fun loadFollowerList() {
        lifecycleScope.launch {
            viewModel.getFollowerList(
                userId = targetUserId,
                keyword = keyword,
                pageNo = pageNo,
                pageSize = CommonBroadConfig.pageSize,
            ).observe(this@SquareFollowerListActivity) { result ->
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
            viewModel.followUser(userId, follow).observe(this@SquareFollowerListActivity) { success ->
                if (success == true) {
                    adapter.updateFollowState(userId, if (follow) "1" else "0")
                }
            }
        }
    }

    private fun toggleBlack(item: SquareFollowerUserBean) {
        val userId = item.userId.trim()
        if (userId.isEmpty()) return
        val black = item.isBlack != "1"
        lifecycleScope.launch {
            viewModel.blackUser(userId, black).observe(this@SquareFollowerListActivity) { success ->
                if (success == true) {
                    adapter.updateBlackState(userId, if (black) "1" else "0")
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

    override fun getViewBindingClass() = SquareFollowerListActivityBinding::class.java
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
                Intent(context, SquareFollowerListActivity::class.java).apply {
                    if (!userId.isNullOrBlank()) {
                        putExtra(EXTRA_USER_ID, userId)
                    }
                },
            )
        }
    }
}
