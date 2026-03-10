package com.cboda.baselibrary.utils

import com.cboda.baselibrary.bean.BarrageBean

val clearUserDataLiveData: BusLiveBroadData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("ClearUserDataEventName")
}

val updateBarrageDataLiveData: BusLiveBroadData<BarrageBean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBroadBus.getInstance().with("UpdateBarrageData")
}

