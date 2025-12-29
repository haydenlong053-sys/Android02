package com.cboda.baselibrary.utils

val clearUserDataLiveData: BusLiveData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBus.getInstance().with("ClearUserDataEventName")
}
