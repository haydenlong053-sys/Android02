package com.cboda.chatui.quickmeet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cboda.baselibrary.utils.eLogD
import com.cboda.chatui.R

class CallService : Service() {
    private val notificationId = android.os.Process.myPid()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        eLogD("call", "onCreate====>")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val createNotification = createNotification()
        startForeground(notificationId, createNotification)
        return super.onStartCommand(intent, flags, startId)
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
//                val intent = if (fromLive) {
//                    val itt = Intent()
//                    itt.setClassName(packageName, ".ui.LiveRoomActivity")
//                    itt
//                } else {
//                    Intent(applicationContext, CallActivity::class.java)
//                }
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                val activity =
//                    PendingIntent.getActivity(applicationContext, 1, intent, PendingIntent.FLAG_IMMUTABLE)
//                this.setContentIntent(activity)
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
//        stopForeground(true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}