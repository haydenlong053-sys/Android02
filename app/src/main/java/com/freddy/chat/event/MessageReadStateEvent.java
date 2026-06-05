package com.freddy.chat.event;

import androidx.annotation.NonNull;

/**
 * 消息已读信令通知 UI：{@link Events#MESSAGE_READ_STATE_CHANGED} 载荷。
 */
public final class MessageReadStateEvent {

    public enum Type {
        /** 单聊 0x0207：对方已读至某 messageId 序列 */
        PEER_READ,
        /** 群聊 0x0210：某消息已读人数变更 */
        GROUP_READ_COUNT
    }

    @NonNull
    private final String sessionId;
    @NonNull
    private final Type type;
    @NonNull
    private final String messageId;
    private final int readPersonCount;

    public MessageReadStateEvent(@NonNull String sessionId,
                                 @NonNull Type type,
                                 @NonNull String messageId,
                                 int readPersonCount) {
        this.sessionId = sessionId;
        this.type = type;
        this.messageId = messageId;
        this.readPersonCount = readPersonCount;
    }

    @NonNull
    public String getSessionId() {
        return sessionId;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public int getReadPersonCount() {
        return readPersonCount;
    }
}
