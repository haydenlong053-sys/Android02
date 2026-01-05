/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.livebus;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class EventLiveBroadData extends LiveData<Object> {

    private final String mSubject;

    public EventLiveBroadData(String subject) {
        mSubject = subject;
    }

    public void update(Object object) {
        postValue(object);
    }

    @Override
    public void removeObserver(@NonNull Observer<? super Object> observer) {
        super.removeObserver(observer);
        if (!hasObservers()) {
            LiveDataBroadBroadBus.unregister(mSubject);
        }
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        super.removeObservers(owner);
        if (!hasObservers()) {
            LiveDataBroadBroadBus.unregister(mSubject);
        }
    }

}
