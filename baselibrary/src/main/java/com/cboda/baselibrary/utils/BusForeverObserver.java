package com.cboda.baselibrary.utils;

import androidx.lifecycle.Observer;

class BusForeverObserver<T> extends BaseBusObserverWrapper<T> {

    BusForeverObserver(Observer<? super T> observer, BusLiveData<T> liveData) {
        super(observer, liveData);
    }
}
