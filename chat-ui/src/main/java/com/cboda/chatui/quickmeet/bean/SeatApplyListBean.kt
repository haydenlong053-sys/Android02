package com.cboda.chatui.quickmeet.bean

data class SeatApplyListBean(
    var adminUserId: String? = null,
    var applyList: List<SeatApplyItem>? = null,
    var groupIdNo: String? = null
)