package com.freddy.webtrc.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.requestPermission
import com.freddy.im.call.CallMissedLocalStore
import com.freddy.im.call.CallSignalSender
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.GroupMemberItem
import com.freddy.webtrc.R
import com.freddy.webtrc.utils.BroadE
import com.freddy.webtrc.utils.CallUtil
import com.freddy.webtrc.widget.MultiCallParticipantItem
import java.lang.ref.WeakReference
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import io.livekit.android.events.RoomEvent

/**
 * 基于 [CallUtil] / LiveKit 的通话页。
 * - 单人：UI 对齐 [com.cboda.chatui.voip.SingleCallBroadActivity]
 * - 多人：UI 对齐 [com.cboda.chatui.voip.MultiCallBroadActivity] 语音布局
 */
class CallActivity : AppCompatActivity() {
    private val tAG = "CallActivityClass"

    // 单人 UI
    private var portraitImageView: ImageView? = null
    private var nameTextView: TextView? = null
    private var descTextView: TextView? = null
    private var outgoingActionContainer: View? = null

    // 多人 UI
    private var audioContainerGridLayout: GridLayout? = null
    private var connectedContainer: View? = null
    private var durationTextView: TextView? = null

    // 共用
    private lateinit var backgroundImageView: ImageView
    private lateinit var muteImageView: ImageView
    private lateinit var speakerImageView: ImageView
    private lateinit var incomingActionContainer: View
    private lateinit var blurView: BlurView
    private lateinit var blurTarget: BlurTarget

    private var targetId: String = ""
    private var participantIds: ArrayList<String> = arrayListOf()
    private var groupId: String = ""
    private var callId: String = ""
    private var roomName: String = ""
    private var callUrl: String = ""
    private var callToken: String = ""
    private var video: Boolean = false
    private var outgoing: Boolean = true
    private var isMultiCall: Boolean = false

    private var muted = false
    private var speakerOn = true
    private var callConnected = false
    private var finishing = false

    private var connectedAtMs = 0L
    private val durationHandler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var audioManager: AudioManager

    /** 群通话：已接通（信令或 LiveKit）的成员，用于取消其未接听超时。 */
    private val connectedParticipantIds = mutableSetOf<String>()
    /** 群通话：各被叫 60s 未接听超时任务。 */
    private val pendingRingTimeouts = mutableMapOf<String, Runnable>()

    private val durationTicker = object : Runnable {
        override fun run() {
            if (callConnected && !isFinishing) {
                updateDurationText()
                durationHandler.postDelayed(this, 1000L)
            }
        }
    }

    /** 本次挂断是否因振铃超时未接听（用于 CANCEL payload / 文案区分）。 */
    private var leaveAsTimeout = false

    /** 通话页打开时间，用于被叫侧判断「振铃过久后的 CANCEL」是否为超时未接。 */
    private var openedAtMs = 0L

    /** 1v1 主叫振铃超时：未接通则按「未接听」结束（仍发 CANCEL 停对端振铃）。 */
    private val outgoingRingTimeout = Runnable {
        if (finishing || isFinishing || callConnected || !outgoing) {
            return@Runnable
        }
        eLogD(tAG, "主叫振铃超时 ${OUTGOING_RING_TIMEOUT_MS}ms，未接听 callId=$callId")
        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_remote_no_answer)
        hangup(sendSignal = true, timeoutNoAnswer = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openedAtMs = System.currentTimeMillis()
        currentRef = WeakReference(this)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        parseIntent()
        isMultiCall = resolveIsMultiCall()
        setContentView(if (isMultiCall) R.layout.activity_multi_call else R.layout.activity_call)
        acquireWakeLock()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        bindViews()
        if (TextUtils.isEmpty(targetId) && participantIds.isEmpty()) {
            finishFade()
            return
        }
        if (TextUtils.isEmpty(targetId) && participantIds.isNotEmpty()) {
            targetId = participantIds.first()
        }
        bindEvents()
        if (isMultiCall) {
            initMultiParticipants()
            applyMultiInitialUi()
            startMultiParticipantRingTimeouts()
        } else {
            renderPeerInfo()
            applySingleInitialUi()
        }
        CallUtil.setCallListener(lifecycle.coroutineScope, this) { event -> onRoomEvent(event) }
        if (outgoing || (!TextUtils.isEmpty(callUrl) && !TextUtils.isEmpty(callToken))) {
            joinCurrentRoom()
        }
        startOutgoingRingTimeout()
    }

    private fun parseIntent() {
        targetId = intent.getStringExtra(EXTRA_TARGET_ID).orEmpty()
        callId = intent.getStringExtra(EXTRA_CALL_ID).orEmpty()
        roomName = intent.getStringExtra(EXTRA_ROOM_NAME).orEmpty()
        callUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        callToken = intent.getStringExtra(EXTRA_TOKEN).orEmpty()
        video = intent.getBooleanExtra(EXTRA_VIDEO, false)
        outgoing = intent.getBooleanExtra(EXTRA_OUTGOING, true)
        participantIds = intent.getStringArrayListExtra(EXTRA_PARTICIPANT_IDS) ?: arrayListOf()
        groupId = intent.getStringExtra(EXTRA_GROUP_ID).orEmpty()
        if (participantIds.isEmpty() && !TextUtils.isEmpty(targetId)) {
            participantIds = arrayListOf(targetId)
        }
        if (TextUtils.isEmpty(callId)) {
            callId = roomName
        }
        if (TextUtils.isEmpty(roomName)) {
            roomName = callId
        }
    }

