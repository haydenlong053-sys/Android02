package com.lxkj.aiim.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.QunGuanLiAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.xdroid.IndexBar.widget.IndexBar

class QunJinYanMemberActivity : BaseSimpleActivity() {
    private var qunId = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: QunGuanLiAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private lateinit var tvSideBarHint: TextView
    private lateinit var indexBar: IndexBar
    private lateinit var tvAgree: ShapeTextBroadView
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
        return R.layout.activity_xuanzelianxiren
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
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
                    mAdapter?.setEmptyView(R.layout.list_empty_layout)
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


        mAdapter = QunGuanLiAdapter(R.layout.item_qun_guanli_layout, "jiechujinyan")
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(this@QunJinYanMemberActivity)
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
//                        AppUtil.debug("野火", "解除禁言-->onSuccess ")
//                    }
//
//                    override fun onFail(errorCode: Int) {
//                        AppUtil.debug("野火", "解除禁言-->onFail:: $errorCode ")
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