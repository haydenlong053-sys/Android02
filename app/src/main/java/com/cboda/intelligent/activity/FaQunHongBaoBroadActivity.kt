package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.shapeview.shape.ShapeConstraintBroadLayout
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.utils.AmountInputFilter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.ZhuanShuHongBaoBroadEvent
import com.cboda.intelligent.holder.HongBaoBottomDialogBroadHolder
import com.cboda.intelligent.holder.PayPsBroadHolder
import com.cboda.intelligent.holder.SendCustomBroadMsg
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.dialog.NormalBroadDialog.OnButtonClick
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FaQunHongBaoBroadActivity : BaseSimple2BroadActivity() {

    private var qunid = ""
    private var viewModel: CommViewBroadModel? = null
    private var typeList = arrayListOf<String>()

    //    private var holder: SelectTypeBroadHolder? = null
    private var type = "1"//默认手气红包


    override fun getLayoutId(): Int {
        return R.layout.activity_faqunhongbao
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun showTransparentTopTitleLayout(): Boolean {
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
                            this@FaQunHongBaoBroadActivity,
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
                                        this@FaQunHongBaoBroadActivity,
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
                            this@FaQunHongBaoBroadActivity,
                            MiBaoBroadActivity::class.java
                        )
                        intent.putExtra("type", "0")
                        startActivity(intent)
                    } else {
//                    Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                    startActivity(intent);
                        val intent = Intent(
                            this@FaQunHongBaoBroadActivity,
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
        initViews()
        edtjine.filters = arrayOf(AmountInputFilter(1000.0, 2))
        qunid = intent?.getStringExtra("qunid") ?: ""
        getToolbarTitle()?.text = getString(R.string.faqunhongbao)
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.qunUserListLiveData?.observe(this) {
            it.dataList?.apply {
                quinrenshu?.text =
                    StringBroadUtils.format(getString(R.string.qunrenshutis), this.size.toString())
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "fahongbaosuc" -> {
                    val sendCustomMsg = SendCustomBroadMsg()
//                    sendCustomMsg.sendQunHongBaoMsg(
//                        this@FaQunHongBaoBroadActivity,
//                        edtjieshao?.text?.trim()?.toString() ?: "",
//                        it.type2,
//                        edtjine?.text?.trim()?.toString() ?: "",
//                        "0",
//                        qunid,
//                        type,
//                        zhuanshuUser?.userId?:"",
//                        zhuanshuUser?.icon?:"",
//                        zhuanshuUser?.name?:"",
//                    )

                    sendCustomMsg.sendYeHuoHongBao(
                        "1",
                        qunid,
                        edtjieshao?.text?.trim()?.toString() ?: "",
                        it.type2,
                        edtjine?.text?.trim()?.toString() ?: "",
                        "0",
                        type,
                        zhuanshuUser?.userId ?: "",
                        zhuanshuUser?.icon ?: "",
                        zhuanshuUser?.name ?: "",
                        this
                    )
                }
            }
        }
        typeList.add(getString(R.string.pinshouqihongboa))
        typeList.add(getString(R.string.putonghongboa))
        typeList.add(getString(R.string.zhuanshuhongboa))


//        holder = SelectTypeBroadHolder(this, hongbaoleixing, gravity = Gravity.START)
//        holder?.itemClickPosListener = object : SelectTypeBroadHolder.OnItemClickPosListener {
//            override fun onItemClickPos(pos: Int) {
//                hongbaoleixing?.text = typeList[pos]
//
//                when (pos) {
//                    0 -> {
//                        type = "1"
//                        tvmiaoshu?.text = getString(R.string.hongbaojinesu)
//                        geshulayout?.isVisible = true
//                        zhuanshulay?.isVisible = false
//                    }
//
//                    1 -> {
//                        type = "2"
//                        tvmiaoshu?.text = getString(R.string.dangejine)
//                        geshulayout?.isVisible = true
//                        zhuanshulay?.isVisible = false
//                    }
//
//                    2 -> {
//                        type = "3"
//                        tvmiaoshu?.text = getString(R.string.hongbaojinesu)
//                        geshulayout?.isVisible = false
//                        zhuanshulay?.isVisible = true
//                    }
//                }
//            }
//        }
//        holder?.setCateList(typeList)
//
        hongbaoleixing?.setOnClickListener {
            val hongBaoBottomDialogHolder = HongBaoBottomDialogBroadHolder(this, typeList)
            hongBaoBottomDialogHolder.callBack =
                object : HongBaoBottomDialogBroadHolder.BottomDialogClickCallBack {
                    override fun clickItem(pos: Int) {
                        hongbaoleixing?.text = typeList[pos]
                        when (pos) {
                            0 -> {
                                type = "1"
//                            tvmiaoshu?.text = getString(R.string.hongbaojinesu)
                                geshulayout?.isVisible = true
                                zhuanshulay?.isVisible = false
                            }

                            1 -> {
                                type = "2"
//                            tvmiaoshu?.text = getString(R.string.dangejine)
                                geshulayout?.isVisible = true
                                zhuanshulay?.isVisible = false
                            }

                            2 -> {
                                type = "3"
//                            tvmiaoshu?.text = getString(R.string.hongbaojinesu)
                                geshulayout?.isVisible = false
                                zhuanshulay?.isVisible = true
                            }
                        }
                    }

                }
            hongBaoBottomDialogHolder.show()
        }

        zhuanshulay?.setOnClickListener {
            JumpBroadUtils.startQunMemberListActivity(this, qunid, "zhuanshuhongbao")
        }

        tvPhoneLogin?.setOnClickListener {
            if ((type == "1" || type == "2") && edthongbaonun?.text.isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.hongboashuliangtips))
                return@setOnClickListener
            }
            if ((type == "1" || type == "2") && (edthongbaonun?.text?.trim().toString()
                    .toFloat()) <= 0
            ) {
                ToastBroadUtils.show(getString(R.string.hongboashuliangtips1))
                return@setOnClickListener
            }

            if (type == "3" && qunmingceng?.text.isNullOrEmpty()) {
                ToastBroadUtils.show(getString(R.string.xuanzefageishui))
                return@setOnClickListener
            }

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
                    viewModel?.faqunhongbao(
                        this@FaQunHongBaoBroadActivity,
                        qunid,
                        type,
                        edtjine?.text?.trim()?.toString() ?: "",
                        if (type == "3") "1" else edthongbaonun?.text?.trim()?.toString() ?: "",
                        zhuanshuUser?.userId ?: "",
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
        viewModel?.getQunUser(this, qunid)
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsgReceive(event: ZhuanShuHongBaoBroadEvent) {
        GlideLoaderBroadHelper.loadCircleUrl(
            this,
            event.user.icon ?: "",
            quntouxiang
        )
        qunmingceng?.text = event.user.name ?: ""
        zhuanshuUser = event.user
    }

    private var zhuanshuUser: CommonDataListBroadBean.CommonBean? = null
    private lateinit var tvPhoneLogin: TextView
    private lateinit var qianNum: TextView
    private lateinit var edtjieshao: EditText
    private lateinit var edtjine: EditText

    //    private lateinit var tvmiaoshu: TextView
    private lateinit var quinrenshu: TextView
    private lateinit var zhuanshulay: ShapeConstraintBroadLayout
    private lateinit var qunmingceng: TextView
    private lateinit var quntouxiang: ImageView
    private lateinit var geshulayout: ShapeConstraintBroadLayout
    private lateinit var tvgeshu: TextView
    private lateinit var edthongbaonun: EditText
    private lateinit var hongbaoleixing: TextView

    private lateinit var mViewBack: ImageView
    private lateinit var mTvTitle: TextView
    private lateinit var tvCurrencyWx: View

    private fun initViews() {
        hongbaoleixing = findViewById(R.id.hongbaoleixing)
        edthongbaonun = findViewById(R.id.edthongbaonun)
        tvgeshu = findViewById(R.id.tvgeshu)
        geshulayout = findViewById(R.id.geshulayout)
        quntouxiang = findViewById(R.id.quntouxiang)
        qunmingceng = findViewById(R.id.qunmingceng)
        zhuanshulay = findViewById(R.id.zhuanshulay)
        quinrenshu = findViewById(R.id.quinrenshu)
//        tvmiaoshu = findViewById(R.id.tvmiaoshu)
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}