package com.cboda.intelligent.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class MinGanCiBroadActivity:BaseSimpleBroadActivity() {
    private var qunId = ""
    private var mgc = ""
    private var viewModel: CommViewBroadModel? = null
    private lateinit var rightLayout: LinearLayoutCompat
    private lateinit var tvAgree: TextView
    private lateinit var edtBeizhu:
            EditText

    private fun initViews() {
        edtBeizhu = findViewById(R.id.edtBeizhu)
        tvAgree = findViewById(R.id.tvAgree)
        rightLayout = findViewById(R.id.rightLayout)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_minganci
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        mgc = intent?.getStringExtra("mgc")?:""
        getToolbarTitle()?.text = getString(R.string.mingancishezhi)

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "mingancisetsuc" -> {
                    finish()
                }
            }
        }

        if (mgc.isNotEmpty()){
            edtBeizhu?.setText(mgc)
        }

        tvAgree?.setOnClickListener {
            if (edtBeizhu?.text?.trim().isNullOrEmpty()) {
                return@setOnClickListener
            }

            viewModel?.mingancishezhi(this, qunId, edtBeizhu?.text?.trim()?.toString() ?: "")
        }
    }
}