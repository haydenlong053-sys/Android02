package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cc.shinichi.library.ImagePreview
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.listener.OnHttpImListener

/**
 * 自研群成员资料页（对齐野火 [com.cboda.intelligent.activity.QunGerenPageBroadActivity]）。
 * 入参仅 [qunid]、[uid]；角色与好友态页内拉取。
 */
class ImQunGerenPageBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var uid = ""
    private var myRole = GroupRole.MEMBER
    private var targetRole = GroupRole.MEMBER
    private var member: GroupUserVo? = null
    private var isFriend = false
    private var avatarUrl = ""

    private var commViewModel: CommViewBroadModel? = null

    private lateinit var yichubenqun: TextView
    private lateinit var gongnenglay: LinearLayoutCompat
    private lateinit var shezhijinyan: LinearLayoutCompat
    private lateinit var tianjiadaotongxun: TextView
    private lateinit var tvname: TextView
    private lateinit var tvnicheng: TextView
    private lateinit var ivxingbie: ImageView
    private lateinit var ivTouxiang: ImageView

    override fun getLayoutId(): Int = R.layout.activity_qun_ge_broad_ren_page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""
        if (qunId.isEmpty() || uid.isEmpty()) {
            finish()
            return
        }

        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        tvnicheng = findViewById(R.id.tvnicheng)
        ivxingbie = findViewById(R.id.ivxingbie)
        yichubenqun = findViewById(R.id.yichubenqun)
        gongnenglay = findViewById(R.id.gongnenglay)
        shezhijinyan = findViewById(R.id.shezhijinyan)
        tianjiadaotongxun = findViewById(R.id.tianjiadaotongxun)
        findViewById<android.view.View>(R.id.chehuishifenzhong)?.isVisible = false

        commViewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        commViewModel?.imUserLiveData?.observe(this) { bean ->
            bean.data?.apply {
                isFriend = isFri == "1"
                tvnicheng.text = StringBroadUtils.format(getString(R.string.nichengshi), name ?: "")
                ivxingbie.setImageResource(
                    if (sex == "1") R.mipmap.ic_broad_nan else R.mipmap.ic_broad_nv
                )
                applyActionVisibility()
            }
        }

        yichubenqun.setOnClickListener { confirmKick() }
        shezhijinyan.setOnClickListener {
            JumpBroadUtils.startImQunGerenJInyanTimeActivity(this, qunId, uid)
        }
        tianjiadaotongxun.setOnClickListener {
            JumpBroadUtils.startHaoyouApplyActivity(
                this,
                uid,
                member?.displayName ?: tvname.text?.toString() ?: "",
                "",
                qunId,
                "1"
            )
        }
        ivTouxiang.setOnClickListener {
            if (avatarUrl.isEmpty()) return@setOnClickListener
            ImagePreview.getInstance()
                .setContext(this)
                .setIndex(0)
                .setImage(avatarUrl)
                .start()
        }

        loadSpecifyMembers()
        loadUserProfile()
    }

    /** 一次拉取自己 + 目标，用于角色与踢人/禁言权限。 */
    private fun loadSpecifyMembers() {
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        val ids = linkedSetOf<String>()
        if (!currentUserId.isNullOrEmpty()) ids.add(currentUserId)
        ids.add(uid)
        ImGroupHttpExample.requestGetSpecifyMembers(
            qunId,
            ids.toList(),
            object : OnHttpImListener<List<GroupUserVo>> {
                override fun onHttpImSuccess(result: List<GroupUserVo>) {
                    runOnUiThread {
                        val selfId = currentUserId
                        for (vo in result) {
                            val id = vo.userId ?: continue
                            if (id == uid) {
                                member = vo
                                targetRole = vo.role
                                if (tvname.text.isNullOrEmpty()) {
                                    tvname.text = vo.displayName
                                }
                                if (avatarUrl.isEmpty() && !vo.headPortrait.isNullOrEmpty()) {
                                    avatarUrl = vo.headPortrait!!
                                    ivTouxiang.loadImg(avatarUrl)
//                                    GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
//                                        this@ImQunGerenPageBroadActivity,
//                                        avatarUrl,
//                                        ivTouxiang,
//                                        PixelBroadBroadUtils.dip2px(
//                                            this@ImQunGerenPageBroadActivity,
//                                            10f
//                                        )
//                                    )
                                }
                            }
                            if (selfId != null && id == selfId) {
                                myRole = vo.role
                            }
                        }
                        applyActionVisibility()
                    }
                }
            },
            {
                runOnUiThread {
                    ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_detail_load_failed))
                }
            }
        )
    }

    private fun loadUserProfile() {
        commViewModel?.imUser(this, uid)
    }

    private fun canManageTarget(): Boolean {
        if (uid == IMSClientBootstrap.getInstance().currentUserId) return false
        return when (myRole) {
            GroupRole.OWNER -> targetRole != GroupRole.OWNER
            GroupRole.ADMIN -> targetRole == GroupRole.MEMBER
            else -> false
        }
    }

    private fun applyActionVisibility() {
        val self = uid == IMSClientBootstrap.getInstance().currentUserId
            || uid == MMKVBroadUtils.getRealUserId()
        val manage = canManageTarget()
        shezhijinyan.isVisible = manage
        gongnenglay.isVisible = manage
        tianjiadaotongxun.isVisible = !self && !isFriend
    }

    private fun confirmKick() {
        val name = member?.displayName?.takeIf { it.isNotBlank() }
            ?: tvname.text?.toString()?.takeIf { it.isNotBlank() }
            ?: uid
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
                ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_kick_failed))
            }
        })
    }
}
