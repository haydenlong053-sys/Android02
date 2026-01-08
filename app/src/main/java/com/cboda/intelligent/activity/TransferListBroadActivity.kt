package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.TransferListBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.databinding.ActivityTransferBroadListBinding
import com.cboda.intelligent.model.OrderInfoBean
import com.cboda.intelligent.viewmodel.TransferViewBroadModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TransferListBroadActivity : BaseSimple2BroadActivity() {
    private var page = 1
    private var totalPage: Int = 1
    private val viewModel: TransferViewBroadModel by viewModels()
    private val mAdapter = TransferListBroadAdapter()
    private var mBind: ActivityTransferBroadListBinding? = null
    private var flag = 0

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityTransferBroadListBinding::class.java
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
        mBind = (binding as? ActivityTransferBroadListBinding)
        initAdapter()
        initListener()

    }

    private fun initAdapter() {
        mBind?.mRvList?.let {
            it.layoutManager = LinearLayoutManager(this@TransferListBroadActivity)
            it.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            startActivity(
                Intent(
                    this@TransferListBroadActivity,
                    TransferDetailBroadActivity::class.java
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
                    .observe(this@TransferListBroadActivity) { resultBean ->
                        showData(resultBean)
                    }
            } else {
                viewModel.getWithdrawInfoList(page)
                    .observe(this@TransferListBroadActivity) { resultBean ->
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
    fun onAppEventReceiver(event: ActionEventBroadBean) {
        if (event.isFinishAction) {
            finish()
        }

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}