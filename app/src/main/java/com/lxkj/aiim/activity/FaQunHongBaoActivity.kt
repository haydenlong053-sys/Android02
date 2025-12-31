package com.lxkj.aiim.activity

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
import com.cboda.baselibrary.shapeview.shape.ShapeConstraintLayout
import com.cboda.baselibrary.toast.ToastUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBean
import com.cboda.baselibrary.utils.AmountInputFilter
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.bean.ZhuanShuHongBaoEvent
import com.lxkj.aiim.holder.HongBaoBottomDialogHolder
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
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.utils.StringUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FaQunHongBaoActivity : BaseSimple2Activity() {

    private var qunid = ""
    private var viewModel: CommViewModel? = null
    private var typeList = arrayListOf<String>()

    //    private var holder: SelectTypeHolder? = null
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
                            this@FaQunHongBaoActivity,
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
                                        this@FaQunHongBaoActivity,
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
                            this@FaQunHongBaoActivity,
                            MiBaoActivity::class.java
                        )
                        intent.putExtra("type", "0")
                        startActivity(intent)
                    } else {
//                    Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                    startActivity(intent);
                        val intent = Intent(
                            this@FaQunHongBaoActivity,
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
        initViews()
        edtjine.filters = arrayOf(AmountInputFilter(1000.0, 2))
        qunid = intent?.getStringExtra("qunid") ?: ""
        getToolbarTitle()?.text = getString(R.string.faqunhongbao)
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.qunUserListLiveData?.observe(this) {
            it.dataList?.apply {
                quinrenshu?.text =
                    StringUtils.format(getString(R.string.qunrenshutis), this.size.toString())
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "fahongbaosuc" -> {
                    val sendCustomMsg = SendCustomMsg()
//                    sendCustomMsg.sendQunHongBaoMsg(
//                        this@FaQunHongBaoActivity,
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


//        holder = SelectTypeHolder(this, hongbaoleixing, gravity = Gravity.START)
//        holder?.itemClickPosListener = object : SelectTypeHolder.OnItemClickPosListener {
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
            val hongBaoBottomDialogHolder = HongBaoBottomDialogHolder(this, typeList)
            hongBaoBottomDialogHolder.callBack =
                object : HongBaoBottomDialogHolder.BottomDialogClickCallBack {
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
            JumpUtils.startQunMemberListActivity(this, qunid, "zhuanshuhongbao")
        }

        tvPhoneLogin?.setOnClickListener {
            if ((type == "1" || type == "2") && edthongbaonun?.text.isNullOrEmpty()) {
                ToastUtils.show(getString(R.string.hongboashuliangtips))
                return@setOnClickListener
            }
            if ((type == "1" || type == "2") && (edthongbaonun?.text?.trim().toString()
                    .toFloat()) <= 0
            ) {
                ToastUtils.show(getString(R.string.hongboashuliangtips1))
                return@setOnClickListener
            }

            if (type == "3" && qunmingceng?.text.isNullOrEmpty()) {
                ToastUtils.show(getString(R.string.xuanzefageishui))
                return@setOnClickListener
            }

            if (edtjine?.text?.trim().isNullOrEmpty()) {
                ToastUtils.show(getString(R.string.querenfahongbaotips))
                return@setOnClickListener
            }

            if ((edtjine?.text?.trim().toString().toFloat()) <= 0) {
                ToastUtils.show(getString(R.string.querenfahongbaotips1))
                return@setOnClickListener
            }

            val holder = PayPsHolder(this, edtjine?.text?.trim()?.toString() ?: "")
            holder.lisetener = object : PayPsHolder.InputLisetener {
                override fun inputResult(content: String) {
                    viewModel?.faqunhongbao(
                        this@FaQunHongBaoActivity,
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
    fun onMsgReceive(event: ZhuanShuHongBaoEvent) {
        GlideLoaderHelper.loadCircleUrl(
            this,
            event.user.icon ?: "",
            quntouxiang
        )
        qunmingceng?.text = event.user.name ?: ""
        zhuanshuUser = event.user
    }

    private var zhuanshuUser: CommonDataListBean.CommonBean? = null
    private lateinit var tvPhoneLogin: TextView
    private lateinit var qianNum: TextView
    private lateinit var edtjieshao: EditText
    private lateinit var edtjine: EditText

    //    private lateinit var tvmiaoshu: TextView
    private lateinit var quinrenshu: TextView
    private lateinit var zhuanshulay: ShapeConstraintLayout
    private lateinit var qunmingceng: TextView
    private lateinit var quntouxiang: ImageView
    private lateinit var geshulayout: ShapeConstraintLayout
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
            ToastUtils.show(R.string.is_not_yet_available)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}