    /** 群通话（有 groupId）或邀请多人，均按多人逻辑处理。 */
    private fun resolveIsMultiCall(): Boolean {
        if (!TextUtils.isEmpty(groupId)) {
            return true
        }
        return participantIds.count { it.isNotBlank() } > 1
    }

    private fun bindViews() {
        backgroundImageView = findViewById(R.id.backgroundImageView)
        blurView = findViewById(R.id.blurView)
        blurTarget = findViewById(R.id.target)
        incomingActionContainer = findViewById(R.id.incomingActionContainer)

        if (isMultiCall) {
            audioContainerGridLayout = findViewById(R.id.audioContainerGridLayout)
            connectedContainer = findViewById(R.id.connectedContainer)
            durationTextView = connectedContainer?.findViewById(R.id.durationTextView)
            muteImageView = connectedContainer!!.findViewById(R.id.muteImageView)
            speakerImageView = connectedContainer!!.findViewById(R.id.speakerImageView)
        } else {
            portraitImageView = findViewById(R.id.portraitImageView)
            nameTextView = findViewById(R.id.nameTextView)
            descTextView = findViewById(R.id.descTextView)
            outgoingActionContainer = findViewById(R.id.outgoingActionContainer)
            muteImageView = outgoingActionContainer!!.findViewById(R.id.muteImageView)
            speakerImageView = outgoingActionContainer!!.findViewById(R.id.speakerImageView)
        }
    }

    private fun bindEvents() {
        findViewById<View>(R.id.minimizeImageView)?.setOnClickListener { moveTaskToBack(true) }
        muteImageView.setOnClickListener { toggleMute() }
        speakerImageView.setOnClickListener { toggleSpeaker() }

        if (isMultiCall) {
            connectedContainer?.findViewById<View>(R.id.hangupImageView)?.setOnClickListener { hangup() }
            incomingActionContainer.findViewById<View>(R.id.incomingHangupImageView)?.setOnClickListener { rejectIncoming() }
            incomingActionContainer.findViewById<View>(R.id.acceptImageView)?.setOnClickListener {
                requestPermission(arrayOf(Manifest.permission.RECORD_AUDIO)) { acceptCall() }
            }
        } else {
            findViewById<View>(R.id.outgoingHangupImageView).setOnClickListener { hangup() }
            findViewById<View>(R.id.incomingHangupImageView).setOnClickListener { rejectIncoming() }
            findViewById<View>(R.id.acceptImageView).setOnClickListener {
                eLogD(tAG, "接听电话")
                requestPermission(arrayOf(Manifest.permission.RECORD_AUDIO)) { acceptCall() }
            }
        }
    }

    private fun applySingleInitialUi() {
        if (outgoing) {
            descTextView?.setText(com.cboda.baselibrary.R.string.call_waiting)
            outgoingActionContainer?.visibility = View.VISIBLE
            incomingActionContainer.visibility = View.GONE
        } else if (TextUtils.isEmpty(callUrl) || TextUtils.isEmpty(callToken)) {
            descTextView?.setText(
                if (video) com.cboda.baselibrary.R.string.call_invite_video
                else com.cboda.baselibrary.R.string.call_invite_audio
            )
            outgoingActionContainer?.visibility = View.GONE
            incomingActionContainer.visibility = View.VISIBLE
        } else {
            descTextView?.setText(com.cboda.baselibrary.R.string.call_connecting)
            outgoingActionContainer?.visibility = View.VISIBLE
            incomingActionContainer.visibility = View.GONE
        }
        speakerImageView.isSelected = speakerOn
        applySpeakerRoute()
    }

    private fun applyMultiInitialUi() {
        if (outgoing) {
            connectedContainer?.visibility = View.VISIBLE
            incomingActionContainer.visibility = View.GONE
        } else if (TextUtils.isEmpty(callUrl) || TextUtils.isEmpty(callToken)) {
            connectedContainer?.visibility = View.GONE
            incomingActionContainer.visibility = View.VISIBLE
        } else {
            connectedContainer?.visibility = View.VISIBLE
            incomingActionContainer.visibility = View.GONE
        }
        speakerImageView.isSelected = speakerOn
        applySpeakerRoute()
    }

