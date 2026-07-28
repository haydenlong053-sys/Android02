package com.cboda.intelligent.activity.im

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils
import com.cboda.baselibrary.common.MyEventCodeEnum
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.APP_IM_TAG
import com.cboda.baselibrary.utils.CompressionBroadUtil.imgCompression
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.groupDataLiveData
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.showAbsentKickTip
import com.cboda.chatui.bridge.MyBroadEvent
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter2
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.holder.BottomDialogBroadHolder
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.holder.ImagePickerBroadHolder
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.UploadFileViewBroadModel
import com.cboda.select.picture.entity.LocalMedia
import com.freddy.chat.MessageListActivity
import com.freddy.chat.clear.SessionMessageClearHelper
import com.freddy.chat.event.CEventCenter
import com.freddy.chat.event.Events
import com.freddy.chat.event.I_CEventListener
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.utils.AvatarViews
import com.freddy.im.db.ImDbExecutor
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.http.model.im.GroupBusiExt
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.groupDataLiveData2
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

/**
 * 自研 IM 群详情（UI 对齐野火 {@link com.cboda.intelligent.activity.QunInfo2BroadActivity}）。
 */
class ImQunInfo2BroadActivity : BaseSimpleBroadActivity(), I_CEventListener {

    companion object {
        private val GROUP_CHANGE_EVENTS = arrayOf(
            Events.SESSION_LIST_UPDATED,
            Events.GROUP_MEMBERS_ROLE_UPDATE,
        )
    }

    private var qunId = ""
    private var currentUserId: String? = null
    private val contentList = arrayListOf<String>()
    private var mAdapter: QunUserBroadAdapter2? = null
    private var allMembers: List<GroupUserVo> = emptyList()
    private var myRole: Int = GroupRole.MEMBER
    private var groupVo: GroupVo? = null

    /** 首次 onResume 跳过成员刷新，避免与 onCreate 中 load() 重复请求 getAllMembers。 */
    private var skipMembersRefreshOnResume = true

