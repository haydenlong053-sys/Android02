package com.cboda.chatui.quickmeet

import com.cboda.chatui.quickmeet.bean.CallData
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.IRtcEngineEventHandler.AudioVolumeInfo

interface CallEventCallback{

    /**
     * 初始化通话
     */
    fun onEngineSetup() {

    }

    /**
     * 结束会议
     * @param data CallData 当前通话数据对象
     */
    fun onEngineDestroy(data: CallData) {

    }

    /**
     * 有人加入频道
     * @param uid
     */
    fun onRtcUserJoined(uid: Int) {

    }

    /**
     * 有人离开频道（远端用户或主播离开频道时触发的回调。）
     * @param uid
     * reason
     * 远端用户（在通信场景中）或主播（在直播场景中）离线的原因：
     * USER_OFFLINE_QUIT (0)：用户主动退出通话，发送离开消息。
     * USER_OFFLINE_DROPPED (1)：SDK 超时未收到任何数据包，用户掉线。网络连接不佳可能导致误判，建议使用声网 RTM SDK 进行可靠的离线检测。
     * USER_OFFLINE_BECOME_AUDIENCE (2)：用户将角色从主播切换为观众。
     */
    fun onRtcUserOffline(uid: Int,reason: Int) {

    }

    fun onRtcJoinChannelSuccess() {

    }

    fun onPermissionDenied() {

    }

    /**
     * 用户角色或观众延迟级别发生变化回调
     *
     * oldRole
     * 用户切换前的角色：
     * CLIENT_ROLE_BROADCASTER (1)：主播。
     * CLIENT_ROLE_AUDIENCE (2)：观众。
     * newRole
     * 用户切换后的角色：
     * CLIENT_ROLE_BROADCASTER (1)：主播。
     * CLIENT_ROLE_AUDIENCE (2)：观众。
     *
     */
    fun onRtcRoleChanged(oldRole: Int,newRole: Int) {

    }

    /**
     * 报告当前通话统计信息的回调
     * 该回调在用户加入频道后每两秒触发一次。
     */
    fun onRtcStats(statsBean: IRtcEngineEventHandler.RtcStats?) {

    }

    /**
     * 报告用户音量信息的回调。
     * totalVolume
     * 说话者的音量，取值范围为 [0, 255]。
     * 对于本地用户的回调，totalVolume 是发送音频流的本地用户的音量。
     * 对于远端用户的回调，totalVolume 是瞬时音量最高的最多三位远端用户的音量总和。
     * 如果调用了 startAudioMixing，则 totalVolume 是混音后的音量。
     */
    fun onRoomAudioVolumeIndication(speakers: Array<AudioVolumeInfo>?,totalVolume: Int){

    }

    /**
     * uid
     * 远端用户 ID。
     * muted
     * 远端用户的音频流是否被静音：
     * true：音频流被静音。
     * false：音频流未被静音。
     */
    fun onRoomUserMuteAudio(uid: Int,muted : Boolean){

    }
    fun onRtcConnectionStateChanged(state: Int, reason: Int){

    }

}