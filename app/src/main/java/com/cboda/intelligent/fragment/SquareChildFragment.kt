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
import com.cboda.intelligent.activity.SquareUserActivity
import com.cboda.intelligent.adapter.SquarePostBroadAdapter
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.bean.SquarePostDeleteBroadEvent
import com.cboda.intelligent.bean.SquarePostLikeBroadEvent
import com.cboda.intelligent.bean.SquarePostPublishBroadEvent
import com.cboda.intelligent.bean.SquarePostTopBroadEvent
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.SquareChildFragmentBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.dialog.pop.SquarePublishPopup
import com.cboda.intelligent.utils.SquarePostLikeBroadHelper
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SquareChildFragment :
    BaseBroadFragment<SquareChildFragmentBinding, CleverBodyViewBroadModel>() {

    private val tabType by lazy { arguments?.getInt(ARG_TAB_TYPE, TAB_WORLD) ?: TAB_WORLD }
    private val mAdapter = SquarePostBroadAdapter("square")
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

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroyView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSquarePostLikeChanged(event: SquarePostLikeBroadEvent) {
        mAdapter.updateLikeState(event.postId, event.isLike, event.likeCount)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSquarePostPublished(event: SquarePostPublishBroadEvent) {
        if (event.publishType != SquarePublishPopup.PUBLISH_TYPE_SQUARE) return
        pageNo = 1
        totalPage = 1
        mBinding?.smart?.setEnableLoadMore(true)
        mBinding?.smart?.setNoMoreData(false)
        loadSquarePostList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSquarePostDeleted(event: SquarePostDeleteBroadEvent) {
        pageNo = 1
        totalPage = 1
        mBinding?.smart?.setEnableLoadMore(true)
        mBinding?.smart?.setNoMoreData(false)
        loadSquarePostList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSquarePostTopChanged(event: SquarePostTopBroadEvent) {
        pageNo = 1
        totalPage = 1
        mBinding?.smart?.setEnableLoadMore(true)
        mBinding?.smart?.setNoMoreData(false)
        loadSquarePostList()
    }

    override fun initListener() {
        mAdapter.setOnItemClickListener { _, _, position ->
            val postId = mAdapter.getItem(position).id.trim()
            if (postId.isNotEmpty()) {
                SquareDetailActivity.start(requireContext(), postId)
            }
        }
        mAdapter.setOnItemChildClickListener { _, view, position ->
            val item = mAdapter.getItem(position)
            when (view.id) {
                R.id.squareLikeLayout -> toggleLike(item, position)
                R.id.squareAvatarWrap -> openUserProfile(item.userId)
                R.id.squareShareLayout -> openForwardPublish(item)
            }
        }
    }

    private fun openForwardPublish(post: SquarePostBean) {
        val postId = post.id.trim()
        if (postId.isEmpty()) return
        SquarePublishPopup(requireActivity()).apply {
            setForwardQuote(post)
        }.showPopupWindow()
    }

    private fun openUserProfile(userId: String) {
        val id = userId.trim()
        if (id.isNotEmpty()) {
            SquareUserActivity.start(requireContext(), id)
        }
    }

    private fun toggleLike(item: SquarePostBean, position: Int) {
        val postId = item.id.trim()
        if (postId.isEmpty()) return
        val like = item.isLike != 1
        lifecycleScope.launch {
            viewModel.likeSquarePost(postId, like).observe(this@SquareChildFragment) { success ->
                if (success != true) return@observe
                val newItem = SquarePostLikeBroadHelper.buildUpdatedPost(item, like)
                mAdapter.data[position] = newItem
                mAdapter.notifyItemChanged(position)
                SquarePostLikeBroadHelper.notifyLikeChanged(
                    postId = postId,
                    isLike = newItem.isLike,
                    likeCount = newItem.likeCount,
                )
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
