package com.cboda.intelligent.activity.im

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.SquareUserActivity
import com.cboda.intelligent.adapter.PengYouQuanBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.AppBroadUtils.Companion.goChatP2p
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.freddy.chat.MessageListActivity
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.utils.WeChatUiHelper
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.freddy.webtrc.utils.BroadE

/**
 * 自研群成员资料页（对齐野火 [com.cboda.intelligent.activity.QunGerenPageBroadActivity]）。
 * 入参仅 [qunid]、[uid]；角色与好友态页内拉取。
 */
class ImQunGerenPageBroadActivity : BaseSimple2BroadActivity() {

    private var qunId = ""
    private var uid = ""
    private var myRole = GroupRole.MEMBER
    private var targetRole = GroupRole.MEMBER
    private var member: GroupUserVo? = null
    private var isFriend = false
    /** 群详情 busiExt.openQysl=="1"：禁止群内加好友（仅非好友时生效） */
    private var forbidAddFriendInGroup = false
    private var avatarUrl = ""
    private var displayName = ""

    private var commViewModel: CommViewBroadModel? = null
    private var pengYouQuanAdapter: PengYouQuanBroadAdapter? = null
    private val pengYouQuanDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()

    private lateinit var yichubenqun: View
    private lateinit var shezhijinyan: View
    private lateinit var tianjiadaotongxun: TextView
    private lateinit var mLlOpenFri: View
    private lateinit var mViewJump: View
    private lateinit var tvname: TextView
    private lateinit var tvnicheng: TextView
    private lateinit var ivxingbie: ImageView
    private lateinit var ivTouxiang: VpGradeView
    private lateinit var mTvJoinGroupDay: TextView
    private lateinit var mTvManageDes: TextView
    private lateinit var mViewSetManger: View
    private lateinit var mTvMangerDes: TextView
    private lateinit var mScChangeManger: SwitchCompat
    private lateinit var changeMangerView: View
    private lateinit var pengyouquanlay: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var mViewFriendActions: View
    private lateinit var mViewChat: View
    private lateinit var mViewCall: View
    private lateinit var mViewMore: View
    private var managerRequestInFlight = false

    override fun getLayoutId(): Int = R.layout.activity_qun_ge_broad_ren_page

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getToolbarTitle()?.text="成员资料"

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
        shezhijinyan = findViewById(R.id.shezhijinyan)
        tianjiadaotongxun = findViewById(R.id.tianjiadaotongxun)
        mLlOpenFri = findViewById(R.id.mLlOpenFri)
        mViewJump = findViewById(R.id.mViewJump)
        mTvJoinGroupDay = findViewById(R.id.mTvJoinGroupDay)
        mTvManageDes = findViewById(R.id.mTvManageDes)
        mViewSetManger = findViewById(R.id.mViewSetManger)
        mTvMangerDes = findViewById(R.id.mTvMangerDes)
        mScChangeManger = findViewById(R.id.mScChangeManger)
        changeMangerView = findViewById(R.id.changeMangerView)
        pengyouquanlay = findViewById(R.id.pengyouquanlay)
        recyclerView = findViewById(R.id.recyclerView)
        mViewFriendActions = findViewById(R.id.mViewFriendActions)
        mViewChat = findViewById(R.id.mViewChat)
        mViewCall = findViewById(R.id.mViewCall)
        mViewMore = findViewById(R.id.mViewMore)
        mScChangeManger.isClickable = false
        changeMangerView.setOnClickListener { onManagerSwitchClicked() }

