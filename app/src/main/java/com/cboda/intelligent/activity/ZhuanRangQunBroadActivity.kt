package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeConstraintBroadLayout
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunMemberBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class ZhuanRangQunBroadActivity : BaseSimpleBroadActivity() {
    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunMemberBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var mDataList3 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtSousuo: EditText
    private lateinit var tvAgree: ShapeTextBroadView
    private lateinit var jinyantian: ShapeConstraintBroadLayout
    private lateinit var tvjinyan: TextView
    private lateinit var jinyants:
            TextView

    private fun initViews() {
        jinyants = findViewById(R.id.jinyants)
        tvjinyan = findViewById(R.id.tvjinyan)
        jinyantian = findViewById(R.id.jinyantian)
        tvAgree = findViewById(R.id.tvAgree)
        edtSousuo = findViewById(R.id.edtSousuo)
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smartLayout)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_xuan_qunjinyan
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.xuanxinqunzhu)
        jinyants?.visibility = View.INVISIBLE
        jinyantian?.visibility = View.INVISIBLE

        viewModel?.chengyuanListLiveData?.observe(this) {
            smartLayout?.finishRefresh()
            smartLayout?.finishLoadMore()
            it.dataList?.apply {
                if (this.isNotEmpty()) {
                    pageNo++
                }
                mDataList.addAll(this)
                mDataList.forEachIndexed { index, commonBean ->
                    if (commonBean.type == "1") {
                        commonBean.isZy = "1"
                    }
                }
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
                "zhuanrangqunsuc" -> {
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

        mAdapter = QunMemberBroadAdapter(R.layout.item_qun_member_layout, "remove")
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@ZhuanRangQunBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            if (mDataList[position].isZy == "1") {
                return@setOnItemClickListener
            }
            for (i in 0 until mDataList.size) {
                mDataList[i].isSelect = i == position
            }
            mAdapter?.setList(mDataList)
        }

        tvAgree?.setOnClickListener {
            mDataList3.clear()
            for (i in 0 until mDataList.size) {
                if (mDataList[i].isSelect) {
                    mDataList3.add(mDataList[i])
                }
            }
            if (mDataList3.isEmpty()) {
                ToastBroadUtils.show(getString(R.string.qingxuanzelianxiren))
                return@setOnClickListener
            }

            viewModel?.zhuanrang(this, mDataList3[0].userId ?: "", qunId)

        }

        smartLayout?.setEnableRefresh(false)
        smartLayout?.setEnableLoadMore(false)
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
        viewModel?.getQunMemberList(
            this,
            pageNo.toString(),
            "/groupUserList", qunId, "",
            edtSousuo?.text?.toString()?:""
        )
    }
}