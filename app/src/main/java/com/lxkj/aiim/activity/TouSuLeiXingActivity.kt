package com.lxkj.aiim.activity

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.TouSuLeixingAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.lxkj.aiim.bean.TouSuLeiXingEvent
import com.lxkj.aiim.viewmodel.CommViewModel
import org.greenrobot.eventbus.EventBus

class TouSuLeiXingActivity:BaseSimpleActivity() {
    private var viewModel: CommViewModel? = null
    private var mAdapter: TouSuLeixingAdapter? = null
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

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.tousuleixingListLiveData?.observe(this){
            it.dataList?.apply {
                mDataList.clear()
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)
            }

        }
        viewModel?.tousuleixing(this,"","")

        mAdapter = TouSuLeixingAdapter(R.layout.item_tousuleixing_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(
                this@TouSuLeiXingActivity,
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
            EventBus.getDefault().post(TouSuLeiXingEvent(mDataList2))
            finish()
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_tousuleixing
    }

}