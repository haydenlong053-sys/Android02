/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cboda.chatui.common.AppScopeViewBroadModel;

public class StatusNotificationViewBroadModel extends ViewModel implements AppScopeViewBroadModel {
    private MutableLiveData<Object> statusNotificationLiveData;
    private List<StatusBroadNotification> notificationItems;

    public LiveData<Object> statusNotificationLiveData() {
        if (statusNotificationLiveData == null) {
            statusNotificationLiveData = new MutableLiveData<>();
        }
        return statusNotificationLiveData;
    }

    public List<StatusBroadNotification> getNotificationItems() {
        return new ArrayList<>(notificationItems);
    }

    public void showStatusNotification(StatusBroadNotification notification) {
        if (notificationItems == null) {
            notificationItems = new ArrayList<>();
        }
        if (!notificationItems.contains(notification)) {
            notificationItems.add(notification);
        } else {
            notificationItems.set(notificationItems.indexOf(notification), notification);
        }

        if (statusNotificationLiveData != null) {
            statusNotificationLiveData.postValue(new Object());
        }
    }

    public void clearStatusNotificationByType(Class<? extends StatusBroadNotification> statusNotificationClass) {
        if (notificationItems == null || notificationItems.isEmpty()) {
            return;
        }
        Iterator<StatusBroadNotification> iterator = notificationItems.iterator();
        boolean cleared = false;
        while (iterator.hasNext()) {
            if (iterator.next().getClass().equals(statusNotificationClass)) {
                iterator.remove();
                cleared = true;
            }
        }
        if (cleared) {
            if (statusNotificationLiveData != null) {
                statusNotificationLiveData.postValue(new Object());
            }
        }
    }

    public void hideStatusNotification(StatusBroadNotification notification) {
        if (notificationItems == null || notificationItems.isEmpty() || !notificationItems.contains(notification)) {
            return;
        }
        notificationItems.remove(notification);
        if (statusNotificationLiveData != null) {
            statusNotificationLiveData.postValue(new Object());
        }
    }
}
