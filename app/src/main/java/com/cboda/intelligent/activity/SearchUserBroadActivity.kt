package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SearchUserBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class SearchUserBroadActivity : BaseSimpleBroadActivity() {

    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: SearchUserBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var quxiao: TextView
    private lateinit var edtSousuo: EditText
    private lateinit var hometool:
            View

    private fun initViews() {
        hometool = findViewById(R.id.hometool)
        edtSousuo = findViewById(R.id.edtSousuo)
        quxiao = findViewById(R.id.quxiao)
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smartLayout)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search_broad_user
    }

    override fun getStatusBarColor(): Int {
        return R.color.white
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = hometool?.layoutParams
        linearParams?.height = result
        hometool?.layoutParams = linearParams

        quxiao?.setOnClickListener {
            if (edtSousuo?.text.isNullOrEmpty()) {
                finish()
            } else {
                pageNo = 1
                mDataList.clear()
                request()
            }

        }
        edtSousuo?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                quxiao?.text = if (edtSousuo?.text.isNullOrEmpty()) getString(R.string.quxiao) else getString(R.string.搜索)
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]

        viewModel?.sousuoLiveData?.observe(this) {
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


        edtSousuo.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->

            if (i == EditorInfo.IME_ACTION_SEARCH) {
                pageNo = 1
                mDataList.clear()
                request()
                return@OnEditorActionListener true
            }
            false
        })


        mAdapter = SearchUserBroadAdapter(R.layout.item_search_user_broad_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@SearchUserBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            JumpBroadUtils.startHaoYouAddActivity(this, mDataList[position].userId ?: "", "4")
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
    }

    private fun request() {
        viewModel?.sousuoUser(
            this,
            edtSousuo?.text?.trim()?.toString() ?: ""
        )
    }

    override fun fitsSystemWindows(): Boolean {
        return true
    }
}