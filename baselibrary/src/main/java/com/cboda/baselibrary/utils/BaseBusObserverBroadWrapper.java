package com.cboda.baselibrary.utils;




import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;


public class BaseBusObserverBroadWrapper<T> implements Observer<T> {

    Observer<? super T> mObserver;

    BusLiveBroadData<T> mLiveData;

    private int mLastVersion;

    BaseBusObserverBroadWrapper(Observer<? super T> observer, BusLiveBroadData<T> liveData) {
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