    private val selectNewOwnerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // onResume 已因 skip 跳过 refreshMembers；此处 load 含一次 memberList
            viewModel?.load(qunId, currentUserId)
        }
    }

    private var viewModel: ImQunInfoViewModel? = null
    private var uploadViewModel: UploadFileViewBroadModel? = null

    private lateinit var tuichuquan: TextView
    private lateinit var jiesanqun: TextView
    private lateinit var qunguanlilay: LinearLayoutCompat
    private lateinit var zhuanrangqun: LinearLayoutCompat
    private lateinit var zhuanrangxian: View
    private lateinit var wodequnnicheng: LinearLayoutCompat
    private lateinit var wodequnbnicjheng: TextView
    private lateinit var qingkong: LinearLayoutCompat
    private lateinit var qungonggao: LinearLayoutCompat
    private lateinit var mTvNoticeSet: TextView
    private lateinit var mTvNoticeDes: TextView
    private lateinit var zidongfanyiView: View
    private lateinit var zidongfanyi: SwitchCompat
    private lateinit var miaodaraoView: View
    private lateinit var miaodarao: SwitchCompat
    private lateinit var zhidingView: View
    private lateinit var zhiding: SwitchCompat
    private lateinit var mIvGroupQrcode: View
    private lateinit var qunid: TextView
    private lateinit var shezhiqunnicheng: LinearLayoutCompat
    private lateinit var qunmingceng: TextView
    private lateinit var mTvGroupName: TextView
    private lateinit var shezhiquntou: LinearLayoutCompat
    private lateinit var mIvGroupAvatar: ImageView
    private lateinit var chakangebngduo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var commercialFunctionLayout: View
    private lateinit var commercialFunctionSwitch: SwitchCompat
    private lateinit var mScAbsentKickView: View
    private lateinit var mScAbsentKick: SwitchCompat
    private lateinit var mTvLookRule: TextView
    private lateinit var mTvRuleDes: TextView
    private lateinit var yaoqinghaioyu: SwitchCompat
    private lateinit var yaoqinghaioyuView: View
    private lateinit var jinqunjiaoyan: SwitchCompat
    private lateinit var jinqunjiaoyanView: View
    private lateinit var qunzhumingpian: SwitchCompat
    private lateinit var qunzhumingpianView: View
    private lateinit var ruqunyanz: SwitchCompat
    private lateinit var ruqunyanzView: View
    private lateinit var wufajiahaoyou: SwitchCompat
    private lateinit var wufajiahaoyouView: View
    private lateinit var kejianguanli: SwitchCompat
    private lateinit var kejianguanliView: View
    private lateinit var jianhao: AppCompatImageView
    private lateinit var jiahao: AppCompatImageView
    private lateinit var tvjiangeshi: EditText
    private var jiangetime = 0
    private var suppressSpeakIntervalWatcher = false

    override fun getLayoutId(): Int = R.layout.activity_qun_broad_info2

    override fun showOrHideTitleLayout(): Boolean = false

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        chakangebngduo = findViewById(R.id.chakangebngduo)
        mIvGroupAvatar = findViewById(R.id.mIvGroupAvatar)
        shezhiquntou = findViewById(R.id.shezhiquntou)
        qunmingceng = findViewById(R.id.qunmingceng)
        mTvGroupName = findViewById(R.id.mTvGroupName)
        shezhiqunnicheng = findViewById(R.id.shezhiqunnicheng)
        qunid = findViewById(R.id.qunid)
        mIvGroupQrcode = findViewById(R.id.mIvGroupQrcode)
        zhiding = findViewById(R.id.zhiding)
        zhidingView = findViewById(R.id.zhidingView)
        // 仅通过覆盖层触发接口，避免 Switch 先本地翻转导致状态错乱
        zhiding.isClickable = false
        zhiding.isFocusable = false
        miaodarao = findViewById(R.id.miaodarao)
        miaodaraoView = findViewById(R.id.miaodaraoView)
        miaodarao.isClickable = false
        miaodarao.isFocusable = false
        zidongfanyi = findViewById(R.id.zidongfanyi)
        zidongfanyiView = findViewById(R.id.zidongfanyiView)
        qungonggao = findViewById(R.id.qungonggao)
        mTvNoticeSet = findViewById(R.id.mTvNoticeSet)
        mTvNoticeDes = findViewById(R.id.mTvNoticeDes)
        qingkong = findViewById(R.id.qingkong)
        wodequnbnicjheng = findViewById(R.id.wodequnbnicjheng)
        wodequnnicheng = findViewById(R.id.wodequnnicheng)
        zhuanrangqun = findViewById(R.id.zhuanrangqun)
        zhuanrangxian = findViewById(R.id.zhuanrangxian)
        qunguanlilay = findViewById(R.id.qunguanlilay)
        jiesanqun = findViewById(R.id.jiesanqun)
        tuichuquan = findViewById(R.id.tuichuquan)
        commercialFunctionLayout = findViewById(R.id.commercialFunctionLayout)
        commercialFunctionSwitch = findViewById(R.id.commercialFunctionSwitch)
        mScAbsentKick = findViewById(R.id.mScAbsentKick)
        mScAbsentKickView = findViewById(R.id.mScAbsentKickView)
        mTvRuleDes = findViewById(R.id.mTvRuleDes)
        mTvLookRule = findViewById(R.id.mTvLookRule)
        yaoqinghaioyu = findViewById(R.id.yaoqinghaioyu)
        yaoqinghaioyuView = findViewById(R.id.yaoqinghaioyuView)
        yaoqinghaioyu.isClickable = false
        yaoqinghaioyu.isFocusable = false
        jinqunjiaoyan = findViewById(R.id.jinqunjiaoyan)
        jinqunjiaoyanView = findViewById(R.id.jinqunjiaoyanView)
        jinqunjiaoyan.isClickable = false
        jinqunjiaoyan.isFocusable = false
        qunzhumingpian = findViewById(R.id.qunzhumingpian)
        qunzhumingpianView = findViewById(R.id.qunzhumingpianView)
        qunzhumingpian.isClickable = false
        qunzhumingpian.isFocusable = false
        ruqunyanz = findViewById(R.id.ruqunyanz)
        ruqunyanzView = findViewById(R.id.ruqunyanzView)
        ruqunyanz.isClickable = false
        ruqunyanz.isFocusable = false
        wufajiahaoyou = findViewById(R.id.wufajiahaoyou)
        wufajiahaoyouView = findViewById(R.id.wufajiahaoyouView)
        wufajiahaoyou.isClickable = false
        wufajiahaoyou.isFocusable = false
        kejianguanli = findViewById(R.id.kejianguanli)
        kejianguanliView = findViewById(R.id.kejianguanliView)
        kejianguanli.isClickable = false
        kejianguanli.isFocusable = false
        jianhao = findViewById(R.id.jianhao)
        jiahao = findViewById(R.id.jiahao)
        tvjiangeshi = findViewById(R.id.tvjiangeshi)
    }

    private fun hideSwitchRow(anchorViewId: Int, ancestorLevel: Int = 1) {
        var view: View? = findViewById(anchorViewId)
        repeat(ancestorLevel) { view = view?.parent as? View }
        view?.isGone = true
    }

    private fun hideUnsupportedWildfireFeatures() {
        zidongfanyiView.isGone = true
        zidongfanyi.isGone = true
        findViewById<View>(R.id.shengjiqun)?.isGone = HttpsBroadConfig.isReviewEnv
//        findViewById<View>(R.id.shengjiqunView)?.isGone = HttpsBroadConfig.isReviewEnv
        hideSwitchRow(R.id.kefahongboaView)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        hideUnsupportedWildfireFeatures()
        qunId = intent?.getStringExtra("qunid") ?: ImGroupIntent.getGroupId(intent) ?: ""
        currentUserId = IMSClientBootstrap.getInstance().currentUserId
        if (qunId.isEmpty()) {
            finish()
            return
        }

        getTopBgView()?.setImageResource(com.cboda.baselibrary.R.mipmap.ic_bg_top_broad_main2)
        findViewById<View>(com.cboda.baselibrary.R.id.mViewBack).click { finish() }
        findViewById<AppCompatImageView>(com.cboda.baselibrary.R.id.mIvRight)
            .setImageResource(R.mipmap.ic_broad_gengduo)
        findViewById<View>(com.cboda.baselibrary.R.id.mFlView).isVisible = true
        findViewById<View>(com.cboda.baselibrary.R.id.mFlView).click { showComplaintMenu() }

        viewModel = ViewModelProvider(this)[ImQunInfoViewModel::class.java]
        viewModel?.state?.observe(this) { state ->
            if (state.loading) return@observe
            if (state.loadFailed) {
                ToastBroadUtils.show(getString(R.string.group_detail_load_failed))
                return@observe
            }
            applyUiState(state)
        }

        mAdapter = QunUserBroadAdapter2(R.layout.item_qun_user_broad_layout2)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = mAdapter
        mAdapter?.setOnItemClickListener { _, _, position ->
            val items = mAdapter?.data ?: return@setOnItemClickListener
            if (position !in items.indices) return@setOnItemClickListener
            when (items[position].type) {
                "add" -> JumpBroadUtils.startImQunMemberListActivity(
                    this, qunId, "add", ImGroupMemberUiMapper.wildfireUserTypeForRole(myRole)
                )

                "remove" -> JumpBroadUtils.startImQunMemberListActivity(
                    this, qunId, "remove", ImGroupMemberUiMapper.wildfireUserTypeForRole(myRole)
                )

                else -> JumpBroadUtils.startImQunGerenPageActivity(
                    this, qunId, items[position].userId ?: ""
                )
            }
        }

        bindClicks()
        // 会话页 shopIv / 本页开关：监听商业功能变更
        groupDataLiveData2.observe(this) { busi ->
            applyCommercialFunctionEntry(busi)
            busi?.let { ext ->
                groupVo?.ensureBusiExt()?.apply {
                    isshop = ext.isshop
                    showShop = ext.showShop
                    shopLink = ext.shopLink
                }
            }
        }
        viewModel?.load(qunId, currentUserId)
        CEventCenter.registerEventListener(this, GROUP_CHANGE_EVENTS)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun showComplaintMenu() {
        contentList.clear()
        contentList.add(getString(R.string.tousuqun))
        val holder = BottomDialogBroadHolder(this, contentList)
        holder.callBack = object : BottomDialogBroadHolder.BottomDialogClickCallBack {
            override fun clickItem(position: Int) {
                JumpBroadUtils.startQunTouSuActivity(this@ImQunInfo2BroadActivity, qunId, "")
            }
        }
        holder.show()
    }

    @SuppressLint("SetTextI18n")
    private fun applyUiState(state: ImGroupInfoUiState) {
        groupVo = state.groupVo
        allMembers = state.members
        myRole = state.myRole
        val vo = state.groupVo ?: return

        val name = vo.name ?: ""
        mTvGroupName.text = name
        qunmingceng.text = name
        qunid.text = "${getString(com.cboda.baselibrary.R.string.msg_group_id)}${vo.id ?: qunId}"
        mIvGroupAvatar.loadImg(vo.icon)

        findViewById<TextView>(R.id.mTvGroupMemberCount).text =
            getString(com.cboda.baselibrary.R.string.msg_group_members) + "(" + vo.memCount.coerceAtLeast(0) + ")"

        jiesanqun.isVisible = GroupRole.isOwner(myRole)
        tuichuquan.isVisible = !GroupRole.isOwner(myRole)
        qunguanlilay.isVisible = GroupRole.isOwnerOrAdmin(myRole)
        zhuanrangqun.isVisible = GroupRole.isOwner(myRole)
        zhuanrangxian.isVisible = GroupRole.isOwner(myRole)
        findViewById<View>(R.id.qunguanliyuan)?.isVisible = GroupRole.isOwner(myRole)
        findViewById<View>(R.id.guanlixian)?.isVisible = GroupRole.isOwner(myRole)

        val canManage = GroupRole.isOwnerOrAdmin(myRole)
//        shezhiquntou.isEnabled = canManage
//        shezhiqunnicheng.isEnabled = canManage
//        qungonggao.isEnabled = canManage
        findViewById<View>(R.id.qunjinmyan)?.isVisible = canManage

        zhiding.isChecked = vo.busiExt?.isZd == "1"
        miaodarao.isChecked = state.sessionMuted

        if (state.noticeContent.isNullOrEmpty()) {
            mTvNoticeSet.isVisible = true
            mTvNoticeDes.isVisible = false
        } else {
            mTvNoticeSet.isVisible = false
            mTvNoticeDes.isVisible = true
            mTvNoticeDes.text = state.noticeContent
        }
        val isOpenGroupInviteFunc = (vo.allowMemberInvite == 1)
        mAdapter?.setList(ImGroupMemberUiMapper.buildGridItems(state.members, myRole,isOpenGroupInviteFunc))

        mScAbsentKick.isChecked = vo.busiExt?.openAbsentKick == "1"
        val limit = vo.busiExt?.absentKickLimit?.toString()?.takeIf { it.isNotBlank() } ?: "5"
        mTvRuleDes.text = getString(R.string.absentkick_tip, limit)

        refreshMyGroupNickname()
        // 商业功能：群详情 busiExt.isshop 非空才展示入口（对齐野火 QunInfo2BroadActivity）
        applyCommercialFunctionEntry(vo.busiExt)
        // 同步到会话页 shopIv（MessageListActivity 监听 groupDataLiveData2）
        vo.busiExt?.let { groupDataLiveData2.postValue(it) }

        val busi = vo.busiExt
        yaoqinghaioyu.isChecked = busi?.openQyyq == "1" ||
                (busi?.openQyyq.isNullOrEmpty() && vo.allowMemberInvite == 1)

        jinqunjiaoyan.isChecked = busi?.openQyyz == "1" ||
                (busi?.openQyyz.isNullOrEmpty() && vo.joinNeedVerify == 1)
        qunzhumingpian.isChecked = busi?.openQzts == "1"
        ruqunyanz.isChecked = busi?.openQygl == "1" ||
                (busi?.openQygl.isNullOrEmpty() && vo.joinNeedVerify == 1)
        wufajiahaoyou.isChecked = busi?.openQysl == "1"
        kejianguanli.isChecked = busi?.openQykj == "1"
        jiangetime = (busi?.jiange ?: 0).coerceAtLeast(0)
        syncSpeakIntervalInput(jiangetime)
    }

    /** 商业功能开关入口：isshop 为空不展示；有值则展示，1=开 / 0=关。 */
    private fun applyCommercialFunctionEntry(busiExt: GroupBusiExt?) {
        commercialFunctionLayout.isGone = TextUtils.isEmpty(busiExt?.isshop)
        commercialFunctionSwitch.isChecked = anyToInt(busiExt?.isshop) == 1
    }

    private fun bindClicks() {
        chakangebngduo.setOnClickListener {
            JumpBroadUtils.startImQunUserListActivity(
                this, qunId, myRole, groupVo?.busiExt?.openQykj
            )
        }
        mIvGroupQrcode.setOnClickListener {
            JumpBroadUtils.startImQunCodeActivity(this, qunId, groupVo)
        }
        shezhiqunnicheng.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanlimingcheng))
                return@setOnClickListener
            }
            JumpBroadUtils.startImQunNameActivity(this, qunId, qunmingceng.text?.toString() ?: "")
        }
        qungonggao.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanlimgongao))
                return@setOnClickListener
            }
            JumpBroadUtils.startImQunGongGaoActivity(this, qunId, mTvNoticeDes.text?.toString() ?: "")
        }
        shezhiquntou.setOnClickListener { pickAndUploadAvatar() }
        // 置顶 / 取消置顶：api/im/conversation/sessionTop（开关态以 busiExt.isZd 为准）
        zhidingView.setOnClickListener {
            val target = !zhiding.isChecked
            viewModel?.setSessionTop(qunId, target) { ok ->
                if (ok) {
                    zhiding.isChecked = target
                    groupVo?.ensureBusiExt()?.isZd = if (target) "1" else "0"
                } else {
                    ToastBroadUtils.show(getString(R.string.group_rename_failed))
                }
            }
        }
        // 免打扰 / 取消免打扰：api/im/conversation/muteNotify
        miaodaraoView.setOnClickListener {
            val target = !miaodarao.isChecked
            viewModel?.setSessionMute(qunId, target) { ok ->
                if (ok) {
                    miaodarao.isChecked = target
                } else {
                    ToastBroadUtils.show(getString(R.string.group_rename_failed))
                }
            }
        }
        jiesanqun.setOnClickListener { confirmDisband() }
        tuichuquan.setOnClickListener { confirmLeave() }
        qingkong.setOnClickListener { confirmClearChat() }
        zhuanrangqun.setOnClickListener { openTransferOwner() }
        findViewById<View>(R.id.qunjinmyan)?.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
                return@setOnClickListener
            }
            JumpBroadUtils.startImQunJinYanActivity(this, qunId)
        }
        findViewById<View>(R.id.qunguanliyuan)?.setOnClickListener {
            if (!GroupRole.isOwner(myRole)) {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
                return@setOnClickListener
            }
            JumpBroadUtils.startImQunManagerActivity(this, qunId)
        }
        findViewById<View>(R.id.jiqiren)?.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
                return@setOnClickListener
            }
            JumpBroadUtils.startJiQiRenActivity(this, qunId)
        }
        findViewById<View>(R.id.huanyingchengyuan)?.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
                return@setOnClickListener
            }
            JumpBroadUtils.startHyyActivity(this, qunId)
        }
        findViewById<View>(R.id.shengjiqun)?.setOnClickListener {
            JumpBroadUtils.startQunShengJiActivity(this, qunId)
        }
        mScAbsentKickView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            if (!mScAbsentKick.isChecked) {
                showAbsentKickTip(this) { goAbsentKick() }
            } else {
                goAbsentKick()
            }
        }
        mTvLookRule.setOnClickListener {
            JumpBroadUtils.startAbsentKickRulesActivity(this)
        }
        wodequnnicheng.setOnClickListener {
            JumpBroadUtils.startImQunNichengActivity(
                this,
                qunId,
                wodequnbnicjheng.text?.toString() ?: ""
            )
        }
        commercialFunctionSwitch.click {
            val checked = commercialFunctionSwitch.isChecked
            lifecycleScope.launch {
                viewModel?.setCommercialFunction(qunId, checked)
                    ?.observe(this@ImQunInfo2BroadActivity) { enabled ->
                        commercialFunctionSwitch.isChecked = enabled
                        val flag = if (enabled) "1" else "0"
                        groupVo?.ensureBusiExt()?.let {
                            it.isshop = flag
                            it.showShop = flag
                            groupDataLiveData2.postValue(it)
                        }
                    }
            }
        }
        // 群员邀请好友进群：api/im/group/modify（allowMemberInvite 1=允许 / 0=不允许）
        yaoqinghaioyuView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            val targetAllow = if (yaoqinghaioyu.isChecked) 0 else 1
            ImGroupHttpExample.requestModifyGroup(
                qunId,
                null,
                null,
                targetAllow,
                object : OnHttpImListener<GroupVo> {
                    override fun onHttpImSuccess(result: GroupVo) {
                        runOnUiThread {
                            yaoqinghaioyu.isChecked = targetAllow == 1
                            val flag = if (targetAllow == 1) "1" else "0"
                            groupVo?.allowMemberInvite = anyToInt(flag)
                            groupVo?.ensureBusiExt()?.openQyyq = flag
                        }
                    }
                },
                {
                    runOnUiThread {
                        ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                    }
                },
            )
        }
        jinqunjiaoyanView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            toggleGroupManageSwitch("openQyyz", jinqunjiaoyan)
        }
        qunzhumingpianView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            toggleGroupManageSwitch("openQzts", qunzhumingpian)
        }
        // 群员入群邀请管理验证：api/im/group/modify（joinNeedVerify 1=允许 / 0=不允许）
        ruqunyanzView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            val targetVerify = if (ruqunyanz.isChecked) 0 else 1
            ImGroupHttpExample.requestModifyGroup(
                qunId,
                null,
                null,
                null,
                targetVerify,
                object : OnHttpImListener<GroupVo> {
                    override fun onHttpImSuccess(result: GroupVo) {
                        runOnUiThread {
                            ruqunyanz.isChecked = targetVerify == 1
                            val flag = if (targetVerify == 1) "1" else "0"
                            groupVo?.joinNeedVerify = anyToInt(flag)
                            groupVo?.ensureBusiExt()?.openQygl = flag
                        }
                    }
                },
                {
                    runOnUiThread {
                        ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                    }
                },
            )
        }
        wufajiahaoyouView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            toggleGroupManageSwitch("openQysl", wufajiahaoyou)
        }
        kejianguanliView.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            toggleGroupManageSwitch("openQykj", kejianguanli)
        }
        // 间隔发言（秒）：每次加减/输入变更立即生效，不依赖失焦或确定
        jianhao.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            submitSpeakInterval((jiangetime - 1).coerceAtLeast(0))
        }
        jiahao.setOnClickListener {
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@setOnClickListener
            submitSpeakInterval(jiangetime + 1)
        }
        tvjiangeshi.doAfterTextChanged { editable ->
            if (suppressSpeakIntervalWatcher) return@doAfterTextChanged
            if (!GroupRole.isOwnerOrAdmin(myRole)) return@doAfterTextChanged
            val raw = editable?.toString()?.trim().orEmpty()
            if (raw.isEmpty()) return@doAfterTextChanged
            val inputVal = raw.toIntOrNull() ?: return@doAfterTextChanged
            submitSpeakInterval(inputVal.coerceAtLeast(0), updateInput = false)
        }
        tvjiangeshi.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE
            val isEnterDown =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (!isDoneAction && !isEnterDown) {
                return@setOnEditorActionListener false
            }
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(tvjiangeshi.windowToken, 0)
            true
        }
    }

    /** 翻转群管理开关并调用 {@code api/editGroup}。 */
    private fun toggleGroupManageSwitch(key: String, switchCompat: SwitchCompat) {
        val target = if (switchCompat.isChecked) "0" else "1"
        lifecycleScope.launch {
            viewModel?.editGroupField(qunId, key, target)
                ?.observe(this@ImQunInfo2BroadActivity) { newValue ->
                    switchCompat.isChecked = newValue == "1"
                    groupVo?.let { ImQunInfoViewModel.applyEditGroupField(it, key, newValue) }
                }
        }
    }

    /**
     * 提交间隔发言秒数：本地立即生效并通知聊天页，再静默同步服务端。
     * @param updateInput 加减按钮需要回写输入框；TextWatcher 触发时传 false 避免循环。
     */
    private fun submitSpeakInterval(seconds: Int, updateInput: Boolean = true) {
        val next = seconds.coerceAtLeast(0)
        if (next == jiangetime && (groupVo?.busiExt?.jiange ?: -1) == next) {
            if (updateInput) {
                syncSpeakIntervalInput(next)
            }
            return
        }
        jiangetime = next
        if (updateInput) {
            syncSpeakIntervalInput(next)
        }
        val ext = groupVo?.ensureBusiExt() ?: GroupBusiExt()
        ext.jiange = jiangetime
        if (groupVo != null) {
            groupVo?.busiExt = ext
        }
        groupDataLiveData2.postValue(ext)
        lifecycleScope.launch {
            viewModel?.editGroupField(qunId, "jiange", jiangetime.toString(), showLoading = false)
                ?.observe(this@ImQunInfo2BroadActivity) { newValue ->
                    jiangetime = newValue.toIntOrNull()?.coerceAtLeast(0) ?: jiangetime
                    groupVo?.ensureBusiExt()?.jiange = jiangetime
                    groupVo?.busiExt?.let { groupDataLiveData2.postValue(it) }
                }
        }
    }

    private fun syncSpeakIntervalInput(seconds: Int) {
        val text = seconds.toString()
        if (tvjiangeshi.text?.toString() == text) {
            return
        }
        suppressSpeakIntervalWatcher = true
        tvjiangeshi.setText(text)
        tvjiangeshi.setSelection(text.length)
        suppressSpeakIntervalWatcher = false
    }

    /** 展示当前用户群昵称：本地 [ImLocalStore.queryGroupMember]。 */
    private fun refreshMyGroupNickname() {
        val uid = currentUserId
        if (uid.isNullOrEmpty()) {
            wodequnbnicjheng.text = ""
            return
        }
        ImDbExecutor.runRead {
            val member = ImLocalStore.queryGroupMember(qunId, uid)
            runOnUiThread {
                wodequnbnicjheng.text = member?.groupNickname?.takeIf { it.isNotBlank() } ?: ""
            }
        }
    }

    private fun goAbsentKick() {
        val state = if (mScAbsentKick.isChecked) "0" else "1"
        lifecycleScope.launch {
            viewModel?.setGroupAbsentKick(qunId, state)?.observe(this@ImQunInfo2BroadActivity) { newState ->
                mScAbsentKick.isChecked = newState == "1"
                groupVo?.ensureBusiExt()?.openAbsentKick = newState
            }
        }
    }

    private fun pickAndUploadAvatar() {
        if (!GroupRole.isOwnerOrAdmin(myRole)) {
            ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
            return
        }
        val holder = ImagePickerBroadHolder(this)
        holder.callBack = object : ImagePickerBroadHolder.OnResultCallback {
            override fun onResult(result: ArrayList<LocalMedia>?) {
                showLoading()
                val path = result?.firstOrNull()?.realPath ?: return
                imgCompression(this@ImQunInfo2BroadActivity, File(path)) { _, compressFile ->
                    compressFile ?: return@imgCompression
                    if (uploadViewModel == null) {
                        uploadViewModel = ViewModelProvider(this@ImQunInfo2BroadActivity)[UploadFileViewBroadModel::class.java]
                    }
                    MainScope().launch {
                        val urlBeans = uploadViewModel?.uploadSyn(compressFile)
                        val url = urlBeans?.url
                        if (url.isNullOrEmpty()) {
                            ToastBroadUtils.show(urlBeans?.erroToaastInfo ?: "")
                            return@launch
                        }
                        ImGroupHttpExample.requestModifyGroup(qunId, null, url, object : OnHttpImListener<GroupVo> {
                            override fun onHttpImSuccess(result: GroupVo) {
                                runOnUiThread {
//                                    viewModel?.load(qunId, currentUserId)
                                    dismissLoading()
                                    ToastBroadUtils.show(getString(R.string.group_apply_action_success))
                                }
                            }
                        }, {
                            runOnUiThread {
                                ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                            }
                        })
                    }
                }
            }

            override fun onCancel() {}
        }
        holder.show()
    }

    private fun confirmDisband() {
        CommanConfimBroadHolder(
            this, getString(R.string.wenxintishi), getString(R.string.jiesanquan),
            getString(R.string.queding), getString(R.string.quxiao)
        ).apply {
            callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) doDisband()
                }
            }
            show()
        }
    }

    private fun doDisband() {
        ImGroupHttpExample.requestDisbandGroup(qunId, object : OnHttpImListener<BaseMsgResponse<Void>> {
            override fun onHttpImSuccess(result: BaseMsgResponse<Void>) {
                onGroupRemoved()
            }
        }, {
            ToastBroadUtils.show(getString(R.string.group_disband_failed))
        })
    }

    private fun confirmLeave() {
        CommanConfimBroadHolder(
            this, getString(R.string.wenxintishi), getString(R.string.querdingjiesanqunshanchu),
            getString(R.string.queding), getString(R.string.quxiao)
        ).apply {
            callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) doLeave()
                }
            }
            show()
        }
    }

    private fun doLeave() {
        ImGroupHttpExample.requestLeaveGroup(qunId, object : OnHttpImListener<String> {
            override fun onHttpImSuccess(result: String) {
                onGroupRemoved()
            }
        }, {
            ToastBroadUtils.show(getString(R.string.group_leave_failed))
        })
    }

    private fun onGroupRemoved() {
        ImLocalStore.removeSessionCompletely(qunId) {
            runOnUiThread {
                ActivityUtils.finishActivity(MessageListActivity::class.java)
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun confirmClearChat() {
        CommanConfimBroadHolder(
            this,
            getString(R.string.qingkongliaotian),
            StringBroadUtils.format(getString(R.string.qingkongquntishi, groupVo?.name ?: "")),
            getString(R.string.queding),
            getString(R.string.quxiao)
        ).apply {
            callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        SessionMessageClearHelper.clearSessionMessages(qunId) {
                            ToastBroadUtils.show(getString(R.string.qincghujikusuc))
                        }
                    }
                }
            }
            show()
        }
    }

    private fun openTransferOwner() {
        if (!GroupRole.isOwner(myRole)) {
            ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
            return
        }
        // 返回时 onResume 常早于 ActivityResult；先置位避免与 load() 重复打 memberList
        skipMembersRefreshOnResume = true
        val intent = Intent(this, ImQunMemberListBroadActivity::class.java).apply {
            putExtra("qunid", qunId)
            putExtra("fromPage", "selectNewOwner")
            putExtra("userType", ImGroupMemberUiMapper.wildfireUserTypeForRole(myRole))
        }
        selectNewOwnerLauncher.launch(intent)
    }

    private fun applyGroupSessionUpdate(update: SessionItem) {
        val name = update.sessionName?.takeIf { it.isNotEmpty() } ?: return
        mTvGroupName.text = name
        qunmingceng.text = name
        groupVo?.setName(name)
        update.icon?.takeIf { it.isNotEmpty() }?.let { icon ->
            mIvGroupAvatar.loadImg(icon)
            groupVo?.setIcon(icon)
        }
        if (update.memCount > 0) {
            findViewById<TextView>(R.id.mTvGroupMemberCount).text =
                getString(com.cboda.baselibrary.R.string.msg_group_members) + "(" + update.memCount + ")"
            groupVo?.setMemCount(update.memCount)
        }
    }

    override fun onCEvent(topic: String?, msgCode: Int, resultCode: Int, obj: Any?) {
        if (topic == Events.GROUP_MEMBERS_ROLE_UPDATE && obj is String && obj == qunId) {
            runOnUiThread { viewModel?.refreshMembersFromLocal(qunId, currentUserId) }
            return
        }
        if (topic != Events.SESSION_LIST_UPDATED) return
        @Suppress("UNCHECKED_CAST")
        val updates = obj as? List<SessionItem> ?: return
        val update = updates.firstOrNull { it.sessionId == qunId } ?: return
        runOnUiThread {
            applyGroupSessionUpdate(update)
            viewModel?.refreshMembersFromLocal(qunId, currentUserId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnouncementUpdated(event: MyBroadEvent) {
        if (event.type != MyEventCodeEnum.UPDATE_ANNOUNCEMENT.value) return
        if (event.params2 != qunId) return
        viewModel?.updateNoticeContent(event.params1)
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        CEventCenter.unregisterEventListener(this, GROUP_CHANGE_EVENTS)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        refreshMyGroupNickname()
        if (skipMembersRefreshOnResume) {
            skipMembersRefreshOnResume = false
            return
        }
        viewModel?.refreshMembers(qunId, currentUserId)
    }
}
