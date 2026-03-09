package com.cboda.chatui.quickmeet

import com.cboda.chatui.quickmeet.bean.CallData

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
     * @param uid Long
     */
    fun onRtcUserJoined(uid: Int) {

    }

    fun onRtcJoinChannelSuccess() {

    }

    /**
     * 声网rtc token 无效或过期
     */
    fun onRtcTokenFail(reason: Int) {

    }

    fun onPermissionDenied() {

    }

    /**
     * 挂断 激活倒计时 事件
     * @param time Long
     */
    fun onHangupCountdown(time: Long) {

    }

    /**
     * 取消挂断倒计时
     */
    fun onCancelHangupCountdown() {

    }

    /**
     * 报告用户音量信息的回调。
     * totalVolume
     * 说话者的音量，取值范围为 [0, 255]。
     * 对于本地用户的回调，totalVolume 是发送音频流的本地用户的音量。
     * 对于远端用户的回调，totalVolume 是瞬时音量最高的最多三位远端用户的音量总和。
     * 如果调用了 startAudioMixing，则 totalVolume 是混音后的音量。
     */
    fun onRoomAudioVolumeIndication(totalVolume: Int){

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

}