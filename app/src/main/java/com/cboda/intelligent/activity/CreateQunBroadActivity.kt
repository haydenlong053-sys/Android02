package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunMemberBroadAdapter
import com.cboda.intelligent.adapter.YiXuanQunMemberBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.cboda.susindexbar.IndexBar.widget.IndexBar

class CreateQunBroadActivity : BaseSimpleBroadActivity() {


    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunMemberBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var mDataList2 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var mDataList3 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var mAdapter2: YiXuanQunMemberBroadAdapter? = null
    private lateinit var tvSideBarHint: TextView
    private lateinit var indexBar: IndexBar
    private lateinit var tvAgree: ShapeTextBroadView
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var edtSousuo: EditText

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
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.xuanzelianxiren)
        viewModel?.haoyouListLiveData?.observe(this) {
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
                "createGroupsuc" -> {
//                    val param = Bundle()
//                    param.putInt(
//                        TUIConstants.TUIChat.CHAT_TYPE,
//                        V2TIMConversation.V2TIM_GROUP
//                    )
//                    param.putString(TUIConstants.TUIChat.CHAT_ID, it.type2)
//                    TUICore.startActivity(TUIConstants.TUIChat.GROUP_CHAT_ACTIVITY_NAME, param)
                    AppBroadUtils.goChat(this, it.type2,"1")
                    finish()
                }
            }
        }

        edtSousuo.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->

            if (i == EditorInfo.IME_ACTION_SEARCH) {
                pageNo = 1
                mDataList.clear()
                request()
                mDataList2.clear()
                mAdapter2?.setList(mDataList2)
                return@OnEditorActionListener true
            }
            false
        })

        mAdapter = QunMemberBroadAdapter(R.layout.item_qun_member_broad_layout, "createGroup")
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@CreateQunBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            mDataList[position].isSelect = !mDataList[position].isSelect
            mAdapter?.setList(mDataList)
            if (mDataList[position].isSelect) {
                addXuan(mDataList[position])
            } else {
                removeXuan(mDataList[position])
            }
            updateYiXuan()
        }


        mAdapter2 = YiXuanQunMemberBroadAdapter(R.layout.item_yi_xu_broad_an_qun_member_layout)
        recyclerView2?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(
                this@CreateQunBroadActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mAdapter2
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
            val str = StringBuilder()
            mDataList3.forEachIndexed { index, commonBean ->
                str.append(commonBean.userId ?: "")
                if (index != mDataList3.size - 1) {
                    str.append("|")
                }
            }

            viewModel?.chuangjainqun(this, str.toString())
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

    private fun addXuan(bean: CommonDataListBroadBean.CommonBean) {
        mDataList2.add(bean)
        mAdapter2?.setList(mDataList2)
    }

    private fun removeXuan(bean: CommonDataListBroadBean.CommonBean) {
        val iterator = mDataList2.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.id == bean.id) {
                iterator.remove()
            }
        }
        mAdapter2?.setList(mDataList2)
    }


    private fun updateYiXuan() {
        recyclerView2?.isVisible = mDataList2.isNotEmpty()
    }

    private fun request() {
        viewModel?.gethaoyoulist(
            this,
            edtSousuo?.text?.trim()?.toString() ?: ""
        )
    }
}