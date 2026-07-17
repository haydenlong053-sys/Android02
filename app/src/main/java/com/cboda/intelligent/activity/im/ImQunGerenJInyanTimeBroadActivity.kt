package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.listener.OnHttpImListener

/**
 * 自研单人禁言时长选择页（UI 对齐 [com.cboda.intelligent.activity.QunGerenJInyanTimeBroadActivity]）。
 */
class ImQunGerenJInyanTimeBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var uid = ""
    /** 1 一天 / 2 一周 / 3 一月 / 4 永久 */
    private var durationType = ""

    private lateinit var tvPhoneLogin: TextView
    private lateinit var yongjiu: FrameLayout
    private lateinit var yongjiuicon: AppCompatImageView
    private lateinit var jinyanyiyueu: FrameLayout
    private lateinit var yueicon: AppCompatImageView
    private lateinit var jinyanyizhou: FrameLayout
    private lateinit var yizhouicon: AppCompatImageView
    private lateinit var jinyanyitian: FrameLayout
    private lateinit var yitianicon: AppCompatImageView

    override fun getLayoutId(): Int = R.layout.activity_she_broad_zhi_jin_yan_time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        if (qunId.isEmpty() || uid.isEmpty()) {
            finish()
            return
        }
        initViews()
        getToolbarTitle()?.text = getString(R.string.xuanshichangjinyanle)
        bindClicks()
    }

    private fun initViews() {
        yitianicon = findViewById(R.id.yitianicon)
        jinyanyitian = findViewById(R.id.jinyanyitian)
        yizhouicon = findViewById(R.id.yizhouicon)
        jinyanyizhou = findViewById(R.id.jinyanyizhou)
        yueicon = findViewById(R.id.yueicon)
        jinyanyiyueu = findViewById(R.id.jinyanyiyueu)
        yongjiuicon = findViewById(R.id.yongjiuicon)
        yongjiu = findViewById(R.id.yongjiu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    private fun bindClicks() {
        jinyanyitian.setOnClickListener {
            durationType = TYPE_DAY
            updateView()
        }
        jinyanyizhou.setOnClickListener {
            durationType = TYPE_WEEK
            updateView()
        }
        jinyanyiyueu.setOnClickListener {
            durationType = TYPE_MONTH
            updateView()
        }
        yongjiu.setOnClickListener {
            durationType = TYPE_FOREVER
            updateView()
        }
        tvPhoneLogin.setOnClickListener {
            if (durationType.isEmpty()) return@setOnClickListener
            submitMute()
        }
    }

    private fun updateView() {
        yitianicon.setImageResource(R.drawable.ic_broad_weixuanz)
        yizhouicon.setImageResource(R.drawable.ic_broad_weixuanz)
        yueicon.setImageResource(R.drawable.ic_broad_weixuanz)
        yongjiuicon.setImageResource(R.drawable.ic_broad_weixuanz)
        when (durationType) {
            TYPE_DAY -> yitianicon.setImageResource(R.drawable.ic_broad_yixuanz)
            TYPE_WEEK -> yizhouicon.setImageResource(R.drawable.ic_broad_yixuanz)
            TYPE_MONTH -> yueicon.setImageResource(R.drawable.ic_broad_yixuanz)
            TYPE_FOREVER -> yongjiuicon.setImageResource(R.drawable.ic_broad_yixuanz)
        }
    }

    private fun submitMute() {
        val durationSec = resolveDurationSec(durationType)
        ImGroupHttpExample.requestSetMembersMute(
            qunId,
            listOf(uid),
            true,
            durationSec,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    runOnUiThread {
                        MessageProcessor.getInstance().notifyGroupMuteUpdated(qunId)
                        ToastBroadUtils.show(getString(R.string.group_apply_action_success))
                        finish()
                    }
                }
            },
            {
                runOnUiThread {
                    ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                }
            }
        )
    }

    /** 永久禁言传 null（与 [ImXuanQunJinYanBroadActivity] 一致）。 */
    private fun resolveDurationSec(type: String): Long? = when (type) {
        TYPE_DAY -> 86_400L
        TYPE_WEEK -> 604_800L
        TYPE_MONTH -> 2_592_000L
        else -> null
    }

    companion object {
        private const val TYPE_DAY = "1"
        private const val TYPE_WEEK = "2"
        private const val TYPE_MONTH = "3"
        private const val TYPE_FOREVER = "4"
    }
}
