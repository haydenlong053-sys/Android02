package com.lxkj.aiim.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.toast.ToastUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.viewmodel.CommViewModel

class HaoYouBeizhuActivity:BaseSimpleActivity() {
    private var uid = ""
    private var beizhu = ""
    private var viewModel: CommViewModel? = null
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
        return R.layout.activity_beizhu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        getToolbarTitle()?.text = "设置备注"
        uid = intent?.getStringExtra("uid") ?: ""
        beizhu = intent?.getStringExtra("beizhu")?:""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "shezhibeizhuSuc" -> {
                    finish()
                }
            }
        }
        if(beizhu.isNotEmpty()){
            edtBeizhu?.setText(beizhu)
            ivqingchu?.isVisible = true
        }
        ivqingchu?.setOnClickListener {
            edtBeizhu?.setText("")
        }
        edtBeizhu?.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivqingchu?.isVisible = edtBeizhu?.text?.trim()?.toString()?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        tvPhoneLogin?.setOnClickListener {
            if (edtBeizhu?.text?.trim().isNullOrEmpty()){
                ToastUtils.show(getString(R.string.bveizhutips))
                return@setOnClickListener
            }

            viewModel?.shezhibeizhu(this,uid,edtBeizhu?.text?.trim()?.toString()?:"")

        }
    }
}