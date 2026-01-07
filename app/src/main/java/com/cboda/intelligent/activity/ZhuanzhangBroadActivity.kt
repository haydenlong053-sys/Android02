package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.holder.PayPsBroadHolder
import com.cboda.intelligent.holder.SendCustomBroadMsg
import com.cboda.intelligent.holder.ZhuanZhangKeyBoardBroadHolder
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.dialog.NormalBroadDialog.OnButtonClick
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.StringBroadUtil
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.HideKeyboardBroadUtils
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import okhttp3.Request
import okhttp3.Response

class ZhuanzhangBroadActivity : BaseSimpleBroadActivity() {

    private var uid = ""
    private var viewModel: CommViewBroadModel? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_zhuanzhang
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
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
                            this@ZhuanzhangBroadActivity,
                            getString(R.string.温馨提示),
                            getString(R.string.未设置支付密码),
                            getString(R.string.取消),
                            getString(R.string.前去设置),
                            false
                        )
                            .setOnButtonClickListener(object : OnButtonClick {
                                override fun OnRightClick() {
                                    if (StringBroadUtil.isEmpty(resultBean.data.mbid)) {
                                        ptConfig()
                                    } else {
//                                        Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                                        startActivity(intent);
                                        val intent = Intent(
                                            this@ZhuanzhangBroadActivity,
                                            SetPayPasswordBroadActivity::class.java
                                        )
                                        intent.putExtra("editIndex", "1")
                                        startActivity(intent)
                                    }
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
                            this@ZhuanzhangBroadActivity,
                            MiBaoBroadActivity::class.java
                        )
                        intent.putExtra("type", "0")
                        startActivity(intent)
                    } else {
//                    Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                    startActivity(intent);
                        val intent = Intent(
                            this@ZhuanzhangBroadActivity,
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

    private var zhuanzhangjinapan:ZhuanZhangKeyBoardBroadHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        tvname?.text = StringBroadUtils.format(getString(R.string.zhuanzhuanggei, ""))
        uid = intent?.getStringExtra("uid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "zhuanzhangsuc" -> {
                    val sendCustomMsg = SendCustomBroadMsg()
//                    sendCustomMsg.sendZhuanZhangMsg(
//                        this@ZhuanzhangBroadActivity,
//                        "",
//                        it.type2,
//                        qianNum?.text?.trim()?.toString() ?: "",
//                        "0",
//                        uid
//                    )

                    sendCustomMsg.sendYeHuoZhuanZhang(
                        "",
                        uid,
                        "",
                        it.type2,
                        qianNum?.text?.trim()?.toString() ?: "",
                        "0",
                        this@ZhuanzhangBroadActivity
                    )
                }
            }
        }

        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    this@ZhuanzhangBroadActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadBroadUtils.dip2px(this@ZhuanzhangBroadActivity, 7f)
                )

                tvname?.text =
                    StringBroadUtils.format(getString(R.string.zhuanzhuanggei, this.name ?: ""))
//                tvnicheng?.text =
//                    StringBroadUtils.format(getString(R.string.shoujihaoshi, this.phone ?: ""))
            }
        }

        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = zhuanzhangvitool?.layoutParams
        linearParams?.height = result
        zhuanzhangvitool?.layoutParams = linearParams

        fanhui?.setOnClickListener {
            finish()
        }

        val hideKeyboardUtils = HideKeyboardBroadUtils()
        hideKeyboardUtils.hideKeyboard(qianNum)
        qianNum.setOnClickListener {
            if (zhuanzhangjinapan?.pop?.isShowing == true){
                return@setOnClickListener
            }
            zhuanzhangjinapan = ZhuanZhangKeyBoardBroadHolder(this,window.decorView,qianNum)
            zhuanzhangjinapan?.zhuanZhangLisetener = object :ZhuanZhangKeyBoardBroadHolder.ZhuanZhangLisetener{
                override fun zhuanzhang() {
                    val holder = PayPsBroadHolder(this@ZhuanzhangBroadActivity, qianNum?.text?.trim()?.toString() ?: "", "2")
                    holder.lisetener = object : PayPsBroadHolder.InputLisetener {
                        override fun inputResult(content: String) {
                            viewModel?.zhuanzhang(
                                this@ZhuanzhangBroadActivity,
                                uid,
                                qianNum.text.trim().toString(),
                                content
                            )
                        }
                    }
                    holder.show()
                }
            }
            zhuanzhangjinapan?.show()
        }

        qianNum?.setOnFocusChangeListener { v, hasFocus ->
            if (zhuanzhangjinapan?.pop != null && isUiCreated){
                zhuanzhangjinapan?.refreshKeyboardOutSideTouchable(!hasFocus)
            }
        }

        qianNum.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            if (TextUtils.isEmpty(qianNum.text.toString())) {
                return@OnEditorActionListener true
            }
            if (i == EditorInfo.IME_ACTION_DONE) {
                val holder = PayPsBroadHolder(this, qianNum?.text?.trim()?.toString() ?: "", "2")
                holder.lisetener = object : PayPsBroadHolder.InputLisetener {
                    override fun inputResult(content: String) {
                        viewModel?.zhuanzhang(
                            this@ZhuanzhangBroadActivity,
                            uid,
                            qianNum.text.trim().toString(),
                            content
                        )
                    }
                }
                holder.show()
                return@OnEditorActionListener true
            }
            false
        })

        viewModel?.imUser(this, uid)
        qianNum?.requestFocus()


    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        isUiCreated = true
    }

    private var isUiCreated = false
    private lateinit var qianNum: EditText
    private lateinit var tvnicheng: TextView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang: ImageView
    private lateinit var fanhui: AppCompatImageView
    private lateinit var zhuanzhangvitool:
            View

    private fun initViews() {
        zhuanzhangvitool = findViewById(R.id.zhuanzhangvitool)
        fanhui = findViewById(R.id.fanhui)
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        tvnicheng = findViewById(R.id.tvnicheng)
        qianNum = findViewById(R.id.qianNum)
    }

}