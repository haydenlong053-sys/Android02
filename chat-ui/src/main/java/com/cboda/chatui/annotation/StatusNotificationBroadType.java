/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cboda.chatui.conversationlist.notification.StatusBroadNotification;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StatusNotificationBroadType {
    Class<? extends StatusBroadNotification> value();
}
