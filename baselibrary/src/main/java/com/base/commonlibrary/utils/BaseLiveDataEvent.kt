package com.base.commonlibrary.utils

val clearUserDataLiveData: BusLiveData<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
    LiveDataBus.getInstance().with("ClearUserDataEventName")
}
