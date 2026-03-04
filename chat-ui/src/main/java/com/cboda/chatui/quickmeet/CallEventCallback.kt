package com.cboda.chatui.quickmeet

import com.cboda.chatui.quickmeet.bean.CallData

interface CallEventCallback {

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
     * 对方加入频道
     * @param uid Long
     */
    fun onRtcUserJoined(uid: Int) {

    }

    fun onRtcJoinChannelSuccess() {

    }


    /**
     * 加入rtc 频道超时失败
     */
    fun onConnectRtcFail() {

    }

    /**
     * 声网rtc token 无效或过期
     */
    fun onRtcTokenExpired() {

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

}