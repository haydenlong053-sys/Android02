package com.cboda.baselibrary.utils;


import static androidx.lifecycle.Lifecycle.State.DESTROYED;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import com.blankj.utilcode.util.ThreadUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BusLiveBroadData<T> extends MutableLiveData<T> {
    private static final int START_VERSION = -1;
    private String mKey;
    private int mVersion;

    private Map<Observer<? super T>, BaseBusObserverBroadWrapper<? super T>> mMap = new ConcurrentHashMap<>();

    BusLiveBroadData(String key) {
        mKey = key;
        mVersion = START_VERSION;
    }

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        BaseBusObserverBroadWrapper<? super T> observerWrapper = mMap.get(observer);
        if (observerWrapper == null) {
            observerWrapper = new BusLifecycleBroadObserver<>(observer, this, owner);
            mMap.put(observer, observerWrapper);
            owner.getLifecycle().addObserver((LifecycleObserver) observerWrapper);
        }
        super.observe(owner, observerWrapper);
    }

    @MainThread
    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        BaseBusObserverBroadWrapper<? super T> observerWrapper = mMap.get(observer);
        if (observerWrapper == null) {
            observerWrapper = new BusForeverBroadObserver<>(observer, this);
            mMap.put(observer, observerWrapper);
        }
        super.observeForever(observerWrapper);
    }

    @MainThread
    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        BaseBusObserverBroadWrapper<? super T> observerWrapper = mMap.remove(observer);
        if (observerWrapper != null) {
            super.removeObserver(observerWrapper);
        } else {
            super.removeObserver(observer);
        }
    }

    @MainThread
    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        for (Observer<? super T> observer : mMap.keySet()) {
            BaseBusObserverBroadWrapper<? super T> observerWrapper = mMap.get(observer);
            if (observerWrapper != null && observerWrapper.isAttachedTo(owner)) {
                mMap.remove(observer);
            }
        }
        super.removeObservers(owner);
    }

    @MainThread
    public void stickyObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        super.observe(owner, observer);
    }

    @MainThread
    public void stickyForeverObserver(@NonNull Observer<? super T> observer) {
        super.observeForever(observer);
    }

    @Override
    public void postValue(T value) {
        ThreadUtils.runOnUiThread(() -> setValue(value));
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (!hasObservers()) {
            LiveDataBroadBus.getInstance().mHashMap.remove(mKey);
        }
    }

    @MainThread
    @Override
    public void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }

    int getVersion() {
        return mVersion;
    }
}
