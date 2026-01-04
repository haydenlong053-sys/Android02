package com.lxkj.aiim.activity

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
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.QunMemberAdapter
import com.lxkj.aiim.adapter.YiXuanQunMemberAdapter
import com.lxkj.aiim.app.LiXinApp
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.lxkj.aiim.bean.ZhuanShuHongBaoEvent
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.xdroid.IndexBar.widget.IndexBar
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class QunMemberListActivity : BaseSimple2Activity() {
    private var fromPage = ""
    private var qunId = ""
    private var userType = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: QunMemberAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var mDataList2 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var mDataList3 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var mAdapter2: YiXuanQunMemberAdapter? = null
    private var mManager: LinearLayoutManager? = null
    private lateinit var tvSideBarHint: TextView
    private lateinit var indexBar: IndexBar
    private lateinit var tvAgree: ShapeTextBroadView
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var edtSousuo:
            EditText

    override fun showBgTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return false
    }

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
        fromPage = intent?.getStringExtra("fromPage") ?: ""
        qunId = intent?.getStringExtra("qunid") ?: ""
        userType = intent?.getStringExtra("userType") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        getToolbarTitle()?.text = getString(R.string.xuanzelianxiren)
        if (fromPage == "zhuanshuhongbao") {
            tvAgree?.isVisible = false
        }

        viewModel?.chengyuanListLiveData?.observe(this) {
            smartLayout?.finishRefresh()
            smartLayout?.finishLoadMore()
            it.dataList?.apply {
                if (this.isNotEmpty()) {
                    pageNo++
                }
                mDataList.addAll(this)

                if (fromPage == "remove") {
                    if (userType == "1") {
                        mDataList.forEachIndexed { index, commonBean ->
                            if (commonBean.type == "1") {
                                commonBean.isZy = "1"
                            }
                        }
                    } else if (userType == "2") {
                        mDataList.forEachIndexed { index, commonBean ->
                            if (commonBean.type == "1" || commonBean.type == "2") {
                                commonBean.isZy = "1"
                            }
                        }
                    }

                }
                indexBar.setmSourceDatas(mDataList) //设置数据
                    .setHeaderViewCount(0) //设置HeaderView数量
                    .invalidate()

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
                "caozuomembersuc", "yichuguanliSuc" -> {
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

        mManager = LinearLayoutManager(this)
        //使用indexBar
        indexBar.setmPressedShowTextView(tvSideBarHint) //设置HintTextView
            .setNeedRealIndex(true) //设置需要真实的索引
            .setmLayoutManager(mManager) //设置RecyclerView的LayoutManager
        mAdapter = QunMemberAdapter(R.layout.item_qun_member_layout, fromPage)
        recyclerView?.apply {
            layoutManager = mManager
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            if (fromPage == "zhuanshuhongbao") {
                EventBus.getDefault().post(ZhuanShuHongBaoEvent(mDataList[position]))
                finish()
            }
            if (fromPage == "add" && mDataList[position].isZy == "1") {
                return@setOnItemClickListener
            }
            if (fromPage == "remove" && mDataList[position].isZy == "1") {
                return@setOnItemClickListener
            }
            mDataList[position].isSelect = !mDataList[position].isSelect
            mAdapter?.setList(mDataList)
            if ("add" == fromPage || "qunmanager" == fromPage || "qunyiyintonghua" == fromPage) {
                if (mDataList[position].isSelect) {
                    addXuan(mDataList[position])
                } else {
                    removeXuan(mDataList[position])
                }
                updateYiXuan()
            }
        }


        mAdapter2 = YiXuanQunMemberAdapter(R.layout.item_yixuan_qun_member_layout)
        recyclerView2?.apply {
            layoutManager = WrapContentLinearLayoutManager(
                this@QunMemberListActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mAdapter2
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
                ToastBroadUtils.show(getString(R.string.qingxuanzelianxiren))
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
            when (fromPage) {
                "qunyiyintonghua" -> {

                }

                "remove" -> {
                    viewModel?.rechuqun(this, qunId, uids.toString())
                }

                "add" -> {
                    viewModel?.larenjinqun(this, qunId, uids.toString())
                }

                "qunmanager" -> {
                    viewModel?.yichuguanli(this, uids.toString(), qunId, "2")
                }
            }
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

    private fun getPosByTag(txt: String): Int {
        var pos = -1
        for (i in 0 until mDataList.size) {
            if (mDataList[i].zimu == txt) {
                pos = i
                break
            }
        }
        return pos
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
        var url = ""
        var type = ""
        when (fromPage) {
            "zhuanshuhongbao" -> {
                url = "/groupUserList"
            }

            "add" -> {
                url = "/friendList"
            }

            "remove" -> {
                url = "/groupUserList"
                type = "3"
            }

            "qunmanager" -> {
                url = "/groupUserList"
                type = "3"
            }

            "qunyiyintonghua" -> {
                url = "/groupUserList"
            }
        }
        viewModel?.getQunMemberList(
            this,
            pageNo.toString(),
            url,
            qunId,
            type,
            edtSousuo?.text?.toString()?.trim() ?: ""
        )
    }
}