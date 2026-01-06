package com.lxkj.aiim.activity

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
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.holder.PayPsHolder
import com.lxkj.aiim.holder.SendCustomMsg
import com.lxkj.aiim.holder.ZhuanZhangKeyBoardHolder
import com.lxkj.aiim.lj.MiBaoActivity
import com.lxkj.aiim.lj.SetPayPasswordActivity
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.dialogfra.NormalDialog
import com.lxkj.aiim.lj.dialogfra.NormalDialog.OnButtonClick
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.utils.StringUtil
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.HideKeyboardUtils
import com.lxkj.aiim.utils.PixelBroadUtils
import com.lxkj.aiim.utils.StringUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import okhttp3.Request
import okhttp3.Response

class ZhuanzhangActivity : BaseSimpleActivity() {

    private var uid = ""
    private var viewModel: CommViewModel? = null

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
                            this@ZhuanzhangActivity,
                            getString(R.string.温馨提示),
                            getString(R.string.未设置支付密码),
                            getString(R.string.取消),
                            getString(R.string.前去设置),
                            false
                        )
                            .setOnButtonClickListener(object : OnButtonClick {
                                override fun OnRightClick() {
                                    if (StringUtil.isEmpty(resultBean.data.mbid)) {
                                        ptConfig()
                                    } else {
//                                        Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                                        startActivity(intent);
                                        val intent = Intent(
                                            this@ZhuanzhangActivity,
                                            SetPayPasswordActivity::class.java
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
                            this@ZhuanzhangActivity,
                            MiBaoActivity::class.java
                        )
                        intent.putExtra("type", "0")
                        startActivity(intent)
                    } else {
//                    Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                    startActivity(intent);
                        val intent = Intent(
                            this@ZhuanzhangActivity,
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

    private var zhuanzhangjinapan:ZhuanZhangKeyBoardHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        tvname?.text = StringUtils.format(getString(R.string.zhuanzhuanggei, ""))
        uid = intent?.getStringExtra("uid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "zhuanzhangsuc" -> {
                    val sendCustomMsg = SendCustomMsg()
//                    sendCustomMsg.sendZhuanZhangMsg(
//                        this@ZhuanzhangActivity,
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
                        this@ZhuanzhangActivity
                    )
                }
            }
        }

        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    this@ZhuanzhangActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadUtils.dip2px(this@ZhuanzhangActivity, 7f)
                )

                tvname?.text =
                    StringUtils.format(getString(R.string.zhuanzhuanggei, this.name ?: ""))
//                tvnicheng?.text =
//                    StringUtils.format(getString(R.string.shoujihaoshi, this.phone ?: ""))
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

        val hideKeyboardUtils = HideKeyboardUtils()
        hideKeyboardUtils.hideKeyboard(qianNum)
        qianNum.setOnClickListener {
            if (zhuanzhangjinapan?.pop?.isShowing == true){
                return@setOnClickListener
            }
            zhuanzhangjinapan = ZhuanZhangKeyBoardHolder(this,window.decorView,qianNum)
            zhuanzhangjinapan?.zhuanZhangLisetener = object :ZhuanZhangKeyBoardHolder.ZhuanZhangLisetener{
                override fun zhuanzhang() {
                    val holder = PayPsHolder(this@ZhuanzhangActivity, qianNum?.text?.trim()?.toString() ?: "", "2")
                    holder.lisetener = object : PayPsHolder.InputLisetener {
                        override fun inputResult(content: String) {
                            viewModel?.zhuanzhang(
                                this@ZhuanzhangActivity,
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
                val holder = PayPsHolder(this, qianNum?.text?.trim()?.toString() ?: "", "2")
                holder.lisetener = object : PayPsHolder.InputLisetener {
                    override fun inputResult(content: String) {
                        viewModel?.zhuanzhang(
                            this@ZhuanzhangActivity,
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