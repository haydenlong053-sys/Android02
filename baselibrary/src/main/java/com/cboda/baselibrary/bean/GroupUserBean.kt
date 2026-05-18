package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class GroupUserBean {
    @SerializedName("groupNickname")
    var groupNickName = ""//用户在群内的昵称

    var joinTime = ""//入群时间（"2026-04-23 15:56:33"）

    var joinTimeTimestamp: Long? = null // /入群时间（ 1776930993000)

    var sex = ""//性别

    var groupID = ""

    var icon = ""

    var joinDays = ""

    var userId = ""

    @SerializedName("isvip")
    var isVip = ""

    var grade = ""

    var name = ""

    var isLiang = ""

    var registDay = ""
}