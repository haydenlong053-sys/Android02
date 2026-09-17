package com.cboda.chatui.quickmeet.bean

import java.io.Serializable

data class QuickDetailMeetBean(
    @JvmField
    var beginTime: String? = null,
    @JvmField
    var channelName: String? = null,
    @JvmField
    var channelId: String? = null,
    @JvmField
    var currentAmount: Double = 0.0,
    @JvmField
    var durationConfigId: Int = 0,
    @JvmField
    var durationConfigValue: Int = 0,
    @JvmField
    var endTime: String? = null,
    @JvmField
    var groupIdNo: String? = null,
    @JvmField
    var id: Int = 0,
    @JvmField
    var idNo: String? = null,
    @JvmField
    var liveAmount: Double = 0.0,
    @JvmField
    var liveStatus: String? = null,
    @JvmField
    var orderNo: String? = null,
    @JvmField
    var remainAmount: Double = 0.0,
    @JvmField
    var token: String? = null,
    @JvmField
    var userId: String? = null,
    @JvmField
    var userTierConfigId: Int = 0,
    @JvmField
    var userTierConfigValue: Int = 0,
    /** LiveKit SFU 地址（api/im/meeting） */
    @JvmField
    var meetingUrl: String? = null,
    /** LiveKit 会议号 */
    @JvmField
    var meetingId: String? = null,
    /** LiveKit 房间名 meet_ 前缀 */
    @JvmField
    var roomName: String? = null,
    /** 本次 token 能否发麦 */
    @JvmField
    var canPublish: Boolean = false,
    /** host / speaker / audience / pending */
    @JvmField
    var myRole: String? = null,
    /** simple / live */
    @JvmField
    var mode: String? = null,
) : Serializable