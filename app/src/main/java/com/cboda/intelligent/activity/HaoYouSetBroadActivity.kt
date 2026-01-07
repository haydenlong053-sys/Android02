package com.cboda.intelligent.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonResultBroadBean.CommonResult
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class HaoYouSetBroadActivity : BaseSimpleBroadActivity() {
    private var uid = ""
    private var uname = ""
    private var viewModel: CommViewBroadModel? = null
//    private var presenter: FriendProfilePresenter? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_haoyou_set
    }

    private var duifang: CommonResult? = null
    private lateinit var tvshanchu: TextView
    private lateinit var zidongfanyi: View
    private lateinit var lockSwitch4: SwitchCompat
    private lateinit var jiaruheimingdan: View
    private lateinit var lockSwitch3: SwitchCompat
    private lateinit var miandarao: View
    private lateinit var lockSwitch2: SwitchCompat
    private lateinit var zhiding: View
    private lateinit var lockSwitch: SwitchCompat

    private fun initViews() {
        lockSwitch = findViewById(R.id.lockSwitch)
        zhiding = findViewById(R.id.zhiding)
        lockSwitch2 = findViewById(R.id.lockSwitch2)
        miandarao = findViewById(R.id.miandarao)
        lockSwitch3 = findViewById(R.id.lockSwitch3)
        jiaruheimingdan = findViewById(R.id.jiaruheimingdan)
        lockSwitch4 = findViewById(R.id.lockSwitch4)
        zidongfanyi = findViewById(R.id.zidongfanyi)
        tvshanchu = findViewById(R.id.tvshanchu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        getToolbarTitle()?.text = "更多设置"
        uname = intent?.getStringExtra("uname") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.imUserLiveData?.observe(this){
            it.data?.apply {
                duifang = this
                lockSwitch?.isChecked = this.isZd == "1"
                lockSwitch3?.isChecked = this.isBlack == "1"
                tvshanchu?.isVisible = this.isFri == "1"
                lockSwitch2.isChecked = this.isMdr == "1"

                lockSwitch4.isChecked = this.isfanyi == "1"
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "shanchusuc" -> {
                   finish()
                }

                "zhidingsuc" -> {
                    viewModel?.imUser(this, uid)
                }

                "miandaraosuc" -> {
                    viewModel?.imUser(this, uid)
                }

                "jiaruheimingdansuc" -> {
                    viewModel?.imUser(this, uid)
                }

                "setFanyisuc" -> {
                    viewModel?.imUser(this, uid)
                }

            }
        }

        tvshanchu?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.shanchulianxiren),
                StringBroadUtils.format(getString(R.string.shanchulianxirentips, duifang?.name?:"")),
                getString(R.string.shanchu),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        viewModel?.shanchu(this@HaoYouSetBroadActivity, uid)
                    }
                }
            }
            holder.show()
        }

        zhiding?.setOnClickListener {
            viewModel?.zhiding(this@HaoYouSetBroadActivity, uid,if (lockSwitch?.isChecked == true)"0" else "1")
            val conversation = Conversation()
            conversation.type = Conversation.ConversationType.Single
            conversation.line = 0
            conversation.target = uid
            ChatManager.Instance().setConversationTop(conversation, if (lockSwitch?.isChecked == true)0 else 1)
        }


        jiaruheimingdan?.setOnClickListener {
            viewModel?.jiaruheimingdan(this@HaoYouSetBroadActivity, uid,if (lockSwitch3?.isChecked == true)"0" else "1")
            ChatManager.Instance().setBlackList(uid,lockSwitch3?.isChecked != true,object :
                GeneralCallback{
                override fun onSuccess() {
                    AppBroadUtils.debug("野火","拉黑成功--->")
                }

                override fun onFail(errorCode: Int) {
                    AppBroadUtils.debug("野火","拉黑失败--->$errorCode")
                }

            })
        }

        miandarao?.setOnClickListener {
            viewModel?.miandarao(this@HaoYouSetBroadActivity, uid,if (lockSwitch2?.isChecked == true)"0" else "1")
            val conversation = Conversation()
            conversation.type = Conversation.ConversationType.Single
            conversation.line = 0
            conversation.target = uid
            ChatManager.Instance().setConversationSilent(conversation, lockSwitch2?.isChecked != true)
        }

        zidongfanyi?.setOnClickListener {
            viewModel?.setFanyi(this@HaoYouSetBroadActivity, uid,if (lockSwitch4?.isChecked == true)"0" else "1")
        }
        viewModel?.imUser(this, uid)
    }
}