/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.model;

public class FriendRequestBroadValue extends HeaderBroadValue {
    private int unreadRequestCount;

    public FriendRequestBroadValue(int unreadRequestCount) {
        this.unreadRequestCount = unreadRequestCount;
    }

    public int getUnreadRequestCount() {
        return unreadRequestCount;
    }

    public void setUnreadRequestCount(int unreadRequestCount) {
        this.unreadRequestCount = unreadRequestCount;
    }

}
