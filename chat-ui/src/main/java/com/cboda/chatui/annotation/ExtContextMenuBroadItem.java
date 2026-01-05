/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;

/**
 * 用于注解聊天扩展{@link ConversationBroadExt}, 点击扩展之后的相应，若扩展{@link ConversationBroadExt}只有一个菜单{@link ExtContextMenuBroadItem}，
 * 那么直接响应；如果有多个，则弹出菜单，让进一步选择
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtContextMenuBroadItem {
    String tag() default "";
}
