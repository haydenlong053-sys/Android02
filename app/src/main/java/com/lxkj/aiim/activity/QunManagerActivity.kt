package com.lxkj.aiim.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.shapeview.shape.ShapeConstraintBroadLayout
import com.cboda.intelligent.R
import com.lxkj.aiim.adapter.QunGuanLiAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager

class QunManagerActivity:BaseSimpleActivity() {
    private var qunId = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: QunGuanLiAdapter? = null
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
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
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

        mAdapter = QunGuanLiAdapter(R.layout.item_qun_guanli_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(this@QunManagerActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            viewModel?.yichuguanli(this,mDataList[position].userId?:"",qunId,"3")
        }

        tvPhoneLogin?.setOnClickListener {
            JumpUtils.startQunMemberListActivity(this,qunId,"qunmanager")
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