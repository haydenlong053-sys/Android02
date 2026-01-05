/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.mention;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class MentionBroadSpan extends ClickableSpan {
    private boolean isMentionAll;
    private String uid;

    public MentionBroadSpan(String uid) {
        this.uid = uid;
    }

    public MentionBroadSpan(boolean isMentionAll) {
        this.isMentionAll = isMentionAll;
    }

    public boolean isMentionAll() {
        return isMentionAll;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        // do nothing
    }

    @Override
    public void onClick(@NonNull View widget) {
        // do nothing
    }
}

