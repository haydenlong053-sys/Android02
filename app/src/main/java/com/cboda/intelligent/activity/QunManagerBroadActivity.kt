package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeConstraintBroadLayout
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunGuanLiBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager

class QunManagerBroadActivity:BaseSimpleBroadActivity() {
    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: QunGuanLiBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private lateinit var tvPhoneLogin: ShapeConstraintBroadLayout
    private lateinit var recyclerView: RecyclerView

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_quanguanli
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.qunguanliyuan)

        viewModel?.guanliListLiveData?.observe(this) {
            it.dataList?.apply {
                mDataList.clear()
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)
            }
        }

        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "yichuguanliSuc" -> {
                   request()
                }
            }
        }

        mAdapter = QunGuanLiBroadAdapter(R.layout.item_qun_guanli_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(this@QunManagerBroadActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            viewModel?.yichuguanli(this,mDataList[position].userId?:"",qunId,"3")
        }

        tvPhoneLogin?.setOnClickListener {
            JumpBroadUtils.startQunMemberListActivity(this,qunId,"qunmanager")
        }

    }

    override fun onResume() {
        super.onResume()
        request()
    }


    private fun request() {
        viewModel?.qunGuanliList(
            this,
            qunId
        )
    }
}