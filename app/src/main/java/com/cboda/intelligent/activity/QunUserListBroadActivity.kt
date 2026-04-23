package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean.CommonResult
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class QunUserListBroadActivity : BaseSimpleBroadActivity() {
    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunUserBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var qunxiangBena: CommonResult? = null
    private lateinit var recyclerView: RecyclerView

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qun_user_broad_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.qunUserListPageLiveData?.observe(this) {
            it.dataList?.apply {
                mDataList.clear()
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)
//                if (qunxiangBena?.userType == "1" || qunxiangBena?.userType == "2") {
//                    mDataList.clear()
//                    mDataList.addAll(this)
//                    mAdapter?.setList(mDataList)
//                } else {
//                    if (qunxiangBena?.openQykj == "1") {
//                        mDataList.clear()
//                        this.forEachIndexed { index, commonBean ->
//                            if (commonBean.type == "1" || commonBean.type == "2") {
//                                mDataList.add(commonBean)
//                            }
//                        }
//                        mAdapter?.setList(mDataList)
//                    } else {
//                        mDataList.clear()
//                        mDataList.addAll(this)
//                        mAdapter?.setList(mDataList)
//                    }
//                }

            }

        }

        viewModel?.gonggaoLiveData?.observe(this) {
            it.data?.apply {
                qunxiangBena = this
            }
            viewModel?.getQunUserPage(this, qunId, true)
        }

        mAdapter = QunUserBroadAdapter(R.layout.item_qun_user_broad_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(
                this@QunUserListBroadActivity,
                4
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            JumpBroadUtils.startQunGerenPageActivity(
                this,
                qunId,
                mDataList[position].userId ?: "",
                "1",
                mDataList[position].type ?: "",
            )
        }

        viewModel?.gonggaoxiangqing(this, qunId, true)
    }


}