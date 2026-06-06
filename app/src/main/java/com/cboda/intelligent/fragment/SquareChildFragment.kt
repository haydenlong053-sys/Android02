package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.intelligent.activity.SquareDetailActivity
import com.cboda.intelligent.adapter.SquarePostDetailAdapter
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.SquareChildFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

class SquareChildFragment :
    BaseBroadFragment<SquareChildFragmentBinding, CleverBodyViewBroadModel>() {

    private val tabType by lazy { arguments?.getInt(ARG_TAB_TYPE, TAB_WORLD) ?: TAB_WORLD }
    private val mAdapter = SquarePostDetailAdapter("square")
    private var pageNo = 1
    private var totalPage = 1

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = SquareChildFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        mBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        initRefreshLayout()
    }

    override fun initListener() {
        mAdapter.setOnItemClickListener { _, _, position ->
            val post = mAdapter.getItem(position)
            val postId = post.id.trim()
            if (postId.isNotEmpty()) {
                viewModel.startAct<SquareDetailActivity>()
            }
        }
    }

    private fun initRefreshLayout() {
        mBinding?.smart?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                pageNo = 1
                totalPage = 1
                mBinding?.smart?.setEnableLoadMore(true)
                refreshLayout.setNoMoreData(false)
                loadSquarePostList()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (pageNo >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    mBinding?.smart?.finishLoadMore()
                    return
                }
                pageNo++
                loadSquarePostList()
            }
        })
        mBinding?.smart?.setEnableRefresh(true)
        mBinding?.smart?.setEnableLoadMore(true)
    }

    override fun lazyLoad() {
        loadSquarePostList()
    }

    private fun loadSquarePostList() {
        lifecycleScope.launch {
            viewModel.getSquarePostList(tabType, pageNo, CommonBroadConfig.pageSize)
                .observe(this@SquareChildFragment) { result ->
                    showData(result)
                }
        }
    }

    private fun showData(result: NetworkListBroadResult<SquarePostBean>) {
        mBinding?.smart?.finishRefresh()
        mBinding?.smart?.finishLoadMore()
        totalPage = result.totalPage.coerceAtLeast(1)

        val list = result.list.orEmpty()
        if (pageNo == 1) {
            mAdapter.setList(list)
        } else {
            mAdapter.addData(list)
        }

        mBinding?.llNoData?.visibility = if (mAdapter.data.isEmpty()) View.VISIBLE else View.GONE
        if (pageNo >= totalPage) {
            mBinding?.smart?.setEnableLoadMore(false)
        }
    }

    companion object {
        private const val ARG_TAB_TYPE = "tabType"
        const val TAB_WORLD = 1
        const val TAB_HOT = 2
        const val TAB_FOLLOW = 3

        fun newInstance(tabType: Int): SquareChildFragment {
            return SquareChildFragment().apply {
                arguments = bundleOf(ARG_TAB_TYPE to tabType)
            }
        }
    }
}
