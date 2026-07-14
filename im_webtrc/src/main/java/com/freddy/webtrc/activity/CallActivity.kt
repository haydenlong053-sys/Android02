package com.freddy.webtrc.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.PermissionUtils
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.requestPermission
import com.freddy.webtrc.R
import com.freddy.webtrc.utils.BroadE
import com.freddy.webtrc.utils.CallUtil
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import io.livekit.android.events.RoomEvent

/**
 * 基于 [CallUtil] / LiveKit 的单聊通话页，UI 对齐 [com.cboda.chatui.voip.SingleCallBroadActivity] 语音布局。
 */
class CallActivity : AppCompatActivity() {
    private val tAG = "CallActivityClass"
    private lateinit var backgroundImageView: ImageView
    private lateinit var portraitImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var descTextView: TextView
    private lateinit var muteImageView: ImageView
    private lateinit var speakerImageView: ImageView
    private lateinit var outgoingActionContainer: View
    private lateinit var incomingActionContainer: View
    private lateinit var blurView: BlurView
    private lateinit var blurTarget: BlurTarget

    private var targetId: String = ""
    private var roomName: String = ""
    private var callUrl: String = ""
    private var callToken: String = ""
    private var video: Boolean = false
    private var outgoing: Boolean = true

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_call)
        acquireWakeLock()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        bindViews()
        parseIntent()
        if (TextUtils.isEmpty(targetId)) {
            finishFade()
            return
        }
        bindEvents()
        renderPeerInfo()
        applyInitialUi()
        CallUtil.setCallListener(lifecycle.coroutineScope, this) { event -> onRoomEvent(event) }
        if (outgoing || (!TextUtils.isEmpty(callUrl) && !TextUtils.isEmpty(callToken))) {
            joinCurrentRoom()
        }
    }

    private fun bindViews() {
        backgroundImageView = findViewById(R.id.backgroundImageView)
        portraitImageView = findViewById(R.id.portraitImageView)
        nameTextView = findViewById(R.id.nameTextView)
        descTextView = findViewById(R.id.descTextView)
        outgoingActionContainer = findViewById(R.id.outgoingActionContainer)
        incomingActionContainer = findViewById(R.id.incomingActionContainer)
        blurView = findViewById(R.id.blurView)
        blurTarget = findViewById(R.id.target)
        muteImageView = outgoingActionContainer.findViewById(R.id.muteImageView)
        speakerImageView = outgoingActionContainer.findViewById(R.id.speakerImageView)
    }

    private fun parseIntent() {
        targetId = intent.getStringExtra(EXTRA_TARGET_ID).orEmpty()
        roomName = intent.getStringExtra(EXTRA_ROOM_NAME).orEmpty()
        callUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        callToken = intent.getStringExtra(EXTRA_TOKEN).orEmpty()
        video = intent.getBooleanExtra(EXTRA_VIDEO, false)
        outgoing = intent.getBooleanExtra(EXTRA_OUTGOING, true)
    }

    private fun bindEvents() {
        findViewById<View>(R.id.muteImageView).setOnClickListener { toggleMute() }
        findViewById<View>(R.id.speakerImageView).setOnClickListener { toggleSpeaker() }
        findViewById<View>(R.id.outgoingHangupImageView).setOnClickListener { hangup() }
        findViewById<View>(R.id.incomingHangupImageView).setOnClickListener { hangup() }
        findViewById<View>(R.id.acceptImageView).setOnClickListener {
            eLogD(tAG, "接听电话")
            requestPermission(arrayOf(Manifest.permission.RECORD_AUDIO)) {
                acceptCall()
            }
        }
        findViewById<View>(R.id.minimizeImageView).setOnClickListener { moveTaskToBack(true) }
    }

    private fun applyInitialUi() {
        if (outgoing) {
            descTextView.setText(com.cboda.baselibrary.R.string.call_waiting)
            outgoingActionContainer.visibility = View.VISIBLE
            incomingActionContainer.visibility = View.GONE
        } else if (TextUtils.isEmpty(callUrl) || TextUtils.isEmpty(callToken)) {
            descTextView.setText(
                if (video) com.cboda.baselibrary.R.string.call_invite_video else com.cboda.baselibrary.R.string.call_invite_audio
            )
            outgoingActionContainer.visibility = View.GONE
            incomingActionContainer.visibility = View.VISIBLE
        } else {
            descTextView.setText(com.cboda.baselibrary.R.string.call_connecting)
            outgoingActionContainer.visibility = View.VISIBLE
            incomingActionContainer.visibility = View.GONE
        }
        speakerImageView.isSelected = speakerOn
        applySpeakerRoute()
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
        portraitImageView.loadImg(userImg)
        backgroundImageView.loadImg(userImg)
        nameTextView.text = userName.orEmpty()
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
        descTextView.setText(com.cboda.baselibrary.R.string.call_connecting)
        CallUtil.joinRoom(callUrl, callToken, lifecycleScope, this)
        CallUtil.openOrCloseMicrophone(true, lifecycleScope, this)
    }

    private fun acceptCall() {
        if (!TextUtils.isEmpty(callUrl) && !TextUtils.isEmpty(callToken)) {
            incomingActionContainer.visibility = View.GONE
            outgoingActionContainer.visibility = View.VISIBLE
            joinCurrentRoom()
            return
        }
        if (TextUtils.isEmpty(roomName)) {
            ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
            finishFade()
            return
        }
        descTextView.setText(com.cboda.baselibrary.R.string.call_connecting)
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
                outgoingActionContainer.visibility = View.VISIBLE
                joinCurrentRoom()
            }
        }
    }

    private fun onRoomEvent(event: RoomEvent) {
//        eLogD(tAG, "event:" + event.toString())
        eLogD(tAG, "event2:" + event.javaClass.simpleName)
        runOnUiThread {
            when (event) {
                is RoomEvent.ParticipantConnected,
                is RoomEvent.TrackSubscribed -> onCallConnected()

                is RoomEvent.Connected -> {
                    // 进房时对端已在房内（典型为被叫接听）则直接进入通话态
                    if (CallUtil.hasRemoteParticipant(this)) {
                        onCallConnected()
                    }
                }

                is RoomEvent.ParticipantDisconnected,
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

    private fun onCallConnected() {
        if (callConnected) return
        callConnected = true
        connectedAtMs = System.currentTimeMillis()
        incomingActionContainer.visibility = View.GONE
        outgoingActionContainer.visibility = View.VISIBLE
        updateDurationText()
        durationHandler.removeCallbacks(durationTicker)
        durationHandler.post(durationTicker)
        CallUtil.openOrCloseMicrophone(!muted, lifecycleScope, this)
    }

    private fun updateDurationText() {
        var s = (System.currentTimeMillis() - connectedAtMs) / 1000
        if (s < 0) s = 0
        descTextView.text = if (s > 3600) {
            String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60)
        } else {
            String.format("%02d:%02d", s / 60, s % 60)
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

    private fun hangup() {
//        if (finishing) return
        ToastBroadUtils.show("挂断通话")
        finishing = true
        durationHandler.removeCallbacks(durationTicker)
        CallUtil.leaveRoom(this)
        restoreAudio()
        finishFade()
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
        const val EXTRA_ROOM_NAME = "roomName"
        const val EXTRA_URL = "url"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_VIDEO = "video"
        const val EXTRA_OUTGOING = "outgoing"

        @JvmStatic
        @JvmOverloads
        fun startOutgoing(
            context: Context,
            targetId: String?,
            url: String?,
            token: String?,
            roomName: String? = null,
            video: Boolean = false
        ) {
            if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(url) || TextUtils.isEmpty(token)) {
                ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
                return
            }
            val intent = Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_TARGET_ID, targetId)
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TOKEN, token)
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
            token: String? = null
        ) {
            if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(roomName)) {
                ToastBroadUtils.show(com.cboda.baselibrary.R.string.call_failed)
                return
            }
            val intent = Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_TARGET_ID, targetId)
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
