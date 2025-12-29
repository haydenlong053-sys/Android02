package com.base.commonlibrary.event.rtc

data class NetworkQualityEventData(
    val uid:String,
    val txQuality:Int,
    val rxQuality:Int
)
