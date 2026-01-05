/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification;

import java.util.Objects;

import cn.wildfirechat.model.PCOnlineInfo;

public class PCOnlineStatusBroadNotification extends StatusBroadNotification {
    private PCOnlineInfo pcOnlineInfo;

    public PCOnlineStatusBroadNotification(PCOnlineInfo info) {
        this.pcOnlineInfo = info;
    }

    public PCOnlineInfo getPcOnlineInfo() {
        return pcOnlineInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PCOnlineStatusBroadNotification that = (PCOnlineStatusBroadNotification) o;

        return Objects.equals(pcOnlineInfo, that.pcOnlineInfo);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pcOnlineInfo != null ? pcOnlineInfo.hashCode() : 0);
        return result;
    }
}
