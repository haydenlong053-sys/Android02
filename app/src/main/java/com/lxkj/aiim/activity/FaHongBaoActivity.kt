package com.lxkj.aiim.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.AmountInputFilter
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.holder.PayPsHolder
import com.lxkj.aiim.holder.SendCustomMsg
import com.lxkj.aiim.lj.MiBaoActivity
import com.lxkj.aiim.lj.SetPayPasswordActivity
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.dialogfra.NormalDialog
import com.lxkj.aiim.lj.dialogfra.NormalDialog.OnButtonClick
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.utils.StringUtil
import com.lxkj.aiim.lj.utils.click
import com.lxkj.aiim.viewmodel.CommViewModel
import okhttp3.Request
import okhttp3.Response

class FaHongBaoActivity : BaseSimple2Activity() {
    private var uid = ""
    private var viewModel: CommViewModel? = null
    private lateinit var tvPhoneLogin: TextView
    private lateinit var qianNum: TextView
    private lateinit var edtjieshao: EditText
    private lateinit var edtjine: EditText
    private lateinit var mViewBack: ImageView
    private lateinit var mTvTitle: TextView
    private lateinit var tvCurrencyWx: View

    private fun initViews() {
        edtjine = findViewById(R.id.edtjine)
        edtjieshao = findViewById(R.id.edtjieshao)
        qianNum = findViewById(R.id.qianNum)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
        mTvTitle = findViewById(R.id.mTvTitle)
        mViewBack = findViewById(R.id.mViewBack)
        tvCurrencyWx = findViewById(R.id.tvCurrencyWx)

        mTvTitle.text = getString(R.string.faqunhongbao)
        mViewBack.setOnClickListener {
            finish()
        }
        setIvBgTopImg(R.mipmap.ic_hongbao_top_bg2)
        tvCurrencyWx.click {
            ToastBroadUtils.show(R.string.is_not_yet_available)
        }
    }


    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_fahongbao
    }

    override fun onResume() {
        super.onResume()
        userInfo()
    }

    private fun userInfo() {
        val param: Map<String, Any> = HashMap()
        OkHttpHelper.getInstance()
            .post_json(this, Url.userInfo, param, object : BaseCallback<ResultBean>() {
                override fun onBeforeRequest(request: Request) {
                }

                override fun onFailure(request: Request, e: Exception) {
                }

                override fun onResponse(response: Response) {
                }

                override fun onSuccess(response: Response?, resultBean: ResultBean) {
                    if (resultBean.data.hasPaypwd == "0") {
                        NormalDialog(
                            this@FaHongBaoActivity,
                            getString(R.string.温馨提示),
                            getString(R.string.未设置支付密码),
                            getString(R.string.取消),
                            getString(R.string.前去设置),
                            false
                        )
                            .setOnButtonClickListener(object : OnButtonClick {
                                override fun OnRightClick() {
//                                    if (StringUtil.isEmpty(resultBean.data.mbid)) {
//                                        ptConfig()
//                                    } else {
//                                        Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                                        startActivity(intent);
                                    val intent = Intent(
                                        this@FaHongBaoActivity,
                                        SetPayPasswordActivity::class.java
                                    )
                                    intent.putExtra("editIndex", "1")
                                    startActivity(intent)
//                                    }
                                }

                                override fun OnLeftClick() {
                                    finish()
                                }
                            }).show()
                    }
                }

                override fun onError(response: Response, code: Int, e: Exception) {
                }
            })
    }

    private fun ptConfig() {
        val param: Map<String, Any> = java.util.HashMap()
        OkHttpHelper.getInstance()
            .post_json(this, Url.ptConfig, param, object : BaseCallback<ResultBean>() {
                override fun onBeforeRequest(request: Request) {
                }

                override fun onFailure(request: Request, e: java.lang.Exception) {
                }

                override fun onResponse(response: Response) {
                }

                override fun onSuccess(response: Response, resultBean: ResultBean) {
                    if (resultBean.data.openMibao == "1") {
                        val intent = Intent(
                            this@FaHongBaoActivity,
                            MiBaoActivity::class.java
                        )
                        intent.putExtra("type", "0")
                        startActivity(intent)
                    } else {
//                    Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                    startActivity(intent);
                        val intent = Intent(
                            this@FaHongBaoActivity,
                            SetPayPasswordActivity::class.java
                        )
                        intent.putExtra("editIndex", "1")
                        startActivity(intent)
                    }
                }

                override fun onError(response: Response, code: Int, e: java.lang.Exception) {
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = intent?.getStringExtra("uid") ?: ""
        initViews()
        edtjine.filters = arrayOf(AmountInputFilter(1000.0, 2))
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "fahongbaosuc" -> {
                    val sendCustomMsg = SendCustomMsg()
//                    sendCustomMsg.sendHongBaoMsg(
//                        this@FaHongBaoActivity,
//                        edtjieshao?.text?.trim()?.toString() ?: "",
//                        it.type2,
//                        edtjine?.text?.trim()?.toString() ?: "",
//                        "0",
//                        uid
//                    )
                    sendCustomMsg.sendYeHuoHongBao(
                        "",
                        uid,
                        edtjieshao?.text?.trim()?.toString() ?: "",
                        it.type2,
                        edtjine?.text?.trim()?.toString() ?: "",
                        "0",
                        "",
                        "",
                        "",
                        "",
                        this@FaHongBaoActivity
                    )
                }
            }
        }
        tvPhoneLogin?.setOnClickListener {
            if (edtjine?.text?.trim().isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.querenfahongbaotips))
                return@setOnClickListener
            }

            if ((edtjine?.text?.trim().toString().toFloat()) <= 0) {
                ToastBroadUtils.show(getString(R.string.querenfahongbaotips1))
                return@setOnClickListener
            }

            val holder = PayPsHolder(this, edtjine?.text?.trim()?.toString() ?: "")
            holder.lisetener = object : PayPsHolder.InputLisetener {
                override fun inputResult(content: String) {
                    viewModel?.fahongbao(
                        this@FaHongBaoActivity,
                        uid,
                        edtjine?.text?.trim()?.toString() ?: "",
                        edtjieshao?.text?.trim()?.toString() ?: "",
                        content
                    )
                }
            }
            holder.show()

        }

        edtjine?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                qianNum?.text = edtjine?.text?.trim()?.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }
}