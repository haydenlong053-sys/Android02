package com.base.commonlibrary.utils;




import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;


public class BaseBusObserverWrapper<T> implements Observer<T> {

    Observer<? super T> mObserver;

    BusLiveData<T> mLiveData;

    private int mLastVersion;

    BaseBusObserverWrapper(Observer<? super T> observer, BusLiveData<T> liveData) {
        mObserver = observer;
        mLiveData = liveData;
        mLastVersion = mLiveData.getVersion();
    }

    @Override
    public void onChanged(T t) {
        if (mLastVersion >= mLiveData.getVersion()) {
            return;
        }
        mLastVersion = mLiveData.getVersion();
        try {
            mObserver.onChanged(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAttachedTo(LifecycleOwner owner) {
        return false;
    }
}
