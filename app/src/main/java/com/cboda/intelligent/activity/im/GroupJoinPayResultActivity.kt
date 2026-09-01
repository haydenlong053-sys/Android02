package com.cboda.intelligent.activity.im

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.im.GroupJoinPayActivity.Companion.EXTRA_GROUP_QR_INFO
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.widge.CustomClickBroadView
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.util.groupToSession
import com.freddy.webtrc.utils.BroadE.checkPeopleInGroup

/**
 * 收费进群：支付成功 / 验证入群资格中。
 *
 */
class GroupJoinPayResultActivity : BaseSimple2BroadActivity() {

    private var orderBean: PayLsOrderBean? = null

    private lateinit var returnView: View

    override fun getLayoutId(): Int = R.layout.activity_group_join_pay_result

    override fun showOrHideTitleLayout(): Boolean = false

    override fun fitsSystemWindows(): Boolean = true

    override fun useStatusBarDarkFont(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderBean = readOrder(intent)
        bindViews()
        bindClicks()

    }

    override fun onResume() {
        super.onResume()
        stopRun()
        startRun()
    }

    override fun onPause() {
        super.onPause()
        stopRun()
    }

    private fun stopRun() {
        ThreadUtils.getMainHandler().removeCallbacks(run)
    }

    private fun startRun() {
        orderBean?.joinGroupBean?.groupId?.let { ThreadUtils.getMainHandler().postDelayed(run, 3000) }
    }

    private val run = Runnable {
        stopRun()
        checkInGroup()
    }


    private fun checkInGroup() {
        orderBean?.joinGroupBean?.groupId?.let { groupId ->
            checkPeopleInGroup(groupId, MMKVBroadUtils.getRealUserId()) { pair ->
                if (pair.first) {
                    val vo = GroupVo()
                    vo.id = groupId
                    val session = groupToSession(vo)
                    if (session != null) {
                        JumpBroadUtils.startChatMsgActivity(session, this)
                    }
                    finish()
                } else {
                    startRun()
                }
            }
        }
    }

    private fun bindViews() {
        returnView = findViewById(R.id.returnView)
        addHeight(returnView)
    }

    private fun bindClicks() {
        returnView.click { finish() }
    }

    companion object {
        fun readOrder(intent: Intent?): PayLsOrderBean? {
            if (intent == null) return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_GROUP_QR_INFO, PayLsOrderBean::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_GROUP_QR_INFO) as? PayLsOrderBean
            }
        }
    }
}
