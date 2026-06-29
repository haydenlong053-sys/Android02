package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.freddy.chat.im.MessageProcessor

/** 自研群禁言管理（对齐野火 {@link com.cboda.intelligent.activity.QunJinYanBroadActivity}）。 */
class ImQunJinYanBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var viewModel: ImQunJinYanViewModel? = null
    private lateinit var quanyuanjinyan: SwitchCompat

    override fun getLayoutId(): Int = R.layout.activity_qun_jin_broad_yan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[ImQunJinYanViewModel::class.java]
        getToolbarTitle()?.text = getString(R.string.qunjinyan)

        findViewById<View>(R.id.minganci).isGone = true
        findViewById<View>(R.id.minganciDivider).isGone = true
        findViewById<View>(R.id.zidongjinyanRow).isGone = true
        findViewById<View>(R.id.zidongjinyanDivider).isGone = true

        quanyuanjinyan = findViewById(R.id.quanyuanjinyan)

        viewModel?.allMute?.observe(this) { quanyuanjinyan.isChecked = it == true }

        findViewById<View>(R.id.chengyuanjinyan).setOnClickListener {
            JumpBroadUtils.startImXuanQunJinYanActivity(this, qunId)
        }
        findViewById<View>(R.id.yijinyanmingdan).setOnClickListener {
            JumpBroadUtils.startImQunJinYanMemberActivity(this, qunId)
        }
        findViewById<View>(R.id.quanyuanjinyanView).setOnClickListener {
            val target = !quanyuanjinyan.isChecked
            viewModel?.setAllMute(qunId, target) { ok ->
                if (!ok) {
                    ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                    quanyuanjinyan.isChecked = !target
                } else {
                    //此处逻辑先注释，目前服务器会下发群禁言更新通知
//                    MessageProcessor.getInstance().notifyGroupMuteUpdated(qunId)
                }
            }
        }

        viewModel?.load(qunId)
    }

    override fun onResume() {
        super.onResume()
        viewModel?.load(qunId)
    }
}
