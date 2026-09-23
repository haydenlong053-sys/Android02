package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.baselibrary.bean.CommonResultBroadBean.CommonResult
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImConversationHttpExample
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.ImUtils

class HaoYouSetBroadActivity : BaseSimpleBroadActivity() {
    private var uid = ""
    private var uname = ""
    private var viewModel: CommViewBroadModel? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_hao_broad_you_set
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
        getToolbarTitle()?.text = getString(R.string.more_settings)
        uname = intent?.getStringExtra("uname") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                duifang = this
                lockSwitch.isChecked = this.isZd == "1"
                lockSwitch3.isChecked = this.isBlack == "1"
                tvshanchu.isVisible = this.isFri == "1"
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

        tvshanchu.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.shanchulianxiren),
                StringBroadUtils.format(getString(R.string.shanchulianxirentips, duifang?.name ?: "")),
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

        zhiding.setOnClickListener {
            val targetTop = lockSwitch.isChecked != true
            if (HttpsBroadConfig.useSelfImOnly) {
                setSelfImSessionTop(targetTop)
            } else {
                viewModel?.zhiding(
                    this@HaoYouSetBroadActivity,
                    uid,
                    if (targetTop) "1" else "0"
                )
                val conversation = Conversation().apply {
                    type = Conversation.ConversationType.Single
                    line = 0
                    target = uid
                }
                ChatManager.Instance().setConversationTop(
                    conversation,
                    if (targetTop) 1 else 0,
                    object : GeneralCallback {
                        override fun onSuccess() {
                            AppBroadUtils.debug("野火", "置顶成功--->")
                        }

                        override fun onFail(errorCode: Int) {
                            AppBroadUtils.debug("野火", "置顶失败--->$errorCode")
                        }
                    }
                )
            }
        }

        jiaruheimingdan.setOnClickListener {
            viewModel?.jiaruheimingdan(
                this@HaoYouSetBroadActivity,
                uid,
                if (lockSwitch3.isChecked) "0" else "1"
            )
            if (!HttpsBroadConfig.useSelfImOnly) {
                ChatManager.Instance().setBlackList(
                    uid,
                    lockSwitch3.isChecked != true,
                    object : GeneralCallback {
                        override fun onSuccess() {
                            AppBroadUtils.debug("野火", "拉黑成功--->")
                        }

                        override fun onFail(errorCode: Int) {
                            AppBroadUtils.debug("野火", "拉黑失败--->$errorCode")
                        }
                    }
                )
            }
        }

        miandarao.setOnClickListener {
            val targetMute = lockSwitch2.isChecked != true
            setSelfImSessionMute(targetMute)
        }

        zidongfanyi.setOnClickListener {
            viewModel?.setFanyi(
                this@HaoYouSetBroadActivity,
                uid,
                if (lockSwitch4.isChecked) "0" else "1"
            )
        }
        viewModel?.imUser(this, uid)
    }

    private fun resolveP2pSessionId(): String {
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        return ImUtils.getP2pSessionIdId(selfId, uid)
    }

    /** 自研 IM：api/im/conversation/sessionTop */
    private fun setSelfImSessionTop(top: Boolean) {
        val sessionId = resolveP2pSessionId()
        if (TextUtils.isEmpty(sessionId)) {
            return
        }
        ImConversationHttpExample.requestSessionTop(
            sessionId,
            top,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    ImLocalStore.updateSessionTopMute(sessionId, top, lockSwitch2.isChecked)
                    runOnUiThread {
                        lockSwitch.isChecked = top
                        // 同步业务侧 isZd，便于 imUser 回填一致
//                        viewModel?.zhiding(
//                            this@HaoYouSetBroadActivity,
//                            uid,
//                            if (top) "1" else "0"
//                        )
                    }
                }
            },
            {
                AppBroadUtils.debug("自研IM", "置顶失败 sessionId=$sessionId")
            }
        )
    }

    /** 自研 IM：api/im/conversation/muteNotify */
    private fun setSelfImSessionMute(mute: Boolean) {
        val sessionId = resolveP2pSessionId()
        if (TextUtils.isEmpty(sessionId)) {
            return
        }
        ImConversationHttpExample.requestMuteNotify(
            sessionId,
            mute,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    ImLocalStore.updateSessionTopMute(sessionId, lockSwitch.isChecked, mute)
                    runOnUiThread {
                        lockSwitch2.isChecked = mute
//                        viewModel?.miandarao(
//                            this@HaoYouSetBroadActivity,
//                            uid,
//                            if (mute) "1" else "0"
//                        )
                    }
                }
            },
            {
                AppBroadUtils.debug("自研IM", "免打扰失败 sessionId=$sessionId")
            }
        )
    }
}