    /** 多人：网格展示自己 + 被邀请成员（对齐 MultiCallAudioBroadFragment）。 */
    private fun initMultiParticipants() {
        val grid = audioContainerGridLayout ?: return
        grid.removeAllViews()
        // 固定 3 列单元格宽度，人多时纵向滚动，避免越缩越小挤在一起
        val size = multiParticipantCellSize()

        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        val selfName = MMKVBroadUtils.getUserName().orEmpty()
        val selfImg = MMKVBroadUtils.getUserImg()
        addParticipantItem(grid, selfId, selfName, selfImg, size, isSelf = true)

        // 背景用第一个被叫头像（好友库 → 群成员库）
        val firstPeer = participantIds.firstOrNull().orEmpty()
        if (firstPeer.isNotEmpty()) {
            loadParticipantProfile(firstPeer) { _, avatar ->
                backgroundImageView.loadImg(avatar)
                blurView.setupWith(blurTarget)
                    .setFrameClearDrawable(window.decorView.background)
                    .setBlurRadius(20f)
            }
        }

        participantIds.forEach { uid ->
            if (uid == selfId) return@forEach
            loadParticipantProfile(uid) { name, avatar ->
                runOnUiThread {
                    addParticipantItem(grid, uid, name, avatar, size, isSelf = false)
                }
            }
        }
    }

    /** 多人语音格子宽度：始终按 3 列均分屏幕，不随人数缩小。 */
    private fun multiParticipantCellSize(): Int {
        return resources.displayMetrics.widthPixels / 3
    }

