package com.freddy.chat.event;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;

/**
 * 消息撤回（0x0202 或 HTTP repeal 成功）后 UI 刷新载荷。
 */
public final class MessageRepealEvent {

    @NonNull
    private final String messageId;
    @Nullable
    private final String sessionId;
    @Nullable
    private final AppMessage recalledMessage;

    public MessageRepealEvent(@NonNull String messageId,
                              @Nullable String sessionId,
                              @Nullable AppMessage recalledMessage) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.recalledMessage = recalledMessage;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    @Nullable
    public AppMessage getRecalledMessage() {
        return recalledMessage;
    }
}
