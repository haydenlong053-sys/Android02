package com.freddy.chat.im

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.eLogD

/**
 * 监听系统网络恢复，踢醒自研 IM 重连（对接 [IMSClientBootstrap.notifyNetworkAvailable]）。
 */
object ImNetworkMonitor {

    private const val TAG = "ImNetworkMonitor"
    private var registered = false
    private var callback: ConnectivityManager.NetworkCallback? = null

    @Synchronized
    fun start() {
        if (registered) {
            return
        }
        val app = try {
            UtilBroadApplication.cxt
        } catch (_: Exception) {
            null
        } ?: return
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                eLogD(TAG, "onAvailable → notifyNetworkAvailable")
                IMSClientBootstrap.getInstance().notifyNetworkAvailable()
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    eLogD(TAG, "onCapabilitiesChanged(INTERNET) → notifyNetworkAvailable")
                    IMSClientBootstrap.getInstance().notifyNetworkAvailable()
                }
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
            callback = cb
            registered = true
            eLogD(TAG, "started")
        } catch (e: Exception) {
            eLogD(TAG, "start failed: ${e.message}")
            callback = null
            registered = false
        }
    }

    @Synchronized
    fun stop() {
        if (!registered) {
            return
        }
        val cb = callback
        callback = null
        registered = false
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
        eLogD(TAG, "stopped")
    }
}
