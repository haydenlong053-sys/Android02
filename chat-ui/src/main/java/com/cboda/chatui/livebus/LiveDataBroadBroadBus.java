/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.livebus;


import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;

public final class LiveDataBroadBroadBus {

    private static Map<String, EventLiveBroadData> sSubjectMap = new HashMap<>();

    private LiveDataBroadBroadBus() {
    }

    @NonNull
    private static synchronized EventLiveBroadData getLiveData(String subject) {
        EventLiveBroadData liveData = sSubjectMap.get(subject);
        if (liveData == null) {
            liveData = new EventLiveBroadData(subject);
            sSubjectMap.put(subject, liveData);
        }

        return liveData;
    }

    public static void subscribe(String subject, @NonNull LifecycleOwner lifecycle, @NonNull Observer<Object> action) {
        getLiveData(subject).observe(lifecycle, action);
    }

    static void unregister(String subject) {
        sSubjectMap.remove(subject);
        eLogD("LiveDataBroadBroadBus", "remove subject " + subject);
    }

    /**
     * pls refer to 
     *
     * @param subject
     * @param message
     */
    public static void postValue(String subject, @NonNull Object message) {
        getLiveData(subject).update(message);
    }

    public static void setValue(String subject, @NonNull Object message) {
        getLiveData(subject).update(message);
    }
}