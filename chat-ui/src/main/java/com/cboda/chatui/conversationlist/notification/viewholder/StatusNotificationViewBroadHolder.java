/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.conversationlist.notification.StatusBroadNotification;

public abstract class StatusNotificationViewBroadHolder {
    protected Fragment fragment;

    public StatusNotificationViewBroadHolder(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract void onBind(View view, StatusBroadNotification notification);
}
