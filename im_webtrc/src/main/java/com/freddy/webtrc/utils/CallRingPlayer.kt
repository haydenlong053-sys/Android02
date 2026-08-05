package com.freddy.webtrc.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.cboda.baselibrary.utils.eLogD
import com.freddy.webtrc.R

/**
 * 自研通话振铃：对齐野火 [com.cboda.chatui.voip.AsyncBroadPlayer] /
 * [com.cboda.chatui.WfcUIBroadKit.shouldStartRing]。
 *
 * - 来电：`incoming_call_broad_ring` + 循环震动
 * - 去电：`outgoing_call_broad_ring`
 * - 进程级单例，缩小悬浮窗时 Activity 销毁也不中断振铃
 */
object CallRingPlayer {

    private const val TAG = "CallRingPlayer"

    /** 来电震动：震 1s / 停 1s，循环（仿系统来电节奏）。 */
    private val INCOMING_VIBRATE_PATTERN = longArrayOf(0L, 1000L, 1000L)

    @Volatile
    private var playing = false

    private var appContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var savedAudioMode = AudioManager.MODE_NORMAL

    @Synchronized
    fun start(context: Context, incoming: Boolean) {
        val app = context.applicationContext
        if (playing) {
            // 已在振铃（如从悬浮窗恢复）：保持当前播放
            return
        }
        stopInternal()
        appContext = app
        val resId = if (incoming) {
            R.raw.incoming_call_broad_ring
        } else {
            R.raw.outgoing_call_broad_ring
        }
        val uri = Uri.parse("android.resource://${app.packageName}/$resId")
        try {
            val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            savedAudioMode = audioManager.mode
            audioManager.mode = AudioManager.MODE_NORMAL

            val player = MediaPlayer()
            @Suppress("DEPRECATION")
            player.setAudioStreamType(AudioManager.STREAM_RING)
            player.setDataSource(app, uri)
            player.isLooping = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            player.setOnErrorListener { _, what, extra ->
                eLogD(TAG, "play ring error what=$what extra=$extra")
                false
            }
            player.prepare()
            player.start()
            mediaPlayer = player
            playing = true
            if (incoming) {
                startVibrate(app)
            }
            eLogD(TAG, "start ring incoming=$incoming")
        } catch (e: Exception) {
            eLogD(TAG, "start ring failed: ${e.message}")
            stopInternal()
        }
    }

    @Synchronized
    fun stop() {
        if (!playing && mediaPlayer == null && vibrator == null) {
            return
        }
        eLogD(TAG, "stop ring")
        stopInternal()
    }

    private fun stopInternal() {
        playing = false
        try {
            mediaPlayer?.run {
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        mediaPlayer = null
        stopVibrate()
        try {
            val ctx = appContext
            if (ctx != null) {
                val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.mode = savedAudioMode
            }
        } catch (_: Exception) {
        }
        appContext = null
    }

    private fun startVibrate(context: Context) {
        val vib = resolveVibrator(context) ?: return
        if (!vib.hasVibrator()) return
        vibrator = vib
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(INCOMING_VIBRATE_PATTERN, 0))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(INCOMING_VIBRATE_PATTERN, 0)
            }
        } catch (e: Exception) {
            eLogD(TAG, "start vibrate failed: ${e.message}")
        }
    }

    private fun stopVibrate() {
        try {
            vibrator?.cancel()
        } catch (_: Exception) {
        }
        vibrator = null
    }

    private fun resolveVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
