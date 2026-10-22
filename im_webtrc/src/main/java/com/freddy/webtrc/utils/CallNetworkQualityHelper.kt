package com.freddy.webtrc.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.freddy.webtrc.floatcall.CallSessionHolder
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.participant.LocalParticipant

/**
 * LiveKit 通话网络质量 / 断网提示：
 * - [ConnectionQualityChanged]：本端/对端弱网
 * - [RoomEvent.Reconnecting] / 系统断网：本端「当前通话你的网络不佳」
 *
 * 说明：Android LiveKit 在断网时未必立刻抛 LOST，常在恢复后才 Reconnecting；
 * 因此额外监听 ConnectivityManager，断网时立即提示。
 */
object CallNetworkQualityHelper {

    private const val TAG = "CallNetworkQuality"
    private const val THROTTLE_MS = 3_000L
    /** 网络恢复后短暂等待，再判断房间是否仍连通（避免误取消补偿）。 */
    private const val NETWORK_RESTORE_CHECK_DELAY_MS = 1_500L

    private val mainHandler = Handler(Looper.getMainLooper())
    private val networkRestoreCheckRunnable = Runnable {
        CallSessionHolder.onLocalNetworkRestoredCheck()
    }

    @Volatile
    private var callConnected = false

    @Volatile
    private var watchingNetwork = false

    @Volatile
    private var lastLocalToastAt = 0L

    @Volatile
    private var lastRemoteToastAt = 0L

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun onCallConnected() {
        callConnected = true
        startNetworkWatch()
    }

    fun reset() {
        callConnected = false
        mainHandler.removeCallbacks(networkRestoreCheckRunnable)
        stopNetworkWatch()
        lastLocalToastAt = 0L
        lastRemoteToastAt = 0L
    }

    /** LiveKit 进入重连：视为本端网络问题。 */
    fun onLocalReconnecting() {
        toastLocalPoor("reconnecting")
    }

    /**
     * 对端网络异常（质量 LOST、对端离房、对端 DISCONNECTED 状态等）。
     * 须在挂断/finish 之前调用，否则 Toast 可能随页面销毁而看不到。
     */
    fun onRemoteNetworkIssue(reason: String) {
        toastRemotePoor(reason)
    }

    /**
     * @param callConnectedArg 兼容旧调用；以内部 [callConnected] 为准。
     */
    fun handle(event: RoomEvent.ConnectionQualityChanged, callConnectedArg: Boolean = callConnected) {
        if (!callConnected && !callConnectedArg) {
            return
        }
        if (event.quality != ConnectionQuality.POOR && event.quality != ConnectionQuality.LOST) {
            return
        }
        val isLocal = event.participant is LocalParticipant
                || event.participant === event.room.localParticipant
        if (isLocal) {
            toastLocalPoor("quality=${event.quality}")
        } else {
            toastRemotePoor("quality=${event.quality}")
        }
    }

    private fun toastLocalPoor(reason: String) {
        if (!callConnected) {
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastLocalToastAt < THROTTLE_MS) {
            return
        }
        lastLocalToastAt = now
        eLogD(TAG, "toast local poor: $reason")
        postToast(com.cboda.baselibrary.R.string.call_network_poor_self)
    }

    private fun toastRemotePoor(reason: String) {
        if (!callConnected) {
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastRemoteToastAt < THROTTLE_MS) {
            return
        }
        lastRemoteToastAt = now
        eLogD(TAG, "toast remote poor: $reason")
        postToast(com.cboda.baselibrary.R.string.call_network_poor_peer)
    }

    private fun postToast(resId: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastBroadUtils.show(resId)
        } else {
            mainHandler.post { ToastBroadUtils.show(resId) }
        }
    }

    @Synchronized
    private fun startNetworkWatch() {
        if (watchingNetwork) {
            return
        }
        val app = try {
            UtilBroadApplication.cxt
        } catch (_: Exception) {
            null
        } ?: return
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                eLogD(TAG, "system network onLost")
                mainHandler.removeCallbacks(networkRestoreCheckRunnable)
                toastLocalPoor("system-onLost")
                // LiveKit 断网时往往不立刻 Reconnecting，须在此启动 30s 超时挂断
                CallSessionHolder.onLocalNetworkLost()
            }

            override fun onUnavailable() {
                eLogD(TAG, "system network onUnavailable")
                mainHandler.removeCallbacks(networkRestoreCheckRunnable)
                toastLocalPoor("system-onUnavailable")
                CallSessionHolder.onLocalNetworkLost()
            }

            override fun onAvailable(network: Network) {
                eLogD(TAG, "system network onAvailable，${NETWORK_RESTORE_CHECK_DELAY_MS}ms 后探测房间态")
                mainHandler.removeCallbacks(networkRestoreCheckRunnable)
                mainHandler.postDelayed(
                    networkRestoreCheckRunnable,
                    NETWORK_RESTORE_CHECK_DELAY_MS
                )
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(cb)
            } else {
                cm.registerNetworkCallback(
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(),
                    cb,
                )
            }
            networkCallback = cb
            watchingNetwork = true
            eLogD(TAG, "network watch started")
        } catch (e: Exception) {
            eLogD(TAG, "startNetworkWatch failed: ${e.message}")
            networkCallback = null
            watchingNetwork = false
        }
    }

    @Synchronized
    private fun stopNetworkWatch() {
        if (!watchingNetwork) {
            networkCallback = null
            return
        }
        val cb = networkCallback
        networkCallback = null
        watchingNetwork = false
        val app = try {
            UtilBroadApplication.cxt
        } catch (_: Exception) {
            null
        } ?: return
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        if (cb != null) {
            try {
                cm.unregisterNetworkCallback(cb)
            } catch (_: Exception) {
            }
        }
        eLogD(TAG, "network watch stopped")
    }
}