        pengYouQuanAdapter = PengYouQuanBroadAdapter(R.layout.item_peng_broad_you_quan_layout)
        recyclerView.layoutManager = WrapContentLinearLayoutBroadManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false,
        )
        recyclerView.adapter = pengYouQuanAdapter
        pengYouQuanAdapter?.setList(pengYouQuanDataList)

        commViewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        commViewModel?.imUserLiveData?.observe(this) { bean ->
            bean.data?.apply {
                isFriend = isFri == "1"
//                displayName = bei?.takeIf { it.isNotBlank() } ?: name ?: ""
                tvnicheng.text = "("+StringBroadUtils.format(getString(R.string.nichengshi), name ?: "")+")"
                ivxingbie.setImageResource(
                    if (sex == "1") R.mipmap.ic_msg_icon_boy else R.mipmap.ic_msg_icon_girl
                )
//                mTvGrade.text="${this.grade}分"
                pengyouquanlay.isVisible = (isOpenQuan == "1")

                applyActionVisibility()
            }
            bean.data?.quan?.apply {
                pengYouQuanDataList.clear()
                forEach { url ->
                    pengYouQuanDataList.add(CommonDataListBroadBean.CommonBean().apply { icon = url })
                }
                pengYouQuanAdapter?.setList(pengYouQuanDataList)
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
                "1",
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
        mViewJump.setOnClickListener {
            SquareUserActivity.start(this, uid)
        }

        mViewChat.setOnClickListener { startP2pChat() }
        mViewCall.setOnClickListener {
            BroadE.startCall(uid, false, context = this)
        }
        mViewMore.setOnClickListener { openFriendMoreSettings() }

        loadSpecifyMembers()
        loadUserProfile()
        loadGroupDetail()
    }

    /** 拉群详情：读取 busiExt.openQysl，用于非好友时的加好友限制。 */
    private fun loadGroupDetail() {
        ImGroupHttpExample.requestGetGroupDetail(
            qunId,
            object : OnHttpImListener<GroupVo> {
                override fun onHttpImSuccess(result: GroupVo) {
                    runOnUiThread {
                        forbidAddFriendInGroup = result.busiExt?.openQysl == "1"
                        applyActionVisibility()
                    }
                }
            },
            {
                // 详情失败不影响资料页其它能力，保持默认允许加好友
            },
        )
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
                @SuppressLint("SetTextI18n")
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
                                if (vo.displayName.isNotBlank()) {
                                    displayName = vo.displayName
                                }
                                if (avatarUrl.isEmpty() && !vo.headPortrait.isNullOrEmpty()) {
                                    avatarUrl = vo.headPortrait!!
                                    val registerTime = groupDaysFromCreateTime(member?.registerTime?:0L)
                                    ivTouxiang.setData(avatarUrl,member?.lianghao?.isNotEmpty()?:false,member?.isvip=="1",member?.grade?.toString(),registerTime.toString())
                                }
                                val fromCreateTime = if (groupDaysFromCreateTime(vo.joinTime)==0){
                                    1
                                }else{
                                    groupDaysFromCreateTime(vo.joinTime)
                                }
//                                mTvJoinGroupDay.isVisible =(fromCreateTime>0)
                                mTvJoinGroupDay.text="入群:${fromCreateTime}天"
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

    /** 仅群主可对非群主成员设/撤管理员（对齐群详情「群管理员」入口权限）。 */
    private fun canSetManager(): Boolean {
        if (uid == IMSClientBootstrap.getInstance().currentUserId) return false
        if (uid == MMKVBroadUtils.getRealUserId()) return false
        return myRole == GroupRole.OWNER && targetRole != GroupRole.OWNER
    }

    private fun applyManagerSwitchUi() {
        val isAdmin = targetRole == GroupRole.ADMIN
        mTvMangerDes.setText(
            if (isAdmin) R.string.group_cancel_manager else R.string.group_set_as_manager
        )
        mScChangeManger.isChecked = isAdmin
    }

    private fun applyActionVisibility() {
        val self = uid == IMSClientBootstrap.getInstance().currentUserId
            || uid == MMKVBroadUtils.getRealUserId()
        val manage = canManageTarget()
        val setManager = canSetManager()
        val showFriendActions = !self && isFriend
        // 仅非好友判断 openQysl：为 1 时不可加好友，展示「不可私聊」
        val blockAddFriend = !self && !isFriend && forbidAddFriendInGroup
        shezhijinyan.isVisible = manage
        yichubenqun.isVisible = manage
        mTvManageDes.isVisible = manage || setManager
        mViewSetManger.isVisible = setManager
        mViewFriendActions.isVisible = showFriendActions
        if (setManager) {
            applyManagerSwitchUi()
        }
        tianjiadaotongxun.isVisible = !self && !isFriend && !blockAddFriend
        mLlOpenFri.isVisible = blockAddFriend
    }

    private fun startP2pChat() {
        AppBroadUtils.activityList.forEach { activity ->
            if (activity is MessageListActivity) {
                activity.finish()
            }
        }
        val chatName = displayName.takeIf { it.isNotBlank() }
            ?: member?.displayName?.takeIf { it.isNotBlank() }
            ?: tvname.text?.toString()?.takeIf { it.isNotBlank() }
        goChatP2p(uid, chatName, this)
    }

    private fun openFriendMoreSettings() {
        JumpBroadUtils.startHaoYouSetActivity(this, uid, displayName)
    }

    private fun onManagerSwitchClicked() {
        if (managerRequestInFlight || !canSetManager()) return
        doSetGroupManager(targetRole != GroupRole.ADMIN)
    }

    private fun doSetGroupManager(setAsManager: Boolean) {
        managerRequestInFlight = true
        ImGroupHttpExample.requestSetGroupManager(
            qunId,
            listOf(uid),
            setAsManager,
            object : OnHttpImListener<String> {
                override fun onHttpImSuccess(result: String) {
                    runOnUiThread {
                        managerRequestInFlight = false
                        targetRole = if (setAsManager) GroupRole.ADMIN else GroupRole.MEMBER
                        member?.role = targetRole
                        applyManagerSwitchUi()
                        ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_apply_action_success))
                    }
                }
            },
            {
                runOnUiThread {
                    managerRequestInFlight = false
                    applyManagerSwitchUi()
                    ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_apply_action_failed))
                }
            },
        )
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

    /**
     * 由 GroupVo/会话 createTime 计算入群天数；无效或不足 1 天返回 null（不展示）。
     */
    private fun groupDaysFromCreateTime(createTime: Long): Int {
        val millis = WeChatUiHelper.normalizeToMillis(createTime)
        if (millis <= 0L) {
            return 0
        }
        val days = (System.currentTimeMillis() - millis) / (24L * 60L * 60L * 1000L)
        if (days <= 0L) {
            return 0
        }
        return days.toInt()
    }

}
