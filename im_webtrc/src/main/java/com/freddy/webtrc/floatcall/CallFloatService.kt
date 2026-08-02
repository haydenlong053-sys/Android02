package com.freddy.webtrc.floatcall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.cboda.baselibrary.utils.eLogD
import com.freddy.im.call.CallMissedLocalStore
import com.freddy.im.call.CallDurationLocalStore
import com.freddy.im.call.CallSignalSender
import com.freddy.webtrc.R
import com.freddy.webtrc.activity.CallActivity
import com.freddy.webtrc.utils.CallUtil
import io.livekit.android.events.RoomEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 自研通话悬浮窗（对齐野火 [com.cboda.chatui.voip.VoipCallBroadService]）：
 * 可拖动；位移很小视为点击 → 恢复通话页。
 */
class CallFloatService : Service() {

    private var wm: WindowManager? = null
    private var view: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var showFloatingWindow = false
    private var resumeActivityIntent: Intent? = null

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var eventJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        listenRoomEvents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!CallSessionHolder.active && !CallUtil.isRoomConnected()) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundCompat()
        showFloatingWindow = intent?.getBooleanExtra(EXTRA_SHOW_FLOATING, false) ?: false
        if (showFloatingWindow) {
            try {
                showFloatingWindow()
            } catch (e: Exception) {
                eLogD(TAG, "showFloatingWindow failed: ${e.message}")
            }
        } else {
            hideFloatBox()
        }
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tickRunnable)
        hideFloatBox()
        eventJob?.cancel()
        serviceScope.cancel()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun startForegroundCompat() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun listenRoomEvents() {
        eventJob?.cancel()
        eventJob = serviceScope.launch(Dispatchers.IO) {
            CallUtil.observeRoomEvents { event ->
                when (event) {
                    is RoomEvent.ParticipantConnected,
                    is RoomEvent.TrackSubscribed,
                    is RoomEvent.Connected -> {
                        handler.post {
                            val uid = when (event) {
                                is RoomEvent.ParticipantConnected ->
                                    event.participant.identity?.value
                                is RoomEvent.TrackSubscribed ->
                                    event.participant.identity?.value
                                else -> null
                            }
                            if (!uid.isNullOrBlank()) {
                                CallSessionHolder.markRemoteConnected(uid)
                            } else if (CallUtil.hasRemoteParticipant()) {
                                CallSessionHolder.markRemoteConnected(null)
                            }
                            refreshFloatContent()
                        }
                    }

                    is RoomEvent.ParticipantDisconnected -> {
                        handler.post {
                            val uid = event.participant.identity?.value.orEmpty()
                            if (CallSessionHolder.isMultiCall) {
                                if (uid.isNotBlank()) {
                                    CallSessionHolder.removeParticipant(uid)
                                }
                                if (CallSessionHolder.shouldEndMultiCall()) {
                                    endCallFromFloat(sendSignal = false)
                                }
                            } else {
                                endCallFromFloat(sendSignal = false)
                            }
                        }
                    }

                    is RoomEvent.Disconnected -> {
                        handler.post { endCallFromFloat(sendSignal = false) }
                    }

                    is RoomEvent.FailedToConnect -> {
                        handler.post { endCallFromFloat(sendSignal = false) }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun endCallFromFloat(sendSignal: Boolean) {
        if (!CallSessionHolder.active && !CallUtil.isRoomConnected()) {
            stopSelf()
            return
        }
        if (sendSignal) {
            sendLeaveSignalFromHolder()
        }
        CallUtil.leaveRoom(this)
        CallSessionHolder.clear()
        hideFloatBox()
        stopSelf()
    }

    private fun sendLeaveSignalFromHolder() {
        val id = CallSessionHolder.callId.ifBlank { CallSessionHolder.roomName }
        if (id.isBlank()) return
        val selfId = com.cboda.baselibrary.utils.MMKVBroadUtils.getRealUserId().orEmpty()
        val targets = linkedSetOf<String>().apply {
            CallSessionHolder.participantIds.forEach { if (it.isNotBlank() && it != selfId) add(it) }
            CallSessionHolder.connectedParticipantIds.forEach { if (it.isNotBlank() && it != selfId) add(it) }
            val t = CallSessionHolder.targetId
            if (isEmpty() && t.isNotBlank() && t != selfId) add(t)
        }.toList()
        if (targets.isEmpty()) return
        val gid = CallSessionHolder.groupId.takeIf { it.isNotBlank() }
        val video = CallSessionHolder.video
        val room = CallSessionHolder.roomName
        val durationSec = if (CallSessionHolder.callConnected && CallSessionHolder.connectedAtMs > 0L) {
            ((System.currentTimeMillis() - CallSessionHolder.connectedAtMs) / 1000L).toInt().coerceAtLeast(0)
        } else {
            0
        }
        if (durationSec > 0) {
            CallDurationLocalStore.mark(id, durationSec)
            CallDurationLocalStore.mark(CallSessionHolder.callId, durationSec)
            CallDurationLocalStore.mark(CallSessionHolder.roomName, durationSec)
        }
        val payload = CallSignalSender.payloadDuration(durationSec)
        when {
            CallSessionHolder.isMultiCall && CallSessionHolder.outgoing
                    && CallSessionHolder.remoteAnsweredCount() <= 1 -> {
                CallSignalSender.end(id, targets, room, video, gid, payload)
            }
            CallSessionHolder.callConnected -> {
                CallSignalSender.hangup(id, targets, room, video, gid, payload)
            }
            CallSessionHolder.outgoing -> {
                if (CallSessionHolder.isMultiCall) {
                    CallSignalSender.end(id, targets, room, video, gid, payload)
                } else if (CallSessionHolder.leaveAsTimeout) {
                    CallSignalSender.cancelForTimeout(id, targets, room, video, null)
                } else {
                    CallSignalSender.cancel(id, targets, room, video, null, null)
                }
            }
        }
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!CallSessionHolder.active && !CallUtil.isRoomConnected()) {
                stopSelf()
                return
            }
            // 主叫振铃超时（Activity 已销毁时由浮窗承接）
            if (CallSessionHolder.active
                && CallSessionHolder.outgoing
                && !CallSessionHolder.isMultiCall
                && !CallSessionHolder.callConnected
            ) {
                val opened = CallSessionHolder.openedAtMs
                if (opened > 0L && System.currentTimeMillis() - opened >= CallActivity.outgoingRingTimeoutMs()) {
                    CallSessionHolder.leaveAsTimeout = true
                    val id = CallSessionHolder.callId.ifBlank { CallSessionHolder.roomName }
                    CallMissedLocalStore.markTimeoutAsCaller(id, CallSessionHolder.targetId)
                    CallMissedLocalStore.markTimeoutAsCaller(CallSessionHolder.roomName, CallSessionHolder.targetId)
                    endCallFromFloat(sendSignal = true)
                    return
                }
            }
            refreshFloatContent()
            startForegroundCompat()
            handler.postDelayed(this, 1000L)
        }
    }

    private fun buildNotification(): Notification {
        resumeActivityIntent = Intent(this, CallDummyActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.getActivity(
                this, 0, resumeActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            @Suppress("DEPRECATION")
            PendingIntent.getActivity(
                this, 0, resumeActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = "$packageName.call.float"
            val channel = NotificationChannel(
                id,
                getString(com.cboda.baselibrary.R.string.call_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
            id
        } else {
            ""
        }
        val title = when {
            !CallSessionHolder.callConnected && CallSessionHolder.outgoing ->
                getString(com.cboda.baselibrary.R.string.call_notification_waiting_peer)
            !CallSessionHolder.callConnected ->
                getString(com.cboda.baselibrary.R.string.call_notification_invite)
            else ->
                getString(com.cboda.baselibrary.R.string.call_notification_in_call)
        }
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.av_float_broad_audio)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showFloatingWindow() {
        if (wm != null) {
            refreshFloatContent()
            return
        }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = PixelFormat.TRANSLUCENT
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
            x = resources.displayMetrics.widthPixels
            y = 0
        }
        view = LayoutInflater.from(this).inflate(R.layout.call_float_view, null).also {
            it.setOnTouchListener(onTouchListener)
            wm?.addView(it, params)
        }
        refreshFloatContent()
    }

    private fun hideFloatBox() {
        val windowManager = wm
        val floatView = view
        if (windowManager != null && floatView != null) {
            try {
                windowManager.removeView(floatView)
            } catch (_: Exception) {
            }
        }
        wm = null
        view = null
    }

    private fun refreshFloatContent() {
        val v = view ?: return
        val timeView = v.findViewById<TextView>(R.id.durationTextView) ?: return
        val mediaIcon = v.findViewById<ImageView>(R.id.av_media_type)
        mediaIcon?.setImageResource(R.drawable.av_float_broad_audio)
        if (!CallSessionHolder.callConnected) {
            timeView.setText(com.cboda.baselibrary.R.string.call_float_waiting_answer)
            return
        }
        var duration = (System.currentTimeMillis() - CallSessionHolder.connectedAtMs) / 1000
        if (duration < 0) duration = 0
        timeView.text = if (duration >= 3600) {
            String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60)
        } else {
            String.format("%02d:%02d", (duration % 3600) / 60, duration % 60)
        }
    }

    private fun clickToResume() {
        showFloatingWindow = false
        val intent = resumeActivityIntent ?: Intent(this, CallDummyActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private val onTouchListener = object : View.OnTouchListener {
        private var lastX = 0f
        private var lastY = 0f
        private var oldOffsetX = 0
        private var oldOffsetY = 0
        private var tag = 0

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val layoutParams = params ?: return false
            val windowManager = wm ?: return false
            val x = event.x
            val y = event.y
            if (tag == 0) {
                oldOffsetX = layoutParams.x
                oldOffsetY = layoutParams.y
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = x
                    lastY = y
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x += ((x - lastX) / 3).toInt()
                    layoutParams.y += ((y - lastY) / 3).toInt()
                    tag = 1
                    windowManager.updateViewLayout(v, layoutParams)
                }
                MotionEvent.ACTION_UP -> {
                    if (kotlin.math.abs(oldOffsetX - layoutParams.x) <= 20
                        && kotlin.math.abs(oldOffsetY - layoutParams.y) <= 20
                    ) {
                        clickToResume()
                    } else {
                        tag = 0
                    }
                }
            }
            return true
        }
    }

    companion object {
        private const val TAG = "CallFloatService"
        private const val NOTIFICATION_ID = 0xC011
        const val EXTRA_SHOW_FLOATING = "showFloatingView"

        @JvmStatic
        fun start(context: Context, showFloatingView: Boolean) {
            val intent = Intent(context, CallFloatService::class.java).apply {
                putExtra(EXTRA_SHOW_FLOATING, showFloatingView)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun stop(context: Context) {
            context.stopService(Intent(context, CallFloatService::class.java))
        }
    }
}
