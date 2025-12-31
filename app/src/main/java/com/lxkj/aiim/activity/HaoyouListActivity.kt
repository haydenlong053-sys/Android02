package com.lxkj.aiim.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.QunMemberAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBean
import com.lxkj.aiim.holder.SendCustomMsg
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class HaoyouListActivity : BaseSimpleActivity() {
    private var qunId = ""
    private var uid = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: QunMemberAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBean.CommonBean>()
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
        return R.layout.activity_haoyoulist
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        uid = intent?.getStringExtra("uid") ?: ""
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
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
                    mAdapter?.setEmptyView(R.layout.list_empty_layout)
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
//            val bean = CommonDataListBean.CommonBean()
//            bean.id = i.toString()
//            mDataList.add(bean)
//        }

        mAdapter = QunMemberAdapter(R.layout.item_qun_member_layout, "zhuanshuhongbao")
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(this@HaoyouListActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            val sendCustomMsg = SendCustomMsg()
            if (uid.isNotEmpty()) {
//                sendCustomMsg.sendMingPianMsg(
//                    this@HaoyouListActivity,
//                    mDataList[position].userId?:"",
//                    mDataList[position].icon?:"",
//                    mDataList[position].name?:"",
//                    uid,
//                )
                sendCustomMsg.sendYeHuoMingPian("", mDataList[position].userId?:"",uid, mDataList[position].name?:"",mDataList[position].icon?:"",this)
            } else {
//                sendCustomMsg.sendQunMingPianMsg(
//                    this@HaoyouListActivity,
//                    mDataList[position].userId?:"",
//                    mDataList[position].icon?:"",
//                    mDataList[position].name?:"",
//                    qunId,
//                )
                sendCustomMsg.sendYeHuoMingPian("1", mDataList[position].userId?:"",qunId,mDataList[position].name?:"",mDataList[position].icon?:"",this)
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