    /**
     * 通话参与者展示信息：好友库优先，群成员库兜底（仅头像 + 名称）。
     * 名称：群昵称 → 好友备注 → 好友昵称 → 群成员昵称 → userId。
     */
    private fun loadParticipantProfile(userId: String, onResult: (name: String, avatar: String?) -> Unit) {
        if (userId.isBlank()) {
            onResult("", null)
            return
        }
        RealmUtil.findUserData(userId) { friend ->
            val member: GroupMemberItem? = if (groupId.isNotBlank()) {
                try {
                    ImLocalStore.queryGroupMember(groupId, userId)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
            val name: String = when {
                member != null && !TextUtils.isEmpty(member.groupNickname) ->
                    member.groupNickname.trim()
                friend != null && !TextUtils.isEmpty(friend.remarkName) ->
                    friend.remarkName.orEmpty()
                friend != null && !TextUtils.isEmpty(friend.nickName) ->
                    friend.nickName.orEmpty()
                member != null && !TextUtils.isEmpty(member.displayName) ->
                    member.displayName
                else -> userId
            }
            val avatar: String? = when {
                friend != null && !TextUtils.isEmpty(friend.userImg) -> friend.userImg
                member != null && !TextUtils.isEmpty(member.headPortrait) -> member.headPortrait
                else -> null
            }
            onResult(name, avatar)
        }
    }

    private fun addParticipantItem(
        grid: GridLayout,
        userId: String,
        name: String?,
        avatar: String?,
        size: Int,
        isSelf: Boolean
    ) {
        if (grid.findViewWithTag<View>(userId) != null) return
        val item = MultiCallParticipantItem(this)
        item.tag = userId
        item.layoutParams = ViewGroup.LayoutParams(size, ViewGroup.LayoutParams.WRAP_CONTENT)
        item.portraitImageView.loadImg(avatar)
        item.userNameTv.text = name.orEmpty()
        if (isSelf) {
            item.statusTextView.visibility = View.GONE
        } else {
            item.statusTextView.visibility = View.VISIBLE
            item.statusTextView.setText(com.cboda.baselibrary.R.string.call_connecting)
        }
        grid.addView(item)
    }

    private fun markParticipantConnected(userId: String) {
        if (userId.isBlank()) return
        connectedParticipantIds.add(userId)
        cancelParticipantRingTimeout(userId)
        val item = audioContainerGridLayout?.findViewWithTag<MultiCallParticipantItem>(userId) ?: return
        item.statusTextView.visibility = View.GONE
        item.statusTextView.text = ""
    }

    private fun removeParticipant(userId: String) {
        cancelParticipantRingTimeout(userId)
        connectedParticipantIds.remove(userId)
        val grid = audioContainerGridLayout
        if (grid != null) {
            val view = grid.findViewWithTag<View>(userId)
            if (view != null) {
                grid.removeView(view)
            }
        }
        participantIds.remove(userId)
        if (TextUtils.equals(targetId, userId)) {
            targetId = participantIds.firstOrNull().orEmpty()
        }
    }

    /** 群通话主叫：对每个被叫启动 60s 未接听超时，超时后从页面移除。 */
    private fun startMultiParticipantRingTimeouts() {
        cancelAllParticipantRingTimeouts()
        if (!outgoing || !isMultiCall || finishing) {
            return
        }
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        participantIds
            .filter { it.isNotBlank() && it != selfId }
            .forEach { scheduleParticipantRingTimeout(it) }
    }

    private fun scheduleParticipantRingTimeout(userId: String) {
        if (userId.isBlank() || connectedParticipantIds.contains(userId)) {
            return
        }
        cancelParticipantRingTimeout(userId)
        val runnable = Runnable {
            pendingRingTimeouts.remove(userId)
            if (finishing || isFinishing) return@Runnable
            if (connectedParticipantIds.contains(userId)) return@Runnable
            eLogD(tAG, "群通话成员 $userId ${OUTGOING_RING_TIMEOUT_MS}ms 未接听，移除 callId=$callId")
            removeUnansweredParticipant(userId)
        }
        pendingRingTimeouts[userId] = runnable
        durationHandler.postDelayed(runnable, OUTGOING_RING_TIMEOUT_MS)
    }

    /** 超时未接听：发 CANCEL(timeout) 停对端振铃（带 groupId，不落会话消息），并从本页移除。 */
    private fun removeUnansweredParticipant(userId: String) {
        val id = if (!TextUtils.isEmpty(callId)) callId else roomName
        if (!TextUtils.isEmpty(id) && userId.isNotBlank()) {
            CallMissedLocalStore.markTimeoutAsCaller(id, userId)
            val gid = groupId.takeIf { it.isNotBlank() }
            CallSignalSender.cancelForTimeout(id, listOf(userId), roomName, video, gid)
        }
        removeParticipant(userId)
        if (shouldEndMultiCall()) {
            ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_remote_no_answer)
            hangup(sendSignal = false)
        }
    }

    private fun cancelParticipantRingTimeout(userId: String) {
        pendingRingTimeouts.remove(userId)?.let { durationHandler.removeCallbacks(it) }
    }

    private fun cancelAllParticipantRingTimeouts() {
        pendingRingTimeouts.values.forEach { durationHandler.removeCallbacks(it) }
        pendingRingTimeouts.clear()
    }

    /**
     * 多人通话是否应整通结束：
     * LiveKit 已无远端，且邀请列表中也没有其他人（含未接听）。
     */
    private fun shouldEndMultiCall(): Boolean {
        if (CallUtil.hasRemoteParticipant(this)) {
            return false
        }
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        return participantIds.none { it.isNotBlank() && it != selfId }
    }

    /** 对端某人离开/拒绝/忙线：多人仅移除该成员；无人剩余时才结束。 */
    private fun handleRemoteParticipantLeft(fromUserId: String?, busy: Boolean) {
        if (finishing || isFinishing) return
        if (!fromUserId.isNullOrBlank()) {
            removeParticipant(fromUserId)
        }
        if (shouldEndMultiCall()) {
            val toastRes = if (busy) {
                com.cboda.baselibrary.R.string.call_remote_busy
            } else {
                com.cboda.baselibrary.R.string.call_ended
            }
            ToastBroadUtils.show(toastRes)
            hangup(sendSignal = false)
        }
    }

    private fun renderPeerInfo() {
        RealmUtil.findUserData(targetId) { friend ->
            if (friend != null) {
                val name = if (!TextUtils.isEmpty(friend.remarkName)) {
                    friend.remarkName
                } else {
                    friend.nickName
                }
                updateUserInfo(friend.userImg, name)
            } else {
                updateUserInfo(null, targetId)
            }
        }
    }

    private fun updateUserInfo(userImg: String?, userName: String?) {
        portraitImageView?.loadImg(userImg)
        backgroundImageView.loadImg(userImg)
        nameTextView?.text = userName.orEmpty()
        blurView.setupWith(blurTarget)
            .setFrameClearDrawable(window.decorView.background)
            .setBlurRadius(20f)
    }

    private fun joinCurrentRoom() {
        if (TextUtils.isEmpty(callUrl) || TextUtils.isEmpty(callToken)) {
            ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
            finishFade()
            return
        }
        if (!isMultiCall) {
            descTextView?.setText(com.cboda.baselibrary.R.string.call_connecting)
        }
        CallUtil.joinRoom(callUrl, callToken, lifecycleScope, this)
        CallUtil.openOrCloseMicrophone(true, lifecycleScope, this)
    }

    private fun acceptCall() {
        val id = if (!TextUtils.isEmpty(callId)) callId else roomName
        if (!TextUtils.isEmpty(callUrl) && !TextUtils.isEmpty(callToken)) {
            incomingActionContainer.visibility = View.GONE
            if (isMultiCall) {
                connectedContainer?.visibility = View.VISIBLE
            } else {
                outgoingActionContainer?.visibility = View.VISIBLE
            }
            joinCurrentRoom()
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(targetId)) {
                CallSignalSender.accept(id, targetId, roomName, video)
            }
            return
        }
        if (TextUtils.isEmpty(roomName)) {
            ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
            finishFade()
            return
        }
        if (!isMultiCall) {
            descTextView?.setText(com.cboda.baselibrary.R.string.call_connecting)
        }
        val userId = MMKVBroadUtils.getRealUserId()
        val userName = MMKVBroadUtils.getUserName()
        BroadE.getCallToken(userId, roomName, userName, video) { bean ->
            runOnUiThread {
                if (bean == null || TextUtils.isEmpty(bean.url) || TextUtils.isEmpty(bean.token)) {
                    ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
                    finishFade()
                    return@runOnUiThread
                }
                callUrl = bean.url
                callToken = bean.token
                if (!TextUtils.isEmpty(bean.roomName)) {
                    roomName = bean.roomName
                }
                incomingActionContainer.visibility = View.GONE
                if (isMultiCall) {
                    connectedContainer?.visibility = View.VISIBLE
                } else {
                    outgoingActionContainer?.visibility = View.VISIBLE
                }
                joinCurrentRoom()
                CallSignalSender.accept(if (!TextUtils.isEmpty(callId)) callId else roomName, targetId, roomName, video)
            }
        }
    }

