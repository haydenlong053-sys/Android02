/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification;

import androidx.annotation.LayoutRes;

import java.util.HashMap;
import java.util.Map;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.StatusNotificationType;
import com.cboda.chatui.conversationlist.notification.viewholder.ConnectionNotificationViewHolder;
import com.cboda.chatui.conversationlist.notification.viewholder.PCOnlineNotificationViewHolder;
import com.cboda.chatui.conversationlist.notification.viewholder.StatusNotificationViewHolder;

public class StatusNotificationManager {
    private static StatusNotificationManager instance;
    private Map<Class<? extends StatusNotification>, Class<? extends StatusNotificationViewHolder>> notificationViewHolders;
    private Map<Class<? extends StatusNotification>, Integer> notificationViewHolderLayoutResMap;

    public synchronized static StatusNotificationManager getInstance() {
        if (instance == null) {
            instance = new StatusNotificationManager();
        }
        return instance;
    }

    private StatusNotificationManager() {
        init();
    }

    private void init() {
        notificationViewHolders = new HashMap<>();
        notificationViewHolderLayoutResMap = new HashMap<>();
        registerNotificationViewHolder(PCOnlineNotificationViewHolder.class, R.layout.conversationlist_item_notification_pc_online);
        registerNotificationViewHolder(ConnectionNotificationViewHolder.class, R.layout.conversationlist_item_notification_connection_status);
    }

    public void registerNotificationViewHolder(Class<? extends StatusNotificationViewHolder> holderClass, @LayoutRes int layoutResId) {
        StatusNotificationType notificationType = holderClass.getAnnotation(StatusNotificationType.class);
        notificationViewHolders.put(notificationType.value(), holderClass);
        notificationViewHolderLayoutResMap.put(notificationType.value(), layoutResId);
    }

    public Class<? extends StatusNotificationViewHolder> getNotificationViewHolder(StatusNotification notification) {
        return notificationViewHolders.get(notification.getClass());
    }

    public @LayoutRes
    int getNotificationViewHolderLayoutResId(StatusNotification notification) {
        return notificationViewHolderLayoutResMap.get(notification.getClass());
    }
}
