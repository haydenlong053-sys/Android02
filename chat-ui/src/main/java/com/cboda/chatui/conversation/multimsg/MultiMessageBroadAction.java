/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.multimsg;

import android.content.Context;

import java.util.List;

import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.model.Conversation;

public abstract class MultiMessageBroadAction {
    protected Conversation conversation;
    protected ConversationBroadFragment fragment;

    public MultiMessageBroadAction() {
    }

    public final void onBind(ConversationBroadFragment fragment, Conversation conversation) {
        this.fragment = fragment;
        this.conversation = conversation;

    }

    public abstract void onClick(List<UiBroadMessage> messages);

    public int priority() {
        return 0;
    }

    public boolean confirm() {
        return false;
    }

    public boolean filter(Conversation conversation) {
        return false;
    }

    public abstract int iconResId();

    public abstract String title(Context context);

    public String confirmPrompt() {
        return "";
    }
}
