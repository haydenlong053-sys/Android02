package com.cboda.chatui.quickmeet.bean

import java.io.Serializable

class CallData : Serializable{
    constructor()

    var callStatus: CallStatus = CallStatus.IDLE
    /**
     * 说话方或接收方
     */
    var direct: CallDirect? = null
    var role: MeetRole? = null
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
     * 说话方(主播)
     */
    CALLER,

    /**
     * 接听方(观众)
     */
    ANSWER
}

/**
 * 会议角色(目前逻辑只用到了OWNER，其他留作拓展)
 */
enum class MeetRole {
    /**
     * 会议的房主
     */
    OWNER,

    /**
     * 管理员
     */
    MANAGER,
    /**
     * 普通人
     */
    NORMAL,
}

/**
 * 会议座次
 */
enum class SeatNumb {
    /**
     * 主麦
     */
    MIC_MAIN,

    /**
     * 副麦
     */
    MIC_SUB,
    /**
     * 嘉宾1
     */
    MIC_GUEST_ONE,
    /**
     * 嘉宾2
     */
    MIC_GUEST_TWO,
}


