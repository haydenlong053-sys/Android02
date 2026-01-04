package com.lxkj.aiim.lj

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.NoticeListAdapter
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivityNoticeListBinding
import com.lxkj.aiim.model.NoticeInfoBean
import com.lxkj.aiim.viewmodel.NoticeViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch


//公告列表
class NoticeListActivity : BaseSimple2Activity() {
    private var page = 1
    private var totalPage: Int = 1
    private val viewModel: NoticeViewModel by viewModels()
    private val mAdapter = NoticeListAdapter()
    private var mBind: ActivityNoticeListBinding? = null

    // 直接在属性声明中注册
    private val childActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { data ->
                    handleResultData(data)
                }
            }
        }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityNoticeListBinding::class.java
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EventBus.getDefault().register(this)
        mBind = (binding as? ActivityNoticeListBinding)
        mBind?.toolbar?.mTvTitle?.text = getString(R.string.sys_notice)
        mBind?.toolbar?.mViewBack?.setOnClickListener {
            finish()
        }
        initAdapter()
        initListener()

    }

    private fun initAdapter() {
        mBind?.mRvList?.let {
            it.layoutManager = LinearLayoutManager(this@NoticeListActivity)
            it.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val noticeInfoBean = mAdapter.data[position]
            childActivityLauncher.launch(
                Intent(
                    this@NoticeListActivity,
                    NoticeDetailActivity::class.java
                ).apply {
                    putExtra("data", noticeInfoBean)
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
            viewModel.getNoticeInfoList(page)
                .observe(this@NoticeListActivity) { resultBean ->
                    showData(resultBean)
                }

        }
    }

    private fun showData(resultBean: NetworkListBroadResult<NoticeInfoBean>) {
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


    private fun handleResultData(data: Intent) {
        val noticeInfoBean = data.getSerializableExtra("data") as? NoticeInfoBean
        noticeInfoBean?.id?.let {
            if (mAdapter.data.isNotEmpty()) {
                mAdapter.data.forEachIndexed { index, bean ->
                    if (bean.id == it) {
                        bean.isRead = true
                        mAdapter.notifyItemChanged(index)
                        return@forEachIndexed
                    }
                }
            }
        }

    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onAppEventReceiver(event: ActionEventBean) {
//        if (event.isFinishAction) {
//            finish()
//        }
//
//    }

    override fun onDestroy() {
//        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}