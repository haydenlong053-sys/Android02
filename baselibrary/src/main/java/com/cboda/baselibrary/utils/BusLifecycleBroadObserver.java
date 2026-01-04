package com.cboda.baselibrary.utils;


import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;



public class BusLifecycleBroadObserver<T> extends BaseBusObserverBroadWrapper<T> implements LifecycleObserver {

    private LifecycleOwner mLifecycleOwner;

    BusLifecycleBroadObserver(Observer<? super T> observer, BusLiveBroadData<T> liveData, LifecycleOwner owner) {
        super(observer, liveData);
        mLifecycleOwner = owner;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mLiveData.removeObserver(mObserver);
        mLifecycleOwner.getLifecycle().removeObserver(this);
    }

    @Override
    public boolean isAttachedTo(LifecycleOwner owner) {
        return owner == mLifecycleOwner;
    }
}
