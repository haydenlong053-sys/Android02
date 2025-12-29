package com.cboda.baselibrary.utils;



import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;


public class LiveDataBus {

    final ConcurrentHashMap<String, BusLiveData<?>> mHashMap = new ConcurrentHashMap<>(10);

    private LiveDataBus() {
        //no instance
    }

    private static final class Holder {
        final static LiveDataBus INSTANCE = new LiveDataBus();
    }

    public static LiveDataBus getInstance() {
        return Holder.INSTANCE;
    }

    @NonNull
    public <T> BusLiveData<T> with(String eventName) {
        BusLiveData<T> liveData = (BusLiveData<T>) mHashMap.get(eventName);
        if (liveData == null) {
            liveData = new BusLiveData<>(eventName);
            mHashMap.put(eventName, liveData);
        }
        return liveData;
    }
}
