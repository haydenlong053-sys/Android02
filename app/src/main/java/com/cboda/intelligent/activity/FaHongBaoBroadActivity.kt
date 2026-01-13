package com.cboda.intelligent.activity

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
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.holder.PayPsBroadHolder
import com.cboda.intelligent.holder.SendCustomBroadMsg
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.dialog.NormalBroadDialog.OnButtonClick
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import okhttp3.Request
import okhttp3.Response

class FaHongBaoBroadActivity : BaseSimple2BroadActivity() {
    private var uid = ""
    private var viewModel: CommViewBroadModel? = null
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
        setIvBgTopImg(R.mipmap.ic_hongbao_top_broad_bg2)
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
        OkHttpBroadHelper.getInstance()
            .post_json(this, BroadUrl.userInfo, param, object : BaseBroadCallback<ResultBroadBean>() {
                override fun onBeforeRequest(request: Request) {
                }

                override fun onFailure(request: Request, e: Exception) {
                }

                override fun onResponse(response: Response) {
                }

                override fun onSuccess(response: Response?, resultBean: ResultBroadBean) {
                    if (resultBean.data.hasPaypwd == "0") {
                        NormalBroadDialog(
                            this@FaHongBaoBroadActivity,
                            getString(R.string.温馨提示),
                            getString(R.string.未设置支付密码),
                            getString(R.string.取消),
                            getString(R.string.前去设置),
                            false
                        )
                            .setOnButtonClickListener(object : OnButtonClick {
                                override fun OnRightClick() {
//                                    if (StringBroadUtil.isEmpty(resultBean.data.mbid)) {
//                                        ptConfig()
//                                    } else {
//                                        Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                                        startActivity(intent);
                                    val intent = Intent(
                                        this@FaHongBaoBroadActivity,
                                        SetPayPasswordBroadActivity::class.java
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
        OkHttpBroadHelper.getInstance()
            .post_json(this, BroadUrl.ptConfig, param, object : BaseBroadCallback<ResultBroadBean>() {
                override fun onBeforeRequest(request: Request) {
                }

                override fun onFailure(request: Request, e: java.lang.Exception) {
                }

                override fun onResponse(response: Response) {
                }

                override fun onSuccess(response: Response, resultBean: ResultBroadBean) {
                    if (resultBean.data.openMibao == "1") {
                        val intent = Intent(
                            this@FaHongBaoBroadActivity,
                            MiBaoBroadActivity::class.java
                        )
                        intent.putExtra("type", "0")
                        startActivity(intent)
                    } else {
//                    Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                    startActivity(intent);
                        val intent = Intent(
                            this@FaHongBaoBroadActivity,
                            SetPayPasswordBroadActivity::class.java
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
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "fahongbaosuc" -> {
                    val sendCustomMsg = SendCustomBroadMsg()
//                    sendCustomMsg.sendHongBaoMsg(
//                        this@FaHongBaoBroadActivity,
//                        edtjieshao?.text?.trim()?.toString() ?: "",
//                        it.type2,
//                        edtjine?.text?.trim()?.toString() ?: "",
//                        "0",
//                        uid
//                    )
                    sendCustomMsg.sendMyImHongBao(
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
                        this@FaHongBaoBroadActivity
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

            val holder = PayPsBroadHolder(this, edtjine?.text?.trim()?.toString() ?: "")
            holder.lisetener = object : PayPsBroadHolder.InputLisetener {
                override fun inputResult(content: String) {
                    viewModel?.fahongbao(
                        this@FaHongBaoBroadActivity,
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