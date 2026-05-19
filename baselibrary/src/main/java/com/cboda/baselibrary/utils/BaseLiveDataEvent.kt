package com.cboda.baselibrary.utils

import com.cboda.baselibrary.bean.BarrageBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean.CommonResult

val clearUserDataLiveData: BusLiveBroadData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("ClearUserDataEventName")
}

val updateBarrageDataLiveData: BusLiveBroadData<BarrageBean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("UpdateBarrageData")
}

//val updateGroupDetailLiveData: BusLiveBroadData<CommonResult> by lazy(LazyThreadSafetyMode.NONE) {
//    LiveDataBroadBroadBus.getInstance().with("UpdateGroupDetail")
//}

//群内有人开启了会议的通知消息
val meetJoinMsgLiveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("meetJoinMsg")
}

val updatePayPwdDataLiveData: BusLiveBroadData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("UpdatePayPwdEventName")
}

//群内有人发起上麦通知消息
val meetUpMicMsgLiveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("meetUpMicMsg")
}

//群内有人会议座次变更通知消息
val meetSeatChangeMsgLiveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("meetSeatChangeMsg")
}

//群内有人结束了会议的通知消息
@JvmField
val meetEndMsgLiveData: BusLiveBroadData<String> = LiveDataBroadBroadBus.getInstance().with("meetEndMsg")


//群数据变更通知
@JvmField
val groupDataLiveData: BusLiveBroadData<CommonResultBroadBean> = LiveDataBroadBroadBus.getInstance().with("groupDataUpdate")