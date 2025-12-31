package com.lxkj.aiim.activity

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.YanZhengAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBean
import com.lxkj.aiim.lj.utils.ToastUtil
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class YanZhengActivity : BaseSimpleActivity() {
    private var fromPage = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: YanZhengAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBean.CommonBean>()
    private var pageNo = 1
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtSousuo:
            EditText

    companion object {
        const val FRIEND_APPLY = "xindepengyou"
        const val GROUP_INVITED = "ruqunyaoqing"
    }

    private fun initViews() {
        edtSousuo = findViewById(R.id.edtSousuo)
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smartLayout)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_yanzheng
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        fromPage = intent?.getStringExtra("fromPage") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        getToolbarTitle()?.text =
            if (fromPage == "xindepengyou") getString(R.string.xindepengyou) else getString(R.string.ruqunyaoqing)


        viewModel?.yanzhengListLiveData?.observe(this) {
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
                "tongyiSuc" -> {
                    toEditId = ""
                    toEditUserId = ""
                    pageNo = 1
                    mDataList.clear()
                    request()
                }
            }
        }
        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                if (this.isFri == "1") {
                    JumpUtils.startHaoYouAddActivity(this@YanZhengActivity, this.id ?: "", "4")
                } else {
                    JumpUtils.startHaoYouAddActivity(this@YanZhengActivity, this.id ?: "", "4")
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
//            mDataList.add(CommonDataListBean.CommonBean())
//        }
        mAdapter = YanZhengAdapter(R.layout.item_yanzheng_user_layout, fromPage)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(this@YanZhengActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.addChildClickViewIds(R.id.tvNotAgree, R.id.tvAgree)
        mAdapter?.setOnItemChildClickListener { _, view, positio ->
            if (mDataList.isEmpty()) return@setOnItemChildClickListener
            when (view.id) {
                R.id.tvNotAgree -> {
                    viewModel?.jujue(this, mDataList[positio].id ?: "", fromPage)
                }

                R.id.tvAgree -> {
                    if (fromPage == FRIEND_APPLY) {
                        toEditId = mDataList[positio].id ?: ""
                        toEditUserId = mDataList[positio].userId ?: ""
                        CircleAndRemarkActivity.toPage(
                            this,
                            startForResult,
                            mDataList[positio].getDisplayName() ?: "",
                            ""
                        )

                    } else {
                        viewModel?.tongyi(this, mDataList[positio].id ?: "", fromPage)
                    }

                }
            }
        }
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            if (fromPage == "xindepengyou") {
                viewModel?.imUser(this, mDataList[position].userId ?: "")
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

    private fun request() {
        viewModel?.getYanZhengList(
            this,
            pageNo.toString(),
            edtSousuo?.text?.trim()?.toString() ?: "",
            fromPage
        )
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                data?.let {
                    val circleId = it.getIntExtra("circleId", -1)
                    val remarkName = it.getStringExtra("remark")
                    acceptFriendApply(circleId, remarkName ?: "")


                }
            }

        }


    private var toEditId = ""
    private var toEditUserId = ""
    private fun acceptFriendApply(circleId: Int, remarkName: String) {
        viewModel?.tongyi(this, toEditId, fromPage, remarkName, circleId)
        if (fromPage == "xindepengyou") {
            ChatManager.Instance().setBlackList(toEditUserId, false, object :
                GeneralCallback {
                override fun onSuccess() {
                    AppUtil.debug("野火", "取消拉黑成功--->")
                }

                override fun onFail(errorCode: Int) {
                    AppUtil.debug("野火", "取消拉黑失败--->$errorCode")
                }

            })
        }
    }

}