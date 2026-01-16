package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.YanZhengBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class VerifyBroadActivity : BaseSimpleBroadActivity() {
    private var fromPage = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: YanZhengBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
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
        return R.layout.activity_yan_broad_zheng
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        fromPage = intent?.getStringExtra("fromPage") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
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
                    mAdapter?.setEmptyView(R.layout.list_empty_broad_layout)
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
                    JumpBroadUtils.startHaoYouAddActivity(this@VerifyBroadActivity, this.id ?: "", "4")
                } else {
                    JumpBroadUtils.startHaoYouAddActivity(this@VerifyBroadActivity, this.id ?: "", "4")
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
//            mDataList.add(CommonDataListBroadBean.CommonBean())
//        }
        mAdapter = YanZhengBroadAdapter(R.layout.item_yan_broad_zheng_user_layout, fromPage)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@VerifyBroadActivity)
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
                        CircleAndRemarkBroadActivity.toPage(
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
                    AppBroadUtils.debug("野火", "取消拉黑成功--->")
                }

                override fun onFail(errorCode: Int) {
                    AppBroadUtils.debug("野火", "取消拉黑失败--->$errorCode")
                }

            })
        }
    }

}