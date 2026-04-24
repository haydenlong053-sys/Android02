package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class QunUserList2BroadActivity : BaseSimpleBroadActivity() {
    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunUserBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var totalPage = 1
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var llNoData: LinearLayout
    private lateinit var recyclerView: RecyclerView

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smart)
        llNoData = findViewById(R.id.llNoData)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qun_user_broad_list2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.qunUserListPageLiveData2?.observe(this) {
            smartLayout.finishRefresh()
            smartLayout.finishLoadMore()
            totalPage = it.totalPage
            it.dataList?.apply {
                if (pageNo == 1) {
                    mDataList.clear()
                }
                if (this.isNotEmpty()) {
                    pageNo++
                }
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)
            }
            llNoData.visibility = if (mDataList.isEmpty()) View.VISIBLE else View.GONE
            if (pageNo >= totalPage) {
                smartLayout.setEnableLoadMore(false)
            }
        }

        mAdapter = QunUserBroadAdapter(R.layout.item_qun_user_broad_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(
                this@QunUserList2BroadActivity,
                4
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            JumpBroadUtils.startQunGerenPageActivity(
                this,
                qunId,
                mDataList[position].userId ?: "",
                "1",
                mDataList[position].type ?: "",
            )
        }
        smartLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                pageNo = 1
                totalPage = 1
                smartLayout.setEnableLoadMore(true)
                getQunUserPage2()
                refreshLayout.setNoMoreData(false)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (pageNo >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    return
                }
                getQunUserPage2()
            }
        })
        smartLayout.setEnableLoadMore(true)
        //此处仅用加载更多
        smartLayout.setEnableRefresh(false)
        getQunUserPage2()
    }

    private fun getQunUserPage2() {
        viewModel?.getQunUserPage2(this, qunId, pageNo, CommonBroadConfig.pageSize_40)
    }

}