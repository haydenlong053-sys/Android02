package com.cboda.chatui.quickmeet

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.cboda.baselibrary.utils.eLogD
import com.cboda.chatui.R

class MeetCallService : Service() {
    private val notificationId = android.os.Process.myPid()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        eLogD("call", "onCreate====>")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // targetSdk 34+：microphone 类型 FGS 必须已授予 RECORD_AUDIO，且 startForeground 需带类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            eLogD("call", "MeetCallService stop: RECORD_AUDIO not granted")
            stopSelf()
            return START_NOT_STICKY
        }
        val notification = createNotification()
        try {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            }
            ServiceCompat.startForeground(this, notificationId, notification, fgsType)
        } catch (e: SecurityException) {
            eLogD("call", "startForeground SecurityException: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            eLogD("call", "startForeground failed: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "meet_call_notification_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.chat_meet_service_notification)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(notificationChannelId, channelName, importance)
                .apply {
                    this.description = channelName
                    this.enableLights(false)
                    lightColor = Color.RED
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                    enableVibration(false)
                }
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val content = getString(R.string.chat_meet_service_tip)
        val build = NotificationCompat.Builder(this, notificationChannelId)
            .apply {
                setContentTitle("SeekWeb4")
                setContentText(content)
                setWhen(System.currentTimeMillis())
                setAutoCancel(false)
                setOngoing(true)
                setSmallIcon(com.cboda.baselibrary.R.mipmap.chat_broad_logo)
                setLargeIcon(BitmapFactory.decodeResource(resources, com.cboda.baselibrary.R.mipmap.chat_broad_logo))
                val contentIntent = PendingIntent.getActivity(
                    applicationContext, 0,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setContentIntent(contentIntent)

            }
        return build.build()
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
