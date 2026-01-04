package com.cboda.baselibrary.utils

val clearUserDataLiveData: BusLiveBroadData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBroadBus.getInstance().with("ClearUserDataEventName")
}
