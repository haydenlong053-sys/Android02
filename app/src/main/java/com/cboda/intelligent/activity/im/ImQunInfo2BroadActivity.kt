package com.cboda.intelligent.activity.im

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.CompressionBroadUtil.imgCompression
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.showAbsentKickTip
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
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnHttpImListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
    private lateinit var mScAbsentKickView: View
    private lateinit var mScAbsentKick: SwitchCompat
    private lateinit var mTvLookRule: TextView
    private lateinit var mTvRuleDes: TextView

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
        miaodarao = findViewById(R.id.miaodarao)
        miaodaraoView = findViewById(R.id.miaodaraoView)
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
        mScAbsentKick = findViewById(R.id.mScAbsentKick)
        mScAbsentKickView = findViewById(R.id.mScAbsentKickView)
        mTvRuleDes = findViewById(R.id.mTvRuleDes)
        mTvLookRule = findViewById(R.id.mTvLookRule)
    }

    private fun hideSwitchRow(anchorViewId: Int, ancestorLevel: Int = 1) {
        var view: View? = findViewById(anchorViewId)
        repeat(ancestorLevel) { view = view?.parent as? View }
        view?.isGone = true
    }

    private fun hideUnsupportedWildfireFeatures() {
        commercialFunctionLayout.isGone = true
        zidongfanyiView.isGone = true
        zidongfanyi.isGone = true
        wodequnnicheng.isGone = true
        findViewById<View>(R.id.shengjiqun)?.isGone = true
        findViewById<View>(R.id.shengjiqunView)?.isGone = true
        findViewById<View>(R.id.jiqiren)?.isGone = true
        findViewById<View>(R.id.huanyingchengyuan)?.isGone = true
        hideSwitchRow(R.id.yaoqinghaioyuView)
        hideSwitchRow(R.id.jinqunjiaoyanView)
        hideSwitchRow(R.id.kefahongboaView)
        hideSwitchRow(R.id.qunzhumingpianView)
        hideSwitchRow(R.id.ruqunyanzView)
        hideSwitchRow(R.id.wufajiahaoyouView)
        hideSwitchRow(R.id.kejianguanliView)
        hideSwitchRow(R.id.jianhao, 2)
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
                    this, qunId, items[position].userId ?: "",
                    items[position].type ?: "", myRole
                )
            }
        }

        bindClicks()
        viewModel?.load(qunId, currentUserId)
        CEventCenter.registerEventListener(this, GROUP_CHANGE_EVENTS)
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
        shezhiquntou.isEnabled = canManage
        shezhiqunnicheng.isEnabled = canManage
        qungonggao.isEnabled = canManage
        findViewById<View>(R.id.qunjinmyan)?.isVisible = canManage

        zhiding.isChecked = state.sessionPinned
        miaodarao.isChecked = state.sessionMuted

        if (state.noticeContent.isNullOrEmpty()) {
            mTvNoticeSet.isVisible = true
            mTvNoticeDes.isVisible = false
        } else {
            mTvNoticeSet.isVisible = false
            mTvNoticeDes.isVisible = true
            mTvNoticeDes.text = state.noticeContent
        }

        mAdapter?.setList(ImGroupMemberUiMapper.buildGridItems(state.members, myRole))

        mScAbsentKick.isChecked = vo.openAbsentKick == "1"
        val limit = vo.absentKickLimit?.takeIf { it.isNotBlank() } ?: "5"
        mTvRuleDes.text = getString(R.string.absentkick_tip, limit)
    }

    private fun bindClicks() {
        chakangebngduo.setOnClickListener {
            JumpBroadUtils.startImQunUserListActivity(this, qunId, myRole)
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
        zhidingView.setOnClickListener {
            val target = !zhiding.isChecked
            viewModel?.setSessionTop(qunId, target) { ok ->
                if (!ok) ToastBroadUtils.show(getString(R.string.group_rename_failed))
                else zhiding.isChecked = target
            }
        }
        miaodaraoView.setOnClickListener {
            val target = !miaodarao.isChecked
            viewModel?.setSessionMute(qunId, target) { ok ->
                if (!ok) ToastBroadUtils.show(getString(R.string.group_rename_failed))
                else miaodarao.isChecked = target
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
    }

    private fun goAbsentKick() {
        val state = if (mScAbsentKick.isChecked) "0" else "1"
        lifecycleScope.launch {
            viewModel?.setGroupAbsentKick(qunId, state)?.observe(this@ImQunInfo2BroadActivity) { newState ->
                mScAbsentKick.isChecked = newState == "1"
                groupVo?.openAbsentKick = newState
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
        runOnUiThread { applyGroupSessionUpdate(update) }
    }

    override fun onDestroy() {
        CEventCenter.unregisterEventListener(this, GROUP_CHANGE_EVENTS)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (skipMembersRefreshOnResume) {
            skipMembersRefreshOnResume = false
            return
        }
        viewModel?.refreshMembers(qunId, currentUserId)
    }
}
