package com.freddy.webtrc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CallUtil {
    private const val tAG = "CallUtilClass"

    // scope: CoroutineScope
    @SuppressLint("StaticFieldLeak")
    private var callRoom: Room? = null

    private fun getCallRoom(context: Context? = null): Room? {
        try {
            if (callRoom == null) {
                callRoom = createRoom(context)
            }
        } catch (e: Exception) {
            e.toLog(tAG)
        }
        return callRoom
    }

    fun initCall(context: Context) {
//        getCallRoom(context)
    }

    private fun createRoom(context: Context? = null): Room {
//        return LiveKit.create(getAppContext(context))
        return LiveKit.create(UtilBroadApplication.cxt)
    }


    private suspend fun joinRoom(url: String?, token: String?, context: Context? = null) {
        try {
            val room = getCallRoom(context) ?: return
            room.connect(url ?: "", token ?: "")
            room.localParticipant.setMicrophoneEnabled(true)
        } catch (e1: Exception) {
            e1.toLog(tAG)
        }
    }

    fun hasRemoteParticipant(context: Context? = null): Boolean {
        return !getCallRoom(context)?.remoteParticipants.isNullOrEmpty()
    }

    fun isRoomConnected(context: Context? = null): Boolean {
        return try {
            getCallRoom(context)?.state == Room.State.CONNECTED
        } catch (e: Exception) {
            e.toLog(tAG)
            false
        }
    }

    /**
     * LiveKit full reconnect 期间会临时清掉远端并抛出 [RoomEvent.ParticipantDisconnected]，
     * 业务侧据此挂断会造成「断网立刻结束通话」；重连中应忽略该事件。
     */
    fun isRoomReconnecting(context: Context? = null): Boolean {
        return try {
            getCallRoom(context)?.state == Room.State.RECONNECTING
        } catch (e: Exception) {
            e.toLog(tAG)
            false
        }
    }

    /** 供悬浮窗 Service 等独立协程监听房间事件。 */
    suspend fun observeRoomEvents(context: Context? = null, callBack: (RoomEvent) -> Unit) {
        setCallListener(context, callBack)
    }

    fun remoteParticipantIdentities(context: Context? = null): List<String> {
        return getCallRoom(context)?.remoteParticipants?.values
            ?.mapNotNull { it.identity?.value?.takeIf { id -> id.isNotBlank() } }
            ?: emptyList()
    }

    // 创建/加入通话
    fun joinRoom(url: String?, token: String?, scope: CoroutineScope? = null, context: Context? = null) {
        try {
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(token)) {
                scope?.launch {
                    withContext(Dispatchers.IO) {
                        joinRoom(url, token, context)
                    }
                } ?: GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        joinRoom(url, token, context)
                    }
                }
            }
        } catch (e2: Exception) {
            e2.toLog(tAG)
        }
    }

    //静音/打开麦克风
    fun openOrCloseMicrophone(boolean: Boolean = false, scope: CoroutineScope? = null, context: Context? = null) {
        scope?.launch {
            withContext(Dispatchers.IO) {
                openOrCloseMicrophone(boolean, context)
            }
        } ?: GlobalScope.launch {
            withContext(Dispatchers.IO) {
                openOrCloseMicrophone(boolean, context)
            }
        }
    }


    private suspend fun openOrCloseMicrophone(boolean: Boolean = false, context: Context? = null) {
        try {
            getCallRoom(context)?.localParticipant?.setMicrophoneEnabled(boolean)
        } catch (e: Exception) {
            e.toLog(tAG)
        }
    }

    //离开通话房间/结束通话
    fun leaveRoom(context: Context? = null) {
        try {
            getCallRoom(context)?.disconnect()
        } catch (e: Exception) {
            e.toLog(tAG)
        }
    }

    private suspend fun setCallListener(context: Context? = null, callBack: (RoomEvent) -> Unit) {
        getCallRoom(context)?.events?.events?.collect { event ->
            when (event) {
                is RoomEvent.ParticipantConnected -> {
                    // 有⼈进来 → 视为接通
                    callBack.invoke(event)
                }

                is RoomEvent.ParticipantDisconnected -> {
                    callBack.invoke(event)
                }

                is RoomEvent.TrackSubscribed -> {
                    // 收到对⽅⾳/视频轨
                    callBack.invoke(event)
                }

                is RoomEvent.TrackUnsubscribed -> {
                    callBack.invoke(event)
                }

                is RoomEvent.ActiveSpeakersChanged -> {
                    //event.speakers 正在说话,做⾼亮
                    callBack.invoke(event)
                }

                is RoomEvent.Disconnected -> {
                    //通话结束,关⻚⾯
                    callBack.invoke(event)
                }

                is RoomEvent.FailedToConnect -> {
                    //连接失败
                    callBack.invoke(event)
                }

                else -> {
                    callBack.invoke(event)
                }
            }
        }
    }

    //通话监听
    fun setCallListener(scope: CoroutineScope? = null, context: Context? = null, callBack: (RoomEvent) -> Unit) {
        scope?.launch {
            withContext(Dispatchers.IO) {
                setCallListener(context, callBack)
            }
        } ?: GlobalScope.launch {
            withContext(Dispatchers.IO) {
                setCallListener(context, callBack)
            }
        }
    }

    //获取通话的人员信息
    fun getCallPeopleList(context: Context? = null) {
        val me = getCallRoom(context)?.localParticipant// 本地
        val remotes = getCallRoom(context)?.remoteParticipants?.values // 远端

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