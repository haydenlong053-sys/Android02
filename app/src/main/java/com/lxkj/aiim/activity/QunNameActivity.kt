package com.lxkj.aiim.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.model.ModifyGroupInfoType
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.baselibrary.toast.ToastUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.viewmodel.CommViewModel

class QunNameActivity : BaseSimpleActivity() {
    private var qunId = ""
    private var qunName = ""
    private var viewModel: CommViewModel? = null
    private lateinit var tvPhoneLogin: TextView
    private lateinit var ivqingchu: AppCompatImageView
    private lateinit var edtBeizhu: EditText

    private fun initViews() {
        edtBeizhu = findViewById(R.id.edtBeizhu)
        ivqingchu = findViewById(R.id.ivqingchu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_xiugaiqunname
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        qunName = intent?.getStringExtra("qunName") ?: ""
        getToolbarTitle()?.text = getString(R.string.qubmingcheng)

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "xiugaiqunmingSuc" -> {
                    ChatManager.Instance().modifyGroupInfo(qunId,
                        ModifyGroupInfoType.Modify_Group_Name, edtBeizhu?.text?.trim()?.toString() ?: "",
                        arrayListOf<Int>(),null,object : GeneralCallback {
                            override fun onSuccess() {
                                AppUtil.debug("野火","修改群名称onSuccess ---> ")
                            }

                            override fun onFail(errorCode: Int) {
                                AppUtil.debug("野火","修改群名称onFail ---> $errorCode")
                            }

                        }
                    )
                    finish()
                }
            }
        }
        if (qunName.isNotEmpty()) {
            edtBeizhu?.setText(qunName)
            ivqingchu?.isVisible = true
        }
        ivqingchu?.setOnClickListener {
            edtBeizhu?.setText("")
        }
        edtBeizhu?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivqingchu?.isVisible = edtBeizhu?.text?.trim()?.toString()?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        tvPhoneLogin?.setOnClickListener {
            if (edtBeizhu?.text?.trim().isNullOrEmpty()) {
                ToastUtils.show(getString(R.string.qingshuruqunmning))
                return@setOnClickListener
            }

            viewModel?.xiugaiqunmingc(this, qunId, edtBeizhu?.text?.trim()?.toString() ?: "")

        }
    }
}