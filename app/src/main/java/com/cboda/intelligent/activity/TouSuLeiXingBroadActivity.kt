package com.cboda.intelligent.activity

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.TouSuLeixingBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.bean.TouSuLeiXingBroadEvent
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import org.greenrobot.eventbus.EventBus

class TouSuLeiXingBroadActivity:BaseSimpleBroadActivity() {
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: TouSuLeixingBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var mDataList2 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private lateinit var tvPhoneLogin: TextView
    private lateinit var recyclerView: RecyclerView

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        getToolbarTitle()?.text = getString(R.string.tousuqun)

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.tousuleixingListLiveData?.observe(this){
            it.dataList?.apply {
                mDataList.clear()
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)
            }

        }
        viewModel?.tousuleixing(this,"","")

        mAdapter = TouSuLeixingBroadAdapter(R.layout.item_tousuleixing_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(
                this@TouSuLeiXingBroadActivity,
                4
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)

        mAdapter?.setOnItemClickListener{_,_,position->
            if (mDataList.isEmpty())return@setOnItemClickListener

            mDataList[position].isSelect = !mDataList[position].isSelect
            mAdapter?.setList(mDataList)
        }
        tvPhoneLogin?.setOnClickListener {
            mDataList2.clear()
            for (i in 0 until mDataList.size){
                if (mDataList[i].isSelect){
                    mDataList2.add(mDataList[i])
                }
            }

            if (mDataList2.isEmpty()){
                ToastBroadUtils.show(getString(R.string.xuanzetousulieixng))
                return@setOnClickListener
            }
            EventBus.getDefault().post(TouSuLeiXingBroadEvent(mDataList2))
            finish()
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_tousuleixing
    }

}