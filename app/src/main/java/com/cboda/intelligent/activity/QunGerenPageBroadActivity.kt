package com.cboda.intelligent.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cc.shinichi.library.ImagePreview
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.*
import com.cboda.intelligent.viewmodel.CommViewBroadModel


class QunGerenPageBroadActivity : BaseSimpleBroadActivity() {
    private var qunid = ""
    private var uid = ""
    private var source = ""
    private var type = ""
    private var viewModel: CommViewBroadModel? = null
    private var qunxiangqing: CommonResultBroadBean.CommonResult? = null
    private var duifang: CommonResultBroadBean.CommonResult? = null
    private lateinit var tianjiadaotongxun: TextView
    private lateinit var gongnenglay: LinearLayoutCompat
    private lateinit var chehuishifenzhong: TextView
    private lateinit var yichubenqun: TextView
    private lateinit var shezhijinyan: LinearLayoutCompat
    private lateinit var tvnicheng: TextView
    private lateinit var ivxingbie: ImageView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang:
            ImageView

    private fun initViews() {
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        ivxingbie = findViewById(R.id.ivxingbie)
        tvnicheng = findViewById(R.id.tvnicheng)
        shezhijinyan = findViewById(R.id.shezhijinyan)
        yichubenqun = findViewById(R.id.yichubenqun)
        chehuishifenzhong = findViewById(R.id.chehuishifenzhong)
        gongnenglay = findViewById(R.id.gongnenglay)
        tianjiadaotongxun = findViewById(R.id.tianjiadaotongxun)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent?.getStringExtra("type") ?: ""
        source = intent?.getStringExtra("source") ?: ""
        qunid = intent?.getStringExtra("qunid") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        initViews()
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.gonggaoLiveData?.observe(this) {
            it.data?.apply {
                qunxiangqing = this
                when (this.userType) {
                    "1" -> {
                        //群主
                        shezhijinyan?.isVisible = uid != BroadConstant.getRealUid()
                        gongnenglay?.isVisible = uid != BroadConstant.getRealUid()
                        initData()
                    }
                    "2" -> {
                        //管理
                        if (type == "1" || type == "2") {
                            //对方是群主和管理
                            shezhijinyan?.isVisible = false
                            gongnenglay?.isVisible = false
                        } else {
                            shezhijinyan?.isVisible = true
                            gongnenglay?.isVisible = true
                        }
                    }
                    else -> {
                        shezhijinyan?.isVisible = false
                        gongnenglay?.isVisible = false
                        if (qunxiangqing?.openQysl=="1"){
                            tianjiadaotongxun?.isVisible = false
                        }
                    }
                }
            }
        }
        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                duifang = this
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    this@QunGerenPageBroadActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadBroadUtils.dip2px(this@QunGerenPageBroadActivity, 10f)
                )

                tvname?.text = this.bei ?: ""
                tvnicheng?.text = StringBroadUtils.format(getString(R.string.nichengshi), "${this.name}")
                ivxingbie?.setImageResource(if (this.sex == "1") R.mipmap.ic_broad_nan else R.mipmap.ic_broad_nv)
                if (this@QunGerenPageBroadActivity.uid == MMKVBroadUtils.getRealUserId()){
                    tianjiadaotongxun?.isVisible = false
                }else if (this.isFri == "1" || uid == BroadConstant.getRealUid()) {
                    tianjiadaotongxun?.isVisible = false
                } else {
                    tianjiadaotongxun?.isVisible = true
                }

                viewModel?.gonggaoxiangqing(this@QunGerenPageBroadActivity, qunid)
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "yichubenqunsuc" -> {
                    finish()
                }
            }
        }



        yichubenqun?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.yichubenqun),
                StringBroadUtils.format(getString(R.string.jiangyonghhuyichuquan, duifang?.name ?: "")),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        viewModel?.yichubenqun(this@QunGerenPageBroadActivity, uid, qunid)
//                        val memberIds = arrayListOf<String>()
//                        memberIds.add(uid)
//                        ChatManager.Instance().removeGroupMembers(qunid,memberIds, arrayListOf<Int>(),null,object :
//                            GeneralCallback{
//                            override fun onSuccess() {
//                                AppBroadUtil.debug("野火","移除群成员---> onSuccess")
//                            }
//
//                            override fun onFail(errorCode: Int) {
//                                AppBroadUtil.debug("野火","移除群成员---> onFail:: $errorCode")
//                            }
//
//                        })
                    }
                }
            }
            holder.show()
        }

        tianjiadaotongxun?.setOnClickListener {
            JumpBroadUtils.startHaoyouApplyActivity(
                this,
                uid,
                duifang?.name ?: "",
                source,
                qunid,
                "1"
            )
        }

        ivTouxiang?.setOnClickListener {
            ImagePreview
                .getInstance()
                .setContext(this)
                .setIndex(0)
                .setImage(duifang?.icon!!)
                .start()
        }

        chehuishifenzhong?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.wenxintishi),
                getString(R.string.chehuishifenzhongfayantips),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {

                        val conversation = Conversation()
                        conversation.type = Conversation.ConversationType.Group
                        conversation.target = qunid
                       val xiaoxiList = ChatManager.Instance().searchMessageByTypesAndTimes(
                            conversation,
                            "",
                            arrayListOf<Int>(),
                            System.currentTimeMillis() - 10*60*1000,
                            System.currentTimeMillis(),
                            true,
                            0,
                            0,
                            uid
                        )
                        AppBroadUtils.debug("野火","撤回消息数量--> ${xiaoxiList?.size}")
                        xiaoxiList?.forEachIndexed { index, message ->
                            ChatManager.Instance().recallMessage(message,object : GeneralCallback{
                                override fun onSuccess() {
                                   AppBroadUtils.debug("野火","撤回消息-->onSuccess")
                                }

                                override fun onFail(errorCode: Int) {
                                    AppBroadUtils.debug("野火","撤回消息-->onFail:: $errorCode")
                                }
                            })
                        }
                        chehuishifenzhong?.postDelayed({
                            viewModel?.yichubenqun(
                                this@QunGerenPageBroadActivity,
                                uid,
                                qunid
                            )
                        }, 1000)

                    }
                }
            }
            holder.show()
        }

        shezhijinyan?.setOnClickListener {
            JumpBroadUtils.startQunGerenJInyanTimeActivity(this, qunid, uid)
        }

        viewModel?.imUser(this, uid)

    }

    private fun initData() {
        if (type == "1" || type == "2") {
            //对方是群主和管理
            shezhijinyan?.isVisible = false
            gongnenglay?.isVisible = false
        } else {
            shezhijinyan?.isVisible = true
            gongnenglay?.isVisible = true
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qun_ge_broad_ren_page
    }
}