package com.lxkj.aiim.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.base.commonlibrary.bean.CommonResultBean.CommonResult
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CommViewModel

class QunJinYanActivity : BaseSimpleActivity() {
    private var qunId = ""
    private var viewModel: CommViewModel? = null
    private var userIdList = arrayListOf<String>()
    override fun getLayoutId(): Int {
        return R.layout.activity_qunjinyan
    }

    private var qunxiangqing : CommonResult? = null
    private lateinit var quanyuanjinyanView: View
    private lateinit var quanyuanjinyan: SwitchCompat
    private lateinit var zidongjinyanView: View
    private lateinit var zidongjinyan: SwitchCompat
    private lateinit var minganci: LinearLayoutCompat
    private lateinit var yijinyanmingdan: LinearLayoutCompat
    private lateinit var chengyuanjinyan:
            LinearLayoutCompat

    private fun initViews() {
        chengyuanjinyan = findViewById(R.id.chengyuanjinyan)
        yijinyanmingdan = findViewById(R.id.yijinyanmingdan)
        minganci = findViewById(R.id.minganci)
        zidongjinyan = findViewById(R.id.zidongjinyan)
        zidongjinyanView = findViewById(R.id.zidongjinyanView)
        quanyuanjinyan = findViewById(R.id.quanyuanjinyan)
        quanyuanjinyanView = findViewById(R.id.quanyuanjinyanView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        getToolbarTitle()?.text = getString(R.string.qunjinyan)
        viewModel?.gonggaoLiveData?.observe(this) {
            it.data?.apply {
                qunxiangqing = this
                zidongjinyan?.isChecked = this.openMgc == "1"
                quanyuanjinyan?.isChecked = this.allJy == "1"
            }
        }
        viewModel?.qunchengyuanListLiveData?.observe(this){
            userIdList.clear()
            it.dataList?.forEachIndexed { index, commonBean ->
                userIdList.add(commonBean.userId?:"")
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "xiugaiqunxinxisuc"->{
                    viewModel?.gonggaoxiangqing(this, qunId)
                }
            }
        }
        viewModel?.qunGuanMemberliList(this,qunId)
        chengyuanjinyan?.setOnClickListener {
            JumpUtils.startXuanQunJinYanActivity(this,qunId)
        }

        yijinyanmingdan?.setOnClickListener {
            JumpUtils.startQunJinYanMemberActivity(this,qunId)
        }

        minganci?.setOnClickListener {
            JumpUtils.startMinGanCiActivity(this,qunId,qunxiangqing?.mgc?:"")
        }
        zidongjinyanView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(this,qunId,"openMgc",if (zidongjinyan?.isChecked == true)"0" else "1")
        }
        quanyuanjinyanView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(this,qunId,"allJy",if (quanyuanjinyan?.isChecked == true)"0" else "1")

//            ChatManager.Instance().muteGroupMember(qunId,quanyuanjinyan?.isChecked != true,userIdList, arrayListOf<Int>(),null,object :
//                GeneralCallback {
//                override fun onSuccess() {
//                    AppUtil.debug("野火","全员禁言解禁-->onSuccess ")
//                }
//
//                override fun onFail(errorCode: Int) {
//                    AppUtil.debug("野火","全员禁言解禁-->onFail:: $errorCode ")
//
//                }
//
//            })
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.gonggaoxiangqing(this,qunId)
    }
}