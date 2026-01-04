package com.cboda.baselibrary.event.rtc

data class NetworkQualityEventBroadData(
    val uid:String,
    val txQuality:Int,
    val rxQuality:Int
)
