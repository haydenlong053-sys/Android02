package com.cboda.baselibrary.utils

import com.cboda.baselibrary.bean.BarrageBean
import com.cboda.baselibrary.bean.CommonResultBroadBean.CommonResult

val clearUserDataLiveData: BusLiveBroadData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("ClearUserDataEventName")
}

val updateBarrageDataLiveData: BusLiveBroadData<BarrageBean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("UpdateBarrageData")
}

val updateGroupDetailLiveData: BusLiveBroadData<CommonResult> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("UpdateGroupDetail")
}

val meetJoinMsgLiveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("meetJoinMsg")
}