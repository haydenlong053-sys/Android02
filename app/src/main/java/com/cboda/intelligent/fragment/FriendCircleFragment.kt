package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.intelligent.adapter.SquarePostBroadAdapter
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.FriendsCircleFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import kotlin.collections.orEmpty

class FriendCircleFragment : BaseBroadFragment<FriendsCircleFragmentBinding, CleverBodyViewBroadModel>() {
    private val mAdapter = SquarePostBroadAdapter("friend")
    private var pageNo = 1
    private var totalPage = 1
    override fun initView() {
        mBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        initRefreshLayout()
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
            viewModel.getFriendPostList(pageNo, CommonBroadConfig.pageSize)
                .observe(this@FriendCircleFragment) { result ->
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

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = FriendsCircleFragmentBinding.inflate(inflater, parent, false)
}