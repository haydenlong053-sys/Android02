package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeConstraintBroadLayout
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunMemberBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.configuration.CommonBroadConfig.pageSize_20
import com.cboda.intelligent.holder.SelectTypeBroadHolder
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import java.lang.StringBuilder

class XuanQunJinYanBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunMemberBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var mDataList3 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var typeList = arrayListOf<String>()

    private var holder: SelectTypeBroadHolder? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_xu_an_qun_broad_jin_yan
    }
    private var type = "1"
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.xuanquanyuanjinyanle)

        viewModel?.chengyuanListLiveData?.observe(this) {
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
                "shezhijinyantimesuc" -> {
                    finish()
                }
            }
        }

        typeList.add(getString(R.string.jinyanyitian))
        typeList.add(getString(R.string.jinyanyizhou))
        typeList.add(getString(R.string.jinyanyigeyue))
        typeList.add(getString(R.string.jinyanyongjiu))
        holder = SelectTypeBroadHolder(this, jinyantian, gravity = Gravity.START)
        holder?.itemListener = object : SelectTypeBroadHolder.OnItemClickListener {
            override fun onItemClick(content: String) {
                tvjinyan?.text = content
                when (content) {
                    getString(R.string.jinyanyitian) -> {
                        type = "1"
                    }
                    getString(R.string.jinyanyizhou) -> {
                        type = "2"
                    }
                    getString(R.string.jinyanyigeyue) -> {
                        type = "3"
                    }
                    getString(R.string.jinyanyongjiu) -> {
                        type = "4"
                    }
                }
            }
        }
        holder?.setCateList(typeList)

        jinyantian?.setOnClickListener {
            holder?.show()
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

        mAdapter = QunMemberBroadAdapter(R.layout.item_qun_member_broad_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@XuanQunJinYanBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            mDataList[position].isSelect = !mDataList[position].isSelect
            mAdapter?.setList(mDataList)
        }



        tvAgree?.setOnClickListener {
            mDataList3.clear()
            val userIdList = arrayListOf<String>()
            for (i in 0 until mDataList.size) {
                if (mDataList[i].isSelect) {
                    mDataList3.add(mDataList[i])
                    userIdList.add(mDataList[i].userId ?: "")
                }
            }
            if (mDataList3.isEmpty()) {
//                ToastBroadUtils.show(getString(R.string.qingxuanzelianxiren))
                return@setOnClickListener
            }
            val uids = StringBuilder()
            userIdList.forEachIndexed { index, s ->
                if (index != ((userIdList.size) - 1)) {
                    uids.append(s)
                    uids.append("|")
                } else {
                    uids.append(s)
                }

            }
            viewModel?.jinyanshichang(this, uids.toString(), qunId,type)

//            ChatManager.Instance().muteGroupMember(qunId,true,userIdList, arrayListOf<Int>(),null,object :
//                GeneralCallback {
//                override fun onSuccess() {
//                    AppBroadUtil.debug("野火","禁言-->onSuccess ")
//                }
//
//                override fun onFail(errorCode: Int) {
//                    AppBroadUtil.debug("野火","禁言-->onFail:: $errorCode ")
//
//                }
//
//            })

        }
        smartLayout?.setEnableRefresh(true)
        smartLayout?.setEnableLoadMore(true)
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
            "/groupUserListPage", qunId, "3",
            edtSousuo?.text?.toString()?.trim()?:"",pageSize_20
        )
    }

}