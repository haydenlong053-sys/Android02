package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SquareNoticeMessageBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.SquareMessageBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.SquareNoticeActivityBinding
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

class SquareNoticeActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareNoticeActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private val adapter = SquareNoticeMessageBroadAdapter()
    private var pageNo = 1
    private var totalPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareNoticeActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        initView()
        loadMessageList()
    }

    private fun initView() {
        val binding = mBinding ?: return
        binding.returnView.click { finish() }
        binding.backIcon.click { finish() }
        binding.clearView.click { readAllMessages() }
        binding.clearIcon.click { readAllMessages() }
        binding.ruleCard.click { SquareExplanationActivity.start(this) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SquareNoticeActivity)
            adapter = this@SquareNoticeActivity.adapter
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.noticeItemContent -> {
                    if (isSwipeMenuOpened(view)) {
                        closeSwipeMenu(view)
                        return@setOnItemChildClickListener
                    }
                    onMessageClick(adapter.getItem(position))
                }
                R.id.noticeDeleteBtn -> deleteSingleMessage(adapter.getItem(position), position)
            }
        }
        initRefreshLayout()
    }

    private fun isSwipeMenuOpened(contentView: View): Boolean {
        val swipeLayout = contentView.parent as? SwipeMenuLayout ?: return false
        return swipeLayout.scrollX != 0
    }

    private fun closeSwipeMenu(contentView: View) {
        val swipeLayout = contentView.parent as? SwipeMenuLayout ?: return
        swipeLayout.smoothClose()
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
                loadMessageList()
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
        loadMessageList()
    }

    private fun loadMessageList() {
        lifecycleScope.launch {
            viewModel.getMessageList(pageNo, CommonBroadConfig.pageSize)
                .observe(this@SquareNoticeActivity) { result ->
                    showData(result)
                }
        }
    }

    private fun showData(result: NetworkListBroadResult<SquareMessageBean>) {
        mBinding?.smart?.finishRefresh()
        mBinding?.smart?.finishLoadMore()
        totalPage = result.totalPage.coerceAtLeast(1)

        val list = result.list.orEmpty()
        if (pageNo == 1) {
            adapter.setList(list)
        } else {
            adapter.addData(list)
        }
        mBinding?.llNoData?.isVisible = adapter.data.isEmpty()
        if (pageNo >= totalPage) {
            mBinding?.smart?.setEnableLoadMore(false)
        }
    }

    private fun onMessageClick(item: SquareMessageBean) {
        val messageId = item.id.trim()
        if (messageId.isEmpty()) {
            navigateToMessage(item)
            return
        }
        lifecycleScope.launch {
            viewModel.readMessage(messageId).observe(this@SquareNoticeActivity) { success ->
                if (success == true && item.isUnread()) {
                    adapter.markRead(messageId)
                }
                navigateToMessage(item)
            }
        }
    }

    private fun navigateToMessage(item: SquareMessageBean) {
        if (item.isSystemMessage()) {
            return
        }
        if (item.isCommentType() && item.targetId.isBlank()) {
            showToastMain(getString(R.string.square_notice_comment_deleted))
            return
        }
        val targetId = item.targetId.trim()
        if (targetId.isEmpty()) return
        when (item.targetType) {
            SquareMessageBean.TARGET_SQUARE -> SquareDetailActivity.start(this, targetId)
            SquareMessageBean.TARGET_MOMENT -> {
                val userId = item.fromUserId.trim()
                if (userId.isNotEmpty()) {
                    SquareUserActivity.start(this, userId)
                }
            }
            else -> SquareDetailActivity.start(this, targetId)
        }
    }

    private fun deleteSingleMessage(item: SquareMessageBean, position: Int) {
        val messageId = anyToInt(item.id.trim()).toString()
        if (messageId.isEmpty()) return
        lifecycleScope.launch {
            viewModel.deleteMessage(messageId).observe(this@SquareNoticeActivity) { success ->
                if (success == true) {
                    adapter.removeAt(position)
                    mBinding?.llNoData?.isVisible = adapter.data.isEmpty()
                }
            }
        }
    }

    private fun readAllMessages() {
        lifecycleScope.launch {
            viewModel.readMessage().observe(this@SquareNoticeActivity) { success ->
                if (success == true) {
                    adapter.markAllRead()
                }
            }
        }
    }

    override fun getViewBindingClass() = SquareNoticeActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = false
}
