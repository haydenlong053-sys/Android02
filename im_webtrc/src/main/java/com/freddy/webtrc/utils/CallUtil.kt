package com.freddy.webtrc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.utils.getAppContext
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CallUtil {

    @SuppressLint("StaticFieldLeak")
    private var callRoom: Room? = null

    private fun getCallRoom(context: Context? = null): Room? {
        if (callRoom == null) {
            callRoom = createRoom(context)
        }
        return callRoom
    }

    private fun createRoom(context: Context? = null): Room {
        return LiveKit.create(getAppContext(context))
    }

    // scope: CoroutineScope
    fun joinRoom(url: String?, token: String?, context: Context? = null) {
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(token)) {
            GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        getCallRoom(context)?.connect(url ?: "", token ?: "")
                    }
                }
            }
        }

        fun openOrCloseMicrophone(boolean: Boolean = false, context: Context? = null) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    getCallRoom(context)?.localParticipant?.setMicrophoneEnabled(boolean)
                }
            }
        }

        fun leaveRoom(context: Context? = null) {
            getCallRoom(context)?.disconnect()
        }


//    lifecycleScope.launch {
//        room.events.collect { event ->
//            when (event) {
//                is RoomEvent.ParticipantConnected -> refreshRoster() // 有⼈进来 → 视为接通
//                is RoomEvent.ParticipantDisconnected -> refreshRoster()
//                is RoomEvent.TrackSubscribed -> refreshRoster() // 收到对⽅⾳/视频轨
//                is RoomEvent.TrackUnsubscribed -> refreshRoster()
//                is RoomEvent.ActiveSpeakersChanged -> { /* event.speakers 正在说话,做⾼亮 */ }
//                is RoomEvent.Disconnected -> { /* 通话结束,关⻚⾯ */ }
//                is RoomEvent.FailedToConnect -> { /* 连接失败 */ }
//                else -> {}
//            }
//        }
//    }
//    fun refreshRoster() {
//        val me = room.localParticipant // 本地
//        val remotes = room.remoteParticipants.values // 远端
//        for (p in listOf(me) + remotes) {
//            val id = p.identity?.value ?: ""
//            val name = p.name
//            val speaking = p.isSpeaking
//// → 刷新你的参与者列表 UI
//        }
//    }


    }