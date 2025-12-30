package com.lxkj.aiim.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.QunUserAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.base.commonlibrary.bean.CommonDataListBean
import com.base.commonlibrary.bean.CommonResultBean.CommonResult
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CommViewModel

class QunUserListActivity : BaseSimpleActivity() {
    private var qunId = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: QunUserAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBean.CommonBean>()
    private var pageNo = 1
    private var qunxiangBena:CommonResult? = null
    private lateinit var recyclerView: RecyclerView

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qun_user_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.qunUserListLiveData?.observe(this) {
            it.dataList?.apply {
                if (qunxiangBena?.userType == "1" || qunxiangBena?.userType == "2"){
                    mDataList.clear()
                    mDataList.addAll(this)
                    mAdapter?.setList(mDataList)
                } else {
                    if (qunxiangBena?.openQykj == "1"){
                        mDataList.clear()
                        this.forEachIndexed { index, commonBean ->
                            if (commonBean.type == "1"|| commonBean.type == "2"){
                                mDataList.add(commonBean)
                            }
                        }
                        mAdapter?.setList(mDataList)
                    } else {
                        mDataList.clear()
                        mDataList.addAll(this)
                        mAdapter?.setList(mDataList)
                    }
                }

            }

        }

        viewModel?.gonggaoLiveData?.observe(this){
            it.data?.apply {
                qunxiangBena = this
            }
            viewModel?.getQunUser(this, qunId)
        }

        mAdapter = QunUserAdapter(R.layout.item_qun_user_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(
                this@QunUserListActivity,
                4
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener{_,_,position->
            if (mDataList.isEmpty())return@setOnItemClickListener
            JumpUtils.startQunGerenPageActivity(
                this,
                qunId,
                mDataList[position].userId ?: "",
                "1",
                mDataList[position].type ?: "",
            )
        }

        viewModel?.gonggaoxiangqing(this,qunId)
    }


}