    /**
     * 信令通知某用户已接通：
     * - 1v1：进入已接通态（显示时长）
     * - 多人：刷新该成员格子状态，并进入已接通计时
     */
    private fun handleRemoteUserConnected(fromUserId: String?) {
        if (finishing || isFinishing) return
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        if (!fromUserId.isNullOrBlank() && TextUtils.equals(fromUserId, selfId)) {
            return
        }
        if (isMultiCall) {
            if (!fromUserId.isNullOrBlank()) {
                ensureRemoteParticipantItem(fromUserId)
                markParticipantConnected(fromUserId)
            }
        }
        onCallConnected()
    }

    private fun onRoomEvent(event: RoomEvent) {
        eLogD(tAG, "event2:" + event.javaClass.simpleName)
        runOnUiThread {
            when (event) {
                is RoomEvent.ParticipantConnected -> {
                    val uid = event.participant.identity?.value.orEmpty()
                    if (isMultiCall && uid.isNotEmpty()) {
                        ensureRemoteParticipantItem(uid)
                        markParticipantConnected(uid)
                    }
                    onCallConnected()
                }

                is RoomEvent.TrackSubscribed -> {
                    val uid = event.participant.identity?.value.orEmpty()
                    if (isMultiCall && uid.isNotEmpty()) {
                        markParticipantConnected(uid)
                    }
                    onCallConnected()
                }

                is RoomEvent.Connected -> {
                    if (CallUtil.hasRemoteParticipant(this)) {
                        if (isMultiCall) {
                            syncRemoteParticipants()
                        }
                        onCallConnected()
                    }
                }

                is RoomEvent.ParticipantDisconnected -> {
                    val uid = event.participant.identity?.value.orEmpty()
                    if (isMultiCall) {
                        if (uid.isNotEmpty()) {
                            removeParticipant(uid)
                        }
                        // 多人：仍有远端在房，或仍有未接听邀请成员时，不结束本通
                        if (shouldEndMultiCall()) {
                            ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                            hangup(sendSignal = false)
                        }
                    } else if (!finishing) {
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                        hangup()
                    }
                }

                is RoomEvent.Disconnected -> {
                    if (finishing) return@runOnUiThread
                    if (isMultiCall) {
                        // 群通话：本地异常断线才收尾；若仍有未接听邀请成员则不因单人离开而整通结束
                        // LiveKit Disconnected 表示本端已离房，关闭本页但不回发挂断
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                        hangup(sendSignal = false)
                    } else {
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                        hangup()
                    }
                }

                is RoomEvent.FailedToConnect -> {
                    ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
                    hangup()
                }

                else -> Unit
            }
        }
    }

    private fun syncRemoteParticipants() {
        // 仅标记 LiveKit 房内已存在的远端，避免把未接听邀请人误标为已接通
        CallUtil.remoteParticipantIdentities(this).forEach { uid ->
            ensureRemoteParticipantItem(uid)
            markParticipantConnected(uid)
        }
    }

    private fun ensureRemoteParticipantItem(userId: String) {
        val grid = audioContainerGridLayout ?: return
        if (grid.findViewWithTag<View>(userId) != null) return
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        if (userId == selfId) return
        val size = multiParticipantCellSize()
        loadParticipantProfile(userId) { name, avatar ->
            runOnUiThread {
                // 插到自己前面（对齐野火）
                val selfView = grid.findViewWithTag<View>(selfId)
                val item = MultiCallParticipantItem(this)
                item.tag = userId
                item.layoutParams = ViewGroup.LayoutParams(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                item.portraitImageView.loadImg(avatar)
                item.userNameTv.text = name
                item.statusTextView.setText(com.cboda.baselibrary.R.string.call_connecting)
                val index = if (selfView != null) grid.indexOfChild(selfView) else -1
                if (index >= 0) {
                    grid.addView(item, index)
                } else {
                    grid.addView(item)
                }
                if (!participantIds.contains(userId)) {
                    participantIds.add(userId)
                }
            }
        }
    }

    private fun onCallConnected() {
        if (callConnected) return
        callConnected = true
        cancelOutgoingRingTimeout()
        connectedAtMs = System.currentTimeMillis()
        incomingActionContainer.visibility = View.GONE
        if (isMultiCall) {
            connectedContainer?.visibility = View.VISIBLE
        } else {
            outgoingActionContainer?.visibility = View.VISIBLE
        }
        updateDurationText()
        durationHandler.removeCallbacks(durationTicker)
        durationHandler.post(durationTicker)
        CallUtil.openOrCloseMicrophone(!muted, lifecycleScope, this)
    }

    private fun updateDurationText() {
        var s = (System.currentTimeMillis() - connectedAtMs) / 1000
        if (s < 0) s = 0
        val text = if (s > 3600) {
            String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60)
        } else {
            String.format("%02d:%02d", s / 60, s % 60)
        }
        if (isMultiCall) {
            durationTextView?.text = text
        } else {
            descTextView?.text = text
        }
    }

    private fun toggleMute() {
        muted = !muted
        muteImageView.isSelected = muted
        CallUtil.openOrCloseMicrophone(!muted, lifecycleScope, this)
    }

