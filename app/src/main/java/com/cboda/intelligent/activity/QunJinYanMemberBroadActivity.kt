package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunGuanLiBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.cboda.susindexbar.IndexBar.widget.IndexBar2

class QunJinYanMemberBroadActivity : BaseSimpleBroadActivity() {
    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunGuanLiBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private lateinit var tvSideBarHint: TextView
    private lateinit var indexBar: IndexBar2
    private lateinit var tvAgree: TextView
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var edtSousuo:
            EditText

    private fun initViews() {
        edtSousuo = findViewById(R.id.edtSousuo)
        recyclerView2 = findViewById(R.id.recyclerView2)
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smartLayout)
        tvAgree = findViewById(R.id.tvAgree)
        indexBar = findViewById(R.id.indexBar)
        tvSideBarHint = findViewById(R.id.tvSideBarHint)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_xu_broad_an_ze_lian_xi_ren
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.xuanzelianxiren)
        tvAgree?.isVisible = false
        viewModel?.jinyanListLiveData?.observe(this) {
            smartLayout?.finishRefresh()
            smartLayout?.finishLoadMore()
            it.dataList?.apply {
                if (this.isNotEmpty()) {
                    pageNo++
                }
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)

                if (mDataList.isEmpty()) {
                    mAdapter?.setEmptyView(R.layout.list_empty_broad_layout)
                }
                if (mDataList.size >= (it.totalCount ?: "0").toInt()) {
                    smartLayout?.setEnableLoadMore(false)
                }

            }
        }

        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "caozuomembersuc" -> {
                    finish()
                }
            }
        }


        edtSousuo.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->

            if (i == EditorInfo.IME_ACTION_SEARCH) {
                pageNo = 1
                mDataList.clear()
                request()
                return@OnEditorActionListener true
            }
            false
        })


        mAdapter = QunGuanLiBroadAdapter(R.layout.item_qun_guan_broad_li_layout, "jiechujinyan")
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@QunJinYanMemberBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            viewModel?.jeichujinyan(this, qunId, mDataList[position].userId ?: "")
//            val userIdList = arrayListOf(mDataList[position].userId ?: "")
//            ChatManager.Instance()
//                .muteGroupMember(qunId, false, userIdList, arrayListOf<Int>(), null, object :
//                    GeneralCallback {
//                    override fun onSuccess() {
//                        AppBroadUtil.debug("野火", "解除禁言-->onSuccess ")
//                    }
//
//                    override fun onFail(errorCode: Int) {
//                        AppBroadUtil.debug("野火", "解除禁言-->onFail:: $errorCode ")
//
//                    }
//
//                })
        }
        smartLayout?.setEnableLoadMore(false)
        smartLayout?.setEnableRefresh(false)
        smartLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                pageNo = 1
                mDataList.clear()
                request()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                request()
            }

        })
        request()
    }


    private fun request() {
        viewModel?.jinyanList(
            this,
            qunId,
            edtSousuo?.text?.toString()?.trim() ?: ""
        )
    }
}