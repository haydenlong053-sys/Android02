package com.base.commonlibrary.event

/**
 * app事件通知的统一类
 */
object AppEventCode {
    //登录成功
    const val  EVENT_LOGIN_SUCCESS = 101



    //会议室rtc相关
    const val EVENT_QUALITY_CHANGE = 201




    //会议室相关

    //弹窗提醒用户是否续费时，用户点击了不续费
    const val  EVENT_MEETING_UN_ADD_TIME = 301

    //弹窗提醒用户是否续费时，用户点击了不续费
    const val  EVENT_MEETING_ADD_TIME=302

    //续费提醒事件
    const val  EVENT_MEETING_ADD_TIP=303

    //页面ui相关
    //刷新联系人页面
    const val EVENT_REFRESH_CONTACT=401
    //新设备登录事件
    const val  EVENT_NEW_MACHINE_CHECK=901


}



data class AppEvent<T>(val eventCode:Int,val data:T?)