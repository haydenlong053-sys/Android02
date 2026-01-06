package com.lxkj.aiim.activity

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.viewmodel.CommViewModel

class QunGerenJInyanTimeActivity:BaseSimpleActivity() {
    private var qunid = ""
    private var uid = ""
    private var viewModel: CommViewModel? = null
    private var type = ""
    private lateinit var tvPhoneLogin: TextView
    private lateinit var yongjiu: FrameLayout
    private lateinit var yongjiuicon: AppCompatImageView
    private lateinit var jinyanyiyueu: FrameLayout
    private lateinit var yueicon: AppCompatImageView
    private lateinit var jinyanyizhou: FrameLayout
    private lateinit var yizhouicon: AppCompatImageView
    private lateinit var jinyanyitian: FrameLayout
    private lateinit var yitianicon:
            AppCompatImageView

    private fun initViews() {
        yitianicon = findViewById(R.id.yitianicon)
        jinyanyitian = findViewById(R.id.jinyanyitian)
        yizhouicon = findViewById(R.id.yizhouicon)
        jinyanyizhou = findViewById(R.id.jinyanyizhou)
        yueicon = findViewById(R.id.yueicon)
        jinyanyiyueu = findViewById(R.id.jinyanyiyueu)
        yongjiuicon = findViewById(R.id.yongjiuicon)
        yongjiu = findViewById(R.id.yongjiu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunid = intent?.getStringExtra("qunid") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        getToolbarTitle()?.text = getString(R.string.xuanshichangjinyanle)
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "shezhijinyantimesuc" -> {
                    finish()
                }
            }
        }
        viewModel?.gonggaoLiveData?.observe(this) {
            type =   it.data?.jyType?:""
            updateView()
        }

        jinyanyitian?.setOnClickListener {
            type = "1"
            updateView()
        }
        jinyanyizhou?.setOnClickListener {
            type = "2"
            updateView()
        }
        jinyanyiyueu?.setOnClickListener {
            type = "3"
            updateView()
        }
        yongjiu?.setOnClickListener {
            type = "4"
            updateView()
        }


        tvPhoneLogin?.setOnClickListener {
            if (type.isEmpty()){
                return@setOnClickListener
            }
            viewModel?.jinyanshichang(this,uid,qunid,type)

//            val memberIds = arrayListOf(uid)
//            ChatManager.Instance().muteGroupMember(qunid,true,memberIds, arrayListOf<Int>(),null,object :
//                GeneralCallback{
//                override fun onSuccess() {
//                   AppUtil.debug("野火","禁言-->onSuccess ")
//                }
//
//                override fun onFail(errorCode: Int) {
//                    AppUtil.debug("野火","禁言-->onFail:: $errorCode ")
//
//                }
//
//            })
        }
        viewModel?.gonggaoxiangqing(this,qunid)
    }

    private fun updateView() {
        yitianicon?.setImageResource(R.drawable.ic_weixuanz)
        yizhouicon?.setImageResource(R.drawable.ic_weixuanz)
        yueicon?.setImageResource(R.drawable.ic_weixuanz)
        yongjiuicon?.setImageResource(R.drawable.ic_weixuanz)

        when(type){
            "1"->{
                yitianicon?.setImageResource(R.drawable.ic_yixuanz)
            }
            "2"->{
                yizhouicon?.setImageResource(R.drawable.ic_yixuanz)
            }
            "3"->{
                yueicon?.setImageResource(R.drawable.ic_yixuanz)
            }
            "4"->{
                yongjiuicon?.setImageResource(R.drawable.ic_yixuanz)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_shezhijinyantime
    }
}