package com.lxkj.aiim.activity

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
import com.base.commonlibrary.bean.Constant
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.base.commonlibrary.bean.CommonResultBean
import com.base.commonlibrary.utils.MMKVUtils
import com.base.commonlibrary.utils.eLogD
import com.lxkj.aiim.holder.CommanConfimHolder
import com.lxkj.aiim.utils.*
import com.lxkj.aiim.viewmodel.CommViewModel


class QunGerenPageActivity : BaseSimpleActivity() {
    private var qunid = ""
    private var uid = ""
    private var source = ""
    private var type = ""
    private var viewModel: CommViewModel? = null
    private var qunxiangqing: CommonResultBean.CommonResult? = null
    private var duifang: CommonResultBean.CommonResult? = null
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
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.gonggaoLiveData?.observe(this) {
            it.data?.apply {
                qunxiangqing = this
                when (this.userType) {
                    "1" -> {
                        //群主
                        shezhijinyan?.isVisible = uid != Constant.getRealUid()
                        gongnenglay?.isVisible = uid != Constant.getRealUid()
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
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    this@QunGerenPageActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelUtils.dip2px(this@QunGerenPageActivity, 10f)
                )

                tvname?.text = this.bei ?: ""
                tvnicheng?.text = StringUtils.format(getString(R.string.nichengshi), "${this.name}")
                ivxingbie?.setImageResource(if (this.sex == "1") R.mipmap.ic_nan else R.mipmap.ic_nv)
                if (this@QunGerenPageActivity.uid == MMKVUtils.getRealUserId()){
                    tianjiadaotongxun?.isVisible = false
                }else if (this.isFri == "1" || uid == Constant.getRealUid()) {
                    tianjiadaotongxun?.isVisible = false
                } else {
                    tianjiadaotongxun?.isVisible = true
                }

                viewModel?.gonggaoxiangqing(this@QunGerenPageActivity, qunid)
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
            val holder = CommanConfimHolder(
                this,
                getString(R.string.yichubenqun),
                StringUtils.format(getString(R.string.jiangyonghhuyichuquan, duifang?.name ?: "")),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        viewModel?.yichubenqun(this@QunGerenPageActivity, uid, qunid)
//                        val memberIds = arrayListOf<String>()
//                        memberIds.add(uid)
//                        ChatManager.Instance().removeGroupMembers(qunid,memberIds, arrayListOf<Int>(),null,object :
//                            GeneralCallback{
//                            override fun onSuccess() {
//                                AppUtil.debug("野火","移除群成员---> onSuccess")
//                            }
//
//                            override fun onFail(errorCode: Int) {
//                                AppUtil.debug("野火","移除群成员---> onFail:: $errorCode")
//                            }
//
//                        })
                    }
                }
            }
            holder.show()
        }

        tianjiadaotongxun?.setOnClickListener {
            JumpUtils.startHaoyouApplyActivity(
                this,
                uid,
                duifang?.name ?: "",
                source,
                qunid,
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
            val holder = CommanConfimHolder(
                this,
                getString(R.string.wenxintishi),
                getString(R.string.chehuishifenzhongfayantips),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimHolder.OnConfimCallBack {
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
                        AppUtil.debug("野火","撤回消息数量--> ${xiaoxiList?.size}")
                        xiaoxiList?.forEachIndexed { index, message ->
                            ChatManager.Instance().recallMessage(message,object : GeneralCallback{
                                override fun onSuccess() {
                                   AppUtil.debug("野火","撤回消息-->onSuccess")
                                }

                                override fun onFail(errorCode: Int) {
                                    AppUtil.debug("野火","撤回消息-->onFail:: $errorCode")
                                }
                            })
                        }
                        chehuishifenzhong?.postDelayed({
                            viewModel?.yichubenqun(
                                this@QunGerenPageActivity,
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
            JumpUtils.startQunGerenJInyanTimeActivity(this, qunid, uid)
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
        return R.layout.activity_qungeren_page
    }
}