package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.listener.OnHttpImListener

/**
 * 自研群成员资料页（对齐野火 {@link com.cboda.intelligent.activity.QunGerenPageBroadActivity}，精简为踢人）。
 */
class ImQunGerenPageBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var uid = ""
    private var memberUiType = ""
    private var myRole = GroupRole.MEMBER
    private var member: GroupUserVo? = null
    private lateinit var yichubenqun: TextView
    private lateinit var gongnenglay: android.view.View
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang: ImageView

    override fun getLayoutId(): Int = R.layout.activity_qun_ge_broad_ren_page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        memberUiType = intent?.getStringExtra("type") ?: ""
        myRole = intent?.getIntExtra(ImGroupIntent.EXTRA_MY_ROLE, GroupRole.MEMBER) ?: GroupRole.MEMBER
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        yichubenqun = findViewById(R.id.yichubenqun)
        gongnenglay = findViewById(R.id.gongnenglay)
        findViewById<android.view.View>(R.id.shezhijinyan)?.isVisible = false
        findViewById<android.view.View>(R.id.tianjiadaotongxun)?.isVisible = false
        findViewById<android.view.View>(R.id.chehuishifenzhong)?.isVisible = false

        loadMember()
        yichubenqun.setOnClickListener { confirmKick() }
    }

    private fun loadMember() {
        ImGroupHttpExample.requestGetAllMembers(qunId, object : OnHttpImListener<List<GroupUserVo>> {
            override fun onHttpImSuccess(result: List<GroupUserVo>) {
                runOnUiThread {
                    member = result.firstOrNull { it.userId == uid }
                    val m = member ?: return@runOnUiThread
                    tvname.text = m.displayName
                    GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                        this@ImQunGerenPageBroadActivity,
                        m.headPortrait ?: "",
                        ivTouxiang,
                        PixelBroadBroadUtils.dip2px(this@ImQunGerenPageBroadActivity, 10f)
                    )
                    applyKickVisibility(m)
                }
            }
        }, null)
    }

    private fun applyKickVisibility(m: GroupUserVo) {
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        if (uid == currentUserId) {
            gongnenglay.isVisible = false
            return
        }
        val canKick = when (myRole) {
            GroupRole.OWNER -> m.role != GroupRole.OWNER
            GroupRole.ADMIN -> m.role == GroupRole.MEMBER
            else -> false
        }
        gongnenglay.isVisible = canKick
    }

    private fun confirmKick() {
        val name = member?.displayName ?: uid
        CommanConfimBroadHolder(
            this,
            getString(R.string.yichubenqun),
            StringBroadUtils.format(getString(R.string.jiangyonghhuyichuquan, name)),
            getString(R.string.queding),
            getString(R.string.quxiao)
        ).apply {
            callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) doKick()
                }
            }
            show()
        }
    }

    private fun doKick() {
        ImGroupHttpExample.requestKickOut(qunId, listOf(uid), object : OnHttpImListener<String> {
            override fun onHttpImSuccess(result: String) {
                runOnUiThread { finish() }
            }
        }, {
            runOnUiThread {
                ToastBroadUtils.show(getString(R.string.group_kick_failed))
            }
        })
    }
}
