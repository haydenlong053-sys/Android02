package com.lxkj.aiim.activity

import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.base.commonlibrary.shapeview.shape.ShapeConstraintLayout
import com.base.commonlibrary.shapeview.shape.ShapeTextView
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.QunMemberAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.base.commonlibrary.bean.CommonDataListBean
import com.lxkj.aiim.holder.SelectTypeHolder
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import java.lang.StringBuilder

class XuanQunJinYanActivity : BaseSimpleActivity() {

    private var qunId = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: QunMemberAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBean.CommonBean>()
    private var pageNo = 1
    private var mDataList3 = arrayListOf<CommonDataListBean.CommonBean>()
    private var typeList = arrayListOf<String>()

    private var holder: SelectTypeHolder? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_xuan_qunjinyan
    }
    private var type = "1"
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtSousuo: EditText
    private lateinit var tvAgree: ShapeTextView
    private lateinit var jinyantian: ShapeConstraintLayout
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
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
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
                    mAdapter?.setEmptyView(R.layout.list_empty_layout)
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
        holder = SelectTypeHolder(this, jinyantian, gravity = Gravity.START)
        holder?.itemListener = object : SelectTypeHolder.OnItemClickListener {
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

        mAdapter = QunMemberAdapter(R.layout.item_qun_member_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(this@XuanQunJinYanActivity)
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
//                ToastUtils.show(getString(R.string.qingxuanzelianxiren))
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
//                    AppUtil.debug("野火","禁言-->onSuccess ")
//                }
//
//                override fun onFail(errorCode: Int) {
//                    AppUtil.debug("野火","禁言-->onFail:: $errorCode ")
//
//                }
//
//            })

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
            "/groupUserList", qunId, "3",
            edtSousuo?.text?.toString()?.trim()?:""
        )
    }

}