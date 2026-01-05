/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification;

import androidx.annotation.LayoutRes;

import java.util.HashMap;
import java.util.Map;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.StatusNotificationBroadType;
import com.cboda.chatui.conversationlist.notification.viewholder.ConnectionNotificationViewBroadHolder;
import com.cboda.chatui.conversationlist.notification.viewholder.PCOnlineNotificationViewBroadHolder;
import com.cboda.chatui.conversationlist.notification.viewholder.StatusNotificationViewBroadHolder;

public class StatusNotificationBroadManager {
    private static StatusNotificationBroadManager instance;
    private Map<Class<? extends StatusBroadNotification>, Class<? extends StatusNotificationViewBroadHolder>> notificationViewHolders;
    private Map<Class<? extends StatusBroadNotification>, Integer> notificationViewHolderLayoutResMap;

    public synchronized static StatusNotificationBroadManager getInstance() {
        if (instance == null) {
            instance = new StatusNotificationBroadManager();
        }
        return instance;
    }

    private StatusNotificationBroadManager() {
        init();
    }

    private void init() {
        notificationViewHolders = new HashMap<>();
        notificationViewHolderLayoutResMap = new HashMap<>();
        registerNotificationViewHolder(PCOnlineNotificationViewBroadHolder.class, R.layout.conversationlist_item_notification_pc_broad_online);
        registerNotificationViewHolder(ConnectionNotificationViewBroadHolder.class, R.layout.conversationlist_item_notification_connection_broad_status);
    }

    public void registerNotificationViewHolder(Class<? extends StatusNotificationViewBroadHolder> holderClass, @LayoutRes int layoutResId) {
        StatusNotificationBroadType notificationType = holderClass.getAnnotation(StatusNotificationBroadType.class);
        notificationViewHolders.put(notificationType.value(), holderClass);
        notificationViewHolderLayoutResMap.put(notificationType.value(), layoutResId);
    }

    public Class<? extends StatusNotificationViewBroadHolder> getNotificationViewHolder(StatusBroadNotification notification) {
        return notificationViewHolders.get(notification.getClass());
    }

    public @LayoutRes
    int getNotificationViewHolderLayoutResId(StatusBroadNotification notification) {
        return notificationViewHolderLayoutResMap.get(notification.getClass());
    }
}
