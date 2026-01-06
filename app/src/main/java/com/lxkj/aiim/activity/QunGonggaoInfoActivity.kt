package com.lxkj.aiim.activity

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.viewmodel.CommViewModel

class QunGonggaoInfoActivity:BaseSimpleActivity() {
    private var qunid = ""
    private var viewModel: CommViewModel? = null
    private lateinit var gonggaoxinxi: TextView

    private fun initViews() {
        gonggaoxinxi = findViewById(R.id.gonggaoxinxi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunid = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        getToolbarTitle()?.text = getString(R.string.qungonggapo)
        viewModel?.gonggaoLiveData?.observe(this){
            gonggaoxinxi?.text = it.data?.gonggao?:""
        }
        viewModel?.gonggaoxiangqing(this,qunid)

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qungonggao
    }
}