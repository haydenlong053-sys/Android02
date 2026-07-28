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
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.requestPermission
import com.freddy.im.call.CallSignalSender
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

    private val durationTicker = object : Runnable {
        override fun run() {
            if (callConnected && !isFinishing) {
                updateDurationText()
                durationHandler.postDelayed(this, 1000L)
            }
        }
    }

    /** 主叫振铃超时：未接通则自动取消。 */
    private val outgoingRingTimeout = Runnable {
        if (finishing || isFinishing || callConnected || !outgoing) {
            return@Runnable
        }
        eLogD(tAG, "主叫振铃超时 ${OUTGOING_RING_TIMEOUT_MS}ms，自动取消 callId=$callId")
        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_remote_no_answer)
        hangup()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentRef = WeakReference(this)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        parseIntent()
        isMultiCall = participantIds.size > 1
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
        val dm: DisplayMetrics = resources.displayMetrics
        val width = dm.widthPixels
        val size = width / maxOf(Math.ceil(Math.sqrt((participantIds.size + 1).toDouble())).toInt(), 3)

        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        val selfName = MMKVBroadUtils.getUserName().orEmpty()
        val selfImg = MMKVBroadUtils.getUserImg()
        addParticipantItem(grid, selfId, selfName, selfImg, size, isSelf = true)

        // 背景用第一个被叫头像
        val firstPeer = participantIds.firstOrNull().orEmpty()
        if (firstPeer.isNotEmpty()) {
            RealmUtil.findUserData(firstPeer) { friend ->
                backgroundImageView.loadImg(friend?.userImg)
                blurView.setupWith(blurTarget)
                    .setFrameClearDrawable(window.decorView.background)
                    .setBlurRadius(20f)
            }
        }

        participantIds.forEach { uid ->
            if (uid == selfId) return@forEach
            RealmUtil.findUserData(uid) { friend ->
                val name = when {
                    friend == null -> uid
                    !TextUtils.isEmpty(friend.remarkName) -> friend.remarkName
                    else -> friend.nickName
                }
                runOnUiThread {
                    addParticipantItem(grid, uid, name, friend?.userImg, size, isSelf = false)
                }
            }
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
        val item = audioContainerGridLayout?.findViewWithTag<MultiCallParticipantItem>(userId) ?: return
        item.statusTextView.visibility = View.GONE
        item.statusTextView.text = ""
    }

    private fun removeParticipant(userId: String) {
        val grid = audioContainerGridLayout ?: return
        val view = grid.findViewWithTag<View>(userId) ?: return
        grid.removeView(view)
        participantIds.remove(userId)
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
        if (!TextUtils.isEmpty(callUrl) && !TextUtils.isEmpty(callToken)) {
            incomingActionContainer.visibility = View.GONE
            if (isMultiCall) {
                connectedContainer?.visibility = View.VISIBLE
            } else {
                outgoingActionContainer?.visibility = View.VISIBLE
            }
            joinCurrentRoom()
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
            }
        }
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
                        // 多人：仅当房间内已无远端成员时结束
                        if (!CallUtil.hasRemoteParticipant(this) && callConnected) {
                            ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                            hangup()
                        }
                    } else if (!finishing) {
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_ended)
                        hangup()
                    }
                }

                is RoomEvent.Disconnected -> {
                    if (!finishing) {
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
        // 进房时已在房内的远端，标记为已连接
        // CallUtil 未暴露列表，通过事件补齐；此处仅标记已存在格子
        participantIds.forEach { markParticipantConnected(it) }
    }

    private fun ensureRemoteParticipantItem(userId: String) {
        val grid = audioContainerGridLayout ?: return
        if (grid.findViewWithTag<View>(userId) != null) return
        val selfId = MMKVBroadUtils.getRealUserId().orEmpty()
        if (userId == selfId) return
        val dm = resources.displayMetrics
        val width = dm.widthPixels
        val size = width / 3
        RealmUtil.findUserData(userId) { friend ->
            val name = when {
                friend == null -> userId
                !TextUtils.isEmpty(friend.remarkName) -> friend.remarkName
                else -> friend.nickName
            }
            runOnUiThread {
                // 插到自己前面（对齐野火）
                val selfView = grid.findViewWithTag<View>(selfId)
                val item = MultiCallParticipantItem(this)
                item.tag = userId
                item.layoutParams = ViewGroup.LayoutParams(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                item.portraitImageView.loadImg(friend?.userImg)
                item.userNameTv.text = name.orEmpty()
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

    /** 来电未接通时点拒绝：发 CALL_SIGNAL(0x0502) / CALL_REJECT=3。 */
    private fun rejectIncoming() {
        if (outgoing || callConnected || finishing) {
            hangup()
            return
        }
        val id = if (!TextUtils.isEmpty(callId)) callId else roomName
        eLogD(tAG, "拒绝通话 callId=$id toUserId=$targetId")
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(targetId)) {
            CallSignalSender.reject(id, targetId, roomName, video)
        }
        hangup()
    }

    private fun hangup(sendSignal: Boolean = true) {
        cancelOutgoingRingTimeout()
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
     * 接通前：主叫取消发 CALL_CANCEL=4；
     * 接通后：任一方挂断发 CALL_HANGUP=6。
     * 来电拒绝由 [rejectIncoming] 单独发 CALL_REJECT。
     */
    private fun sendLeaveCallSignal() {
        val id = if (!TextUtils.isEmpty(callId)) callId else roomName
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(targetId)) {
            return
        }
        if (callConnected) {
            eLogD(tAG, "挂断通话 CALL_HANGUP callId=$id toUserId=$targetId")
            CallSignalSender.hangup(id, targetId, roomName, video)
            return
        }
        if (outgoing) {
            eLogD(tAG, "主叫取消通话 CALL_CANCEL callId=$id toUserId=$targetId")
            CallSignalSender.cancel(id, targetId, roomName, video)
        }
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
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_ROOM_NAME = "roomName"
        const val EXTRA_URL = "url"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_VIDEO = "video"
        const val EXTRA_OUTGOING = "outgoing"

        /** 主叫振铃超时（毫秒）。 */
        private const val OUTGOING_RING_TIMEOUT_MS = 60_000L

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

        /** 对端拒绝/结束时关闭当前通话页（按 callId 或 roomName 匹配）。 */
        @JvmStatic
        fun finishIfRemoteEnded(matchId: String?) {
            finishIfRemoteSignal(matchId, com.cboda.baselibrary.R.string.call_ended)
        }

        /** 对端忙线：提示「对方忙线」并结束本通（主叫侧）。 */
        @JvmStatic
        fun finishIfRemoteBusy(matchId: String?) {
            finishIfRemoteSignal(matchId, com.cboda.baselibrary.R.string.call_remote_busy)
        }

        /** 主叫接通前取消：被叫提示「对方已取消」并关闭来电页。 */
        @JvmStatic
        fun finishIfRemoteCancelled(matchId: String?) {
            finishIfRemoteSignal(matchId, com.cboda.baselibrary.R.string.call_remote_cancelled)
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
            participantIds: List<String>? = null
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
            callId: String? = null
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
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
