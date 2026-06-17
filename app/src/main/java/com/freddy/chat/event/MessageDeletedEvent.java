package com.freddy.chat.event;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 本地消息删除后 UI 刷新载荷。
 */
public final class MessageDeletedEvent {

    @Nullable
    private final String sessionId;
    @NonNull
    private final List<AppMessage> deletedMessages;

    public MessageDeletedEvent(@Nullable String sessionId,
                               @NonNull List<AppMessage> deletedMessages) {
        this.sessionId = sessionId;
        this.deletedMessages = Collections.unmodifiableList(new ArrayList<>(deletedMessages));
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    @NonNull
    public List<AppMessage> getDeletedMessages() {
        return deletedMessages;
    }
}
