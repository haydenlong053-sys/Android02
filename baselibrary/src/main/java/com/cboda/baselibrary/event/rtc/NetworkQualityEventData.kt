package com.cboda.baselibrary.event.rtc

data class NetworkQualityEventData(
    val uid:String,
    val txQuality:Int,
    val rxQuality:Int
)