    private fun toggleSpeaker() {
        speakerOn = !speakerOn
        speakerImageView.isSelected = speakerOn
        applySpeakerRoute()
    }

    private fun applySpeakerRoute() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = speakerOn
    }

    /** 来电未接通时点拒绝：发 CALL_SIGNAL(0x0502) / CALL_REJECT=3（群通话带 groupId）。 */
    private fun rejectIncoming() {
        if (outgoing || callConnected || finishing) {
            hangup()
            return
        }
        val id = if (!TextUtils.isEmpty(callId)) callId else roomName
        val gid = groupId.takeIf { it.isNotBlank() }
        eLogD(tAG, "拒绝通话 callId=$id toUserId=$targetId groupId=$gid")
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(targetId)) {
            CallSignalSender.reject(id, targetId, roomName, video, gid)
        }
        hangup(sendSignal = false)
    }

    private fun hangup(sendSignal: Boolean = true, timeoutNoAnswer: Boolean = false) {
        cancelOutgoingRingTimeout()
        cancelAllParticipantRingTimeouts()
        if (timeoutNoAnswer) {
            leaveAsTimeout = true
            val id = if (!TextUtils.isEmpty(callId)) callId else roomName
            CallMissedLocalStore.markTimeoutAsCaller(id, targetId)
            CallMissedLocalStore.markTimeoutAsCaller(roomName, targetId)
        }
        if (!finishing && sendSignal) {
            sendLeaveCallSignal()
        }
        finishing = true
        durationHandler.removeCallbacks(durationTicker)
        CallUtil.leaveRoom(this)
        restoreAudio()
        finishFade()
    }

    /** 1v1 主叫：接通前 20s 未接听则超时取消。 */
    private fun startOutgoingRingTimeout() {
        cancelOutgoingRingTimeout()
        if (!outgoing || isMultiCall || callConnected || finishing) {
            return
        }
        durationHandler.postDelayed(outgoingRingTimeout, OUTGOING_RING_TIMEOUT_MS)
    }

    private fun cancelOutgoingRingTimeout() {
        durationHandler.removeCallbacks(outgoingRingTimeout)
    }

    /**
     * 接通前 / 发起方挂断：
     * - 1v1 主叫取消发 CALL_CANCEL=4
     * - 群/多人：无人接听或仅 1 人接听 → CALL_END=9 整通结束（停振铃 + 结束已接听方）
     * - 群/多人：已有 ≥2 人接听 → CALL_HANGUP=6，发起方离开，其余人继续
     * 接通后非上述「整通结束」场景：离开本通发 CALL_HANGUP=6。
     * 来电拒绝由 [rejectIncoming] 单独发 CALL_REJECT。
     */
    private fun sendLeaveCallSignal() {
        val id = if (!TextUtils.isEmpty(callId)) callId else roomName
        if (TextUtils.isEmpty(id)) {
            return
        }
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        val targets = resolveLeaveSignalTargets(selfId)
        if (targets.isEmpty()) {
            return
        }
        val gid = groupId.takeIf { it.isNotBlank() }

        // 群聊发起方挂断：0 人或仅 1 人接听 → 整通结束
        if (isMultiCall && outgoing && shouldEndWholeMultiCallOnInitiatorLeave()) {
            eLogD(
                tAG,
                "群通话发起方结束整通 CALL_END answered=${remoteAnsweredCount()}" +
                        " callId=$id toUserIds=$targets groupId=$gid"
            )
            CallSignalSender.end(id, targets, roomName, video, gid)
            return
        }

        if (callConnected) {
            eLogD(tAG, "挂断通话 CALL_HANGUP callId=$id toUserIds=$targets groupId=$gid")
            CallSignalSender.hangup(id, targets, roomName, video, gid)
            return
        }
        if (outgoing) {
            if (isMultiCall) {
                eLogD(tAG, "群通话主叫结束 CALL_END callId=$id toUserIds=$targets groupId=$gid")
                CallSignalSender.end(id, targets, roomName, video, gid)
            } else if (leaveAsTimeout) {
                eLogD(tAG, "主叫超时未接听 CALL_CANCEL(timeout) callId=$id toUserIds=$targets")
                CallSignalSender.cancelForTimeout(id, targets, roomName, video, null)
            } else {
                eLogD(tAG, "主叫取消通话 CALL_CANCEL callId=$id toUserIds=$targets")
                CallSignalSender.cancel(id, targets, roomName, video, null, null)
            }
        }
    }

    /** 挂断信令目标：剩余邀请成员 + 已接通成员（去重，排除自己）。 */
    private fun resolveLeaveSignalTargets(selfId: String): List<String> {
        val set = linkedSetOf<String>()
        participantIds.forEach { uid ->
            if (uid.isNotBlank() && uid != selfId) set.add(uid)
        }
        connectedParticipantIds.forEach { uid ->
            if (uid.isNotBlank() && uid != selfId) set.add(uid)
        }
        if (set.isEmpty() && targetId.isNotBlank() && targetId != selfId) {
            set.add(targetId)
        }
        return set.toList()
    }

    /** 远端已接通人数（不含自己）。 */
    private fun remoteAnsweredCount(): Int {
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        return connectedParticipantIds.count { it.isNotBlank() && it != selfId }
    }

    /**
     * 发起方离开时是否应整通结束：
     * 接听方无人接听，或仅有 1 人接听。
     */
    private fun shouldEndWholeMultiCallOnInitiatorLeave(): Boolean {
        return remoteAnsweredCount() <= 1
    }

    private fun finishFade() {
        if (isFinishing) return
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun restoreAudio() {
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "CallActivity"
        ).also { it.acquire(60 * 60 * 1000L) }
    }

    override fun onDestroy() {
        if (currentRef?.get() === this) {
            currentRef = null
        }
        cancelOutgoingRingTimeout()
        cancelAllParticipantRingTimeouts()
        durationHandler.removeCallbacks(durationTicker)
        if (!finishing) {
            CallUtil.leaveRoom(this)
        }
        restoreAudio()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
        super.onDestroy()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    companion object {
        const val EXTRA_TARGET_ID = "targetId"
        const val EXTRA_PARTICIPANT_IDS = "participantIds"
        const val EXTRA_GROUP_ID = "groupId"
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_ROOM_NAME = "roomName"
        const val EXTRA_URL = "url"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_VIDEO = "video"
        const val EXTRA_OUTGOING = "outgoing"

        /** 主叫振铃超时（毫秒）。 */
        private const val OUTGOING_RING_TIMEOUT_MS = 60_000L

        /** 被叫侧判断「振铃过久后的 CANCEL」是否为超时（略短于主叫超时，留网络余量）。 */
        @JvmStatic
        fun ringTimeoutDetectMs(): Long = OUTGOING_RING_TIMEOUT_MS - 2_000L

        /** 主叫振铃超时时长（供信令层对齐判断）。 */
        @JvmStatic
        fun outgoingRingTimeoutMs(): Long = OUTGOING_RING_TIMEOUT_MS

        @Volatile
        private var currentRef: WeakReference<CallActivity>? = null

        /** 当前是否已在通话页（用于忙线判断）。 */
        @JvmStatic
        fun isInCall(): Boolean {
            val activity = currentRef?.get() ?: return false
            return !activity.isFinishing && !activity.finishing
        }

        /** 是否为当前这通通话（按 callId / roomName 匹配）。 */
        @JvmStatic
        fun isSameCall(matchId: String?): Boolean {
            if (matchId.isNullOrBlank()) return false
            val activity = currentRef?.get() ?: return false
            return TextUtils.equals(matchId, activity.callId)
                    || TextUtils.equals(matchId, activity.roomName)
        }

        /**
         * 信令：某用户已接通（0x0502 type=1 / ACCEPT / MEMBER_JOINED）。
         * 刷新通话页：1v1 进入计时；群通话更新该成员状态。
         */
        @JvmStatic
        fun onRemoteUserConnected(matchId: String?, fromUserId: String?) {
            if (matchId.isNullOrBlank()) return
            val activity = currentRef?.get() ?: return
            val same = TextUtils.equals(matchId, activity.callId)
                    || TextUtils.equals(matchId, activity.roomName)
            if (!same || activity.isFinishing || activity.finishing) return
            activity.runOnUiThread {
                activity.handleRemoteUserConnected(fromUserId)
            }
        }

        /**
         * 对端某人挂断/拒绝/忙线/取消。
         * - 多人：只移除该成员；仍有人在通或未接听时不结束本通
         * - 1v1：整通结束
         */
        @JvmStatic
        @JvmOverloads
        fun onRemoteParticipantLeft(matchId: String?, fromUserId: String?, busy: Boolean = false) {
            if (matchId.isNullOrBlank()) return
            val activity = currentRef?.get() ?: return
            val same = TextUtils.equals(matchId, activity.callId)
                    || TextUtils.equals(matchId, activity.roomName)
            if (!same || activity.isFinishing || activity.finishing) return
            activity.runOnUiThread {
                if (activity.isMultiCall) {
                    activity.handleRemoteParticipantLeft(fromUserId, busy)
                } else {
                    val toastRes = if (busy) {
                        com.cboda.baselibrary.R.string.call_remote_busy
                    } else {
                        com.cboda.baselibrary.R.string.call_ended
                    }
                    ToastBroadUtils.show(toastRes)
                    activity.hangup(sendSignal = false)
                }
            }
        }

        /**
         * 主叫接通前取消：
         * - 群/多人且本端已在通话中：仅视为该成员离开，不整通结束
         * - 来电未接听 / 1v1：关闭本通
         * - [timeoutNoAnswer]=true 时为振铃超时，不提示「对方已取消」
         */
        @JvmStatic
        @JvmOverloads
        fun finishIfRemoteCancelled(
            matchId: String?,
            fromUserId: String? = null,
            timeoutNoAnswer: Boolean = false
        ) {
            if (matchId.isNullOrBlank()) return
            val activity = currentRef?.get() ?: return
            val same = TextUtils.equals(matchId, activity.callId)
                    || TextUtils.equals(matchId, activity.roomName)
            if (!same || activity.isFinishing || activity.finishing) return
            activity.runOnUiThread {
                val ringingLongEnough = !activity.outgoing
                        && !activity.callConnected
                        && activity.openedAtMs > 0L
                        && (System.currentTimeMillis() - activity.openedAtMs) >=
                        ringTimeoutDetectMs()
                val asTimeout = timeoutNoAnswer || ringingLongEnough
                if (activity.isMultiCall && (activity.callConnected || activity.outgoing)) {
                    // 主叫侧或已入会：他人取消/离开不拆整通
                    activity.handleRemoteParticipantLeft(fromUserId, busy = false)
                } else {
                    if (asTimeout) {
                        CallMissedLocalStore.markTimeoutAsCallee(matchId, fromUserId)
                        CallMissedLocalStore.markTimeoutAsCallee(activity.callId, fromUserId)
                        CallMissedLocalStore.markTimeoutAsCallee(activity.roomName, activity.targetId)
                    } else {
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_remote_cancelled)
                    }
                    activity.hangup(sendSignal = false)
                }
            }
        }

        /** 解析 CALL_CANCEL 的 payload 是否为超时未接听。 */
        @JvmStatic
        fun isCancelTimeoutPayload(payload: String?): Boolean {
            if (payload.isNullOrBlank()) return false
            val p = payload.trim()
            if (p == CallSignalSender.PAYLOAD_TIMEOUT) return true
            return p.contains("\"timeout\"", ignoreCase = true)
                    || p.contains("reason\":\"timeout", ignoreCase = true)
                    || p.contains("reason\": \"timeout", ignoreCase = true)
        }

        /** 收到 CALL_END：整通结束，关闭本通（群/1v1 均直接收尾）。 */
        @JvmStatic
        @JvmOverloads
        fun finishIfRemoteEnded(matchId: String?, fromUserId: String? = null) {
            if (matchId.isNullOrBlank()) return
            val activity = currentRef?.get() ?: return
            val same = TextUtils.equals(matchId, activity.callId)
                    || TextUtils.equals(matchId, activity.roomName)
            if (!same || activity.isFinishing || activity.finishing) return
            activity.runOnUiThread {
                eLogD(
                    activity.tAG,
                    "收到 CALL_END，整通结束 matchId=$matchId from=$fromUserId"
                )
                ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                activity.hangup(sendSignal = false)
            }
        }

        /** 对端忙线：提示「对方忙线」并结束本通（主叫侧，仅 1v1）。 */
        @JvmStatic
        fun finishIfRemoteBusy(matchId: String?) {
            finishIfRemoteSignal(matchId, com.cboda.baselibrary.R.string.call_remote_busy)
        }

        private fun finishIfRemoteSignal(matchId: String?, toastRes: Int) {
            if (matchId.isNullOrBlank()) return
            val activity = currentRef?.get() ?: return
            val same = TextUtils.equals(matchId, activity.callId)
                    || TextUtils.equals(matchId, activity.roomName)
            if (!same || activity.isFinishing || activity.finishing) return
            activity.runOnUiThread {
                ToastBroadUtils.show(toastRes)
                // 对端已挂断，本端只收尾，不再回发挂断信令
                activity.hangup(sendSignal = false)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun startOutgoing(
            context: Context,
            targetId: String?,
            url: String?,
            token: String?,
            roomName: String? = null,
            video: Boolean = false,
            participantIds: List<String>? = null,
            groupId: String? = null
        ) {
            val ids = ArrayList(
                (participantIds ?: listOfNotNull(targetId))
                    .filter { !it.isNullOrBlank() }
            )
            val primary = targetId?.takeIf { it.isNotBlank() } ?: ids.firstOrNull()
            if (primary.isNullOrBlank() || TextUtils.isEmpty(url) || TextUtils.isEmpty(token)) {
                ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
                return
            }
            val intent = Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_TARGET_ID, primary)
                putStringArrayListExtra(EXTRA_PARTICIPANT_IDS, ids)
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_CALL_ID, roomName)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_VIDEO, video)
                putExtra(EXTRA_OUTGOING, true)
                putExtra(EXTRA_GROUP_ID, groupId.orEmpty())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        /**
         * 来电：可先只传 roomName，接听时再 [BroadE.getCallToken]；
         * 若已拿到 url/token，可一并传入直接加入。
         */
        @JvmStatic
        @JvmOverloads
        fun startIncoming(
            context: Context,
            targetId: String?,
            roomName: String?,
            video: Boolean = false,
            url: String? = null,
            token: String? = null,
            participantIds: List<String>? = null,
            callId: String? = null,
            groupId: String? = null
        ) {
            if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(roomName)) {
                ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
                return
            }
            val ids = ArrayList(
                (participantIds ?: listOfNotNull(targetId))
                    .filter { !it.isNullOrBlank() }
            )
            val resolvedCallId = callId?.takeIf { it.isNotBlank() } ?: roomName
            val intent = Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_TARGET_ID, targetId)
                putStringArrayListExtra(EXTRA_PARTICIPANT_IDS, ids)
                putExtra(EXTRA_CALL_ID, resolvedCallId)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_VIDEO, video)
                putExtra(EXTRA_OUTGOING, false)
                putExtra(EXTRA_GROUP_ID, groupId.orEmpty())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
