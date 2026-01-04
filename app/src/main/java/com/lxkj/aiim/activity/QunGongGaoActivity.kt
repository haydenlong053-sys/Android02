package com.lxkj.aiim.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.viewmodel.CommViewModel

class QunGongGaoActivity:BaseSimpleActivity() {

    private var qunId = ""
    private var qunName = ""
    private var viewModel: CommViewModel? = null
    private lateinit var rightLayout: LinearLayoutCompat
    private lateinit var tvAgree: TextView
    private lateinit var tvNotAgree: TextView
    private lateinit var tvNumHint: TextView
    private lateinit var edtBeizhu:
            EditText

    private fun initViews() {
        edtBeizhu = findViewById(R.id.edtBeizhu)
        tvNumHint = findViewById(R.id.tvNumHint)
        tvNotAgree = findViewById(R.id.tvNotAgree)
        tvAgree = findViewById(R.id.tvAgree)
        rightLayout = findViewById(R.id.rightLayout)
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_xiugaiqungonggao
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        qunName = intent?.getStringExtra("qunName") ?: ""
        getToolbarTitle()?.text = getString(R.string.qungonggapo)

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "xiugaigonggaoSuc" -> {
                    YeHuoCommonBridge.getInstance()?.setYeHuoQiaoJie(this@QunGongGaoActivity,"huoququnxiangqing",qunId,"",null)

//                    val v2TIMGroupInfo =  V2TIMGroupInfo()
//                    v2TIMGroupInfo.groupID = qunId
//                    v2TIMGroupInfo.notification = edtBeizhu?.text?.trim()?.toString() ?: ""
//                    V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, object :
//                        V2TIMCallback {
//                        override fun onSuccess() {
//                            AppUtil.debug("修改群公告","onSuccess ---> ")
//                        }
//
//                        override fun onError(code: Int, desc: String?) {
//                            AppUtil.debug("修改群公告","onError ---> $code  $desc")
//                        }
//
//                    })

                    finish()
                }
            }
        }
        if (qunName.isNotEmpty()) {
            edtBeizhu?.setText(qunName)
        }

        tvNotAgree?.setOnClickListener {
            finish()
        }

        edtBeizhu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                tvNumHint.text = if (p0?.length == 0) {
                    "0/600"
                } else {
                    "${p0?.length}/600"
                }
            }

        })

        tvAgree?.setOnClickListener {
            if (edtBeizhu?.text?.trim().isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.qingshuruqungonggao))
                return@setOnClickListener
            }

            viewModel?.xiugaigonggao(this, qunId, edtBeizhu?.text?.trim()?.toString() ?: "")
        }
    }
}