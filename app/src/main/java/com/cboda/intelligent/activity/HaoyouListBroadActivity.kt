package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunMemberBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.holder.SendCustomBroadMsg
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class HaoyouListBroadActivity : BaseSimpleBroadActivity() {
    private var qunId = ""
    private var uid = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunMemberBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtSousuo: EditText

    private fun initViews() {
        edtSousuo = findViewById(R.id.edtSousuo)
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smartLayout)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_hao_broad_you_list
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        uid = intent?.getStringExtra("uid") ?: ""
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.xuanzelianxiren)

        viewModel?.haoyouListLiveData?.observe(this) {
            smartLayout?.finishRefresh()
            smartLayout?.finishLoadMore()
            it.dataList?.apply {
                if (this.isNotEmpty()) {
                    pageNo++
                }
                if (uid.isNotEmpty() && this.isNotEmpty()){
                    //单聊过滤对方
                    this.removeIf {bean ->
                        bean.userId == uid
                    }
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

//        for (i in 0 until 3) {
//            val bean = CommonDataListBroadBean.CommonBean()
//            bean.id = i.toString()
//            mDataList.add(bean)
//        }

        mAdapter = QunMemberBroadAdapter(R.layout.item_qun_member_broad_layout, "zhuanshuhongbao")
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@HaoyouListBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            val sendCustomMsg = SendCustomBroadMsg()
            if (uid.isNotEmpty()) {
                sendCustomMsg.sendMyImMingPian("", mDataList[position].userId?:"",uid, mDataList[position].name?:"",mDataList[position].icon?:"",this,mDataList[position].idno?:"")
            } else {
                sendCustomMsg.sendMyImMingPian("1", mDataList[position].userId?:"",qunId,mDataList[position].name?:"",mDataList[position].icon?:"",this,mDataList[position].idno?:"")
            }
            finish()

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
        viewModel?.gethaoyoulist(
            this,
            edtSousuo?.text?.trim()?.toString()?:""
        )
    }


}