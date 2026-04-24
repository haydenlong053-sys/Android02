package com.cboda.baselibrary.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MsgBroadExtra(
    val grade: Double = 0.0,//信用分
    val joinGroupTime: String = "",//入群时间
    val isMeetingImage: String = "0",//是否会议大图
    val rDay: String = "",//注册天数
    val isVip: String = "0",//是否vip
    val isNice: String = "0"//是否靓号
) : Parcelable