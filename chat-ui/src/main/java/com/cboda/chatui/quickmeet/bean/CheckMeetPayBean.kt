package com.cboda.chatui.quickmeet.bean

import java.io.Serializable

class CheckMeetPayBean : Serializable {
    var paidUnused: CheckMeetBean? = null
    var pausedMeeting: CheckMeetBean? = null
    var activeMeeting: CheckMeetBean? = null
    var hasReminder: Boolean = false
    var reminderType = ""

    class CheckMeetBean : Serializable {
        var amountUsdt: String = ""
        var channelName = ""
        var channelId = ""
        var durationConfigId: Int = -1
        var expireTime = ""
        var groupIdNo = ""
        var has: Boolean = false
        var orderId = ""
        var payTime = ""
        var userTierConfigId: Int = -1
        var remainSeconds: Long = 0
    }

//    "amountUsdt": 10,
//    "durationConfigId": 1,
//    "expireTime": "2026-06-26 19:30:40",
//    "groupIdNo": "67c4838fd79746a18fb80825626ac3c9",
//    "has": true,
//    "orderId": "1782473140572.000886688",
//    "payTime": "2026-06-26 19:25:40",
//    "remainSeconds": 267,
//    "userTierConfigId":1
}