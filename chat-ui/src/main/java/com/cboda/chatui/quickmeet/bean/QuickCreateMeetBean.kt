package com.cboda.chatui.quickmeet.bean

data class QuickCreateMeetBean(
    var beginTime: String? = null,
    var channelName: String? = null,
    var currentAmount: Double = 0.0,
    var durationConfigId: Int = 0,
    var durationConfigValue: Int = 0,
    var endTime: String? = null,
    var groupIdNo: String? = null,
    var id: Int = 0,
    var idNo: String? = null,
    var liveAmount: Double = 0.0,
    var liveStatus: String? = null,
    var orderNo: String? = null,
    var remainAmount: Double = 0.0,
    var token: String? = null,
    var userId: String? = null,
    var userTierConfigId: Int = 0,
    var userTierConfigValue: Int = 0
)