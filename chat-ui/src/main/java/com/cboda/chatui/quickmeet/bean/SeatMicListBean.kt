package com.cboda.chatui.quickmeet.bean

import com.cboda.baselibrary.bean.MeetUser

data class SeatMicListBean(
    var adminUserId: String? = null,
    var speakingList: MutableList<MeetUser>? = null,
    var groupIdNo: String? = null
)