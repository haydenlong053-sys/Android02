package com.cboda.intelligent.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SquareBlacklistUserBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.SquareFollowerUserBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.SquareBlackActivityBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SquareBlackActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareBlackActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private val adapter = SquareBlacklistUserBroadAdapter()
    private var keyword = ""
    private var pageNo = 1
    private var totalPage = 1
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareBlackActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        initTitleBar()
        initRecyclerView()
        initSearch()
        initRefreshLayout()
        loadBlacklist()
    }

    private fun initTitleBar() {
        getToolbarTitle()?.text = getString(R.string.square_blacklist_title)
        getRightView()?.visibility = View.GONE
        getRightImg()?.visibility = View.GONE
    }

    private fun initRecyclerView() {
        mBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(this@SquareBlackActivity)
            adapter = this@SquareBlackActivity.adapter
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            val item = adapter.getItem(position)
            when (view.id) {
                R.id.blacklistAvatar -> openUserProfile(item.userId)
                R.id.unblockBtn -> unblockUser(item.userId)
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
                loadBlacklist()
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
        loadBlacklist()
    }

    private fun loadBlacklist() {
        lifecycleScope.launch {
            viewModel.getBlacklistList(
                keyword = keyword,
                pageNo = pageNo,
                pageSize = CommonBroadConfig.pageSize,
            ).observe(this@SquareBlackActivity) { result ->
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

    private fun unblockUser(userId: String) {
        val id = userId.trim()
        if (id.isEmpty()) return
        lifecycleScope.launch {
            viewModel.blackUser(id, false).observe(this@SquareBlackActivity) { success ->
                if (success == true) {
                    refreshList()
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

    override fun getViewBindingClass() = SquareBlackActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L

        fun start(context: Context) {
            context.startActivity(android.content.Intent(context, SquareBlackActivity::class.java))
        }
    }
}
