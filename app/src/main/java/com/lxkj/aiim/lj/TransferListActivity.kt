package com.lxkj.aiim.lj

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.intelligent.R
import com.lxkj.aiim.adapter.TransferListAdapter
import com.lxkj.aiim.base.BaseSimple2Activity
import com.cboda.intelligent.databinding.ActivityTransferListBinding
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.model.OrderInfoBean
import com.lxkj.aiim.viewmodel.TransferViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TransferListActivity : BaseSimple2Activity() {
    private var page = 1
    private var totalPage: Int = 1
    private val viewModel: TransferViewModel by viewModels()
    private val mAdapter = TransferListAdapter()
    private var mBind: ActivityTransferListBinding? = null
    private var flag = 0

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityTransferListBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        flag = intent.getIntExtra("type", 0)
        mAdapter.flagType = flag
        getToolbarTitle()?.text = if (flag == 1) {
            getResources().getString(R.string.提现记录)
        } else {
            getResources().getString(R.string.mine_Transfer_records)
        }
        mBind = (binding as? ActivityTransferListBinding)
        initAdapter()
        initListener()

    }

    private fun initAdapter() {
        mBind?.mRvList?.let {
            it.layoutManager = LinearLayoutManager(this@TransferListActivity)
            it.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            startActivity(
                Intent(
                    this@TransferListActivity,
                    TransferDetailActivity::class.java
                ).apply {
                    putExtra("type", flag)
                    putExtra("id", mAdapter.data[position].id)
                })
        }
        mBind?.smart?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                page = 1
                initData()
                refreshLayout.setNoMoreData(false)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (page >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    return
                }
                page++
                initData()
            }
        })
        initData()
    }

    private fun initData() {
        lifecycleScope.launch {
            if (flag == 0) {
                viewModel.getTransferInfoList(page)
                    .observe(this@TransferListActivity) { resultBean ->
                        showData(resultBean)
                    }
            } else {
                viewModel.getWithdrawInfoList(page)
                    .observe(this@TransferListActivity) { resultBean ->
                        showData(resultBean)
                    }
            }

        }
    }

    private fun showData(resultBean: NetworkListBroadResult<OrderInfoBean>) {
        totalPage = resultBean.totalPage
        mBind?.smart?.finishLoadMore()
        mBind?.smart?.finishRefresh()

        if (page == 1) {
            mAdapter.setList(resultBean.list)
        } else {
            resultBean.list?.let {
                mAdapter.addData(it)
            }
        }
        if (resultBean.list?.isEmpty() == true) {
            mBind?.llNoData?.visibility = View.VISIBLE
        } else {
            mBind?.llNoData?.visibility = View.INVISIBLE
        }

    }

    private fun initListener() {

    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionEventBean) {
        if (event.isFinishAction) {
            finish()
        }

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}