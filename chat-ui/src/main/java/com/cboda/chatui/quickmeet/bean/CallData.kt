package com.cboda.chatui.quickmeet.bean

import java.io.Serializable

class CallData : Serializable{
    constructor()

    var callStatus: CallStatus = CallStatus.IDLE
    /**
     * 说话方或接收方
     */
    var direct: CallDirect? = null
    var channelId: String = ""
    var quickDetailMeetBean:QuickDetailMeetBean?=null


}


/**
 * 通话状态
 */
enum class CallStatus {
    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 等待接听
     */
    WAITING,

    /**
     * 会议中
     */
    CALLING,
}


/**
 * 会议发言方、接听方
 */
enum class CallDirect {
    /**
     * 说话方
     */
    CALLER,

    /**
     * 接听方
     */
    ANSWER
}

