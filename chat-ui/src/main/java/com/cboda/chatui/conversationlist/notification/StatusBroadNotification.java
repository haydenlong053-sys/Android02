/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification;

import androidx.annotation.Nullable;

public abstract class StatusBroadNotification {
    @Override
    public boolean equals(@Nullable Object obj) {
        return this.getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
