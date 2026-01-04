package com.cboda.baselibrary.utils;

import androidx.lifecycle.Observer;

class BusForeverBroadObserver<T> extends BaseBusObserverBroadWrapper<T> {

    BusForeverBroadObserver(Observer<? super T> observer, BusLiveBroadData<T> liveData) {
        super(observer, liveData);
    }
}
