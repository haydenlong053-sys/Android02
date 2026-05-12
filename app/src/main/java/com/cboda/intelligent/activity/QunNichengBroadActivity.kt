package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class QunNichengBroadActivity : BaseSimple2BroadActivity() {

    private var qunid = ""
    private var beizhu = ""
    private var viewModel: CommViewBroadModel? = null
    private lateinit var tvPhoneLogin: TextView
    private lateinit var ivqingchu: AppCompatImageView
    private lateinit var edtBeizhu: EditText
    private lateinit var tbeizhu:
            TextView

    private fun initViews() {
        tbeizhu = findViewById(R.id.tbeizhu)
        edtBeizhu = findViewById(R.id.edtBeizhu)
        ivqingchu = findViewById(R.id.ivqingchu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_bei_broad_zhu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        initViews()
        getToolbarTitle()?.text = getString(com.cboda.baselibrary.R.string.group_nick_name)
        qunid = intent?.getStringExtra("qunid") ?: ""
        beizhu = intent?.getStringExtra("beizhu") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "qunichenguSuc" -> {
                    ChatManager.Instance().modifyGroupAlias(
                        qunid,
                        edtBeizhu?.text?.trim()?.toString() ?: "",
                        arrayListOf<Int>(), null, object : GeneralCallback {
                            override fun onSuccess() {
                                eLogD("野火", "修改群昵称onSuccess ---> ")
                            }

                            override fun onFail(errorCode: Int) {
                                eLogD("野火", "修改群那你成onFail ---> $errorCode")
                            }

                        }
                    )
                    finish()
                }
            }
        }
        if (beizhu.isNotEmpty()) {
            edtBeizhu?.setText(beizhu)
            ivqingchu?.isVisible = true
        }
        ivqingchu?.setOnClickListener {
            edtBeizhu?.setText("")
        }
        tbeizhu?.text = getString(R.string.nichengb)
        edtBeizhu?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivqingchu?.isVisible = edtBeizhu?.text?.trim()?.toString()?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        edtBeizhu?.setHint(R.string.shuqubveizhutips)
        tvPhoneLogin?.setOnClickListener {
            if (edtBeizhu?.text?.trim().isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.shuqubveizhutips))
                return@setOnClickListener
            }

            viewModel?.shezhiqunnicheng(this, qunid, edtBeizhu?.text?.trim()?.toString() ?: "")

        }
        findViewById<View>(R.id.tvPhoneCancel).click {
            finish()
        }
    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }
}