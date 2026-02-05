package com.cboda.intelligent.activity

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getGroupNotice
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class QunGonggaoInfoBroadActivity:BaseSimpleBroadActivity() {
    private var qunid = ""
    private var viewModel: CommViewBroadModel? = null
    private lateinit var gonggaoxinxi: TextView

    private fun initViews() {
        gonggaoxinxi = findViewById(R.id.gonggaoxinxi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunid = intent?.getStringExtra("qunid") ?: ""
        showCacheGroupNoticeData(qunid)
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.qungonggapo)
        viewModel?.gonggaoLiveData?.observe(this){
            gonggaoxinxi?.text = it.data?.gonggao?:""
        }
        viewModel?.gonggaoxiangqing(this,qunid)

    }

    private fun showCacheGroupNoticeData(qunId: String) {
        //群聊页先取本地缓存的群公告数据
        if (qunId.isNotEmpty()) {
            val groupNotice = getGroupNotice(qunId)
            gonggaoxinxi.text =groupNotice
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qun_gong_broad_gao
    }
}