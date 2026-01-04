package com.cboda.baselibrary.utils;



import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;


public class LiveDataBroadBus {

    final ConcurrentHashMap<String, BusLiveBroadData<?>> mHashMap = new ConcurrentHashMap<>(10);

    private LiveDataBroadBus() {
        //no instance
    }

    private static final class BroadHolder {
        final static LiveDataBroadBus INSTANCE = new LiveDataBroadBus();
    }

    public static LiveDataBroadBus getInstance() {
        return BroadHolder.INSTANCE;
    }

    @NonNull
    public <T> BusLiveBroadData<T> with(String eventName) {
        BusLiveBroadData<T> liveData = (BusLiveBroadData<T>) mHashMap.get(eventName);
        if (liveData == null) {
            liveData = new BusLiveBroadData<>(eventName);
            mHashMap.put(eventName, liveData);
        }
        return liveData;
    }
}
