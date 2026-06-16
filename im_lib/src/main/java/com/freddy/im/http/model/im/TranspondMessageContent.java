package com.freddy.im.http.model.im;

/**
 * POST {@code api/im/message/transpond} 成功时 {@code content} 业务体（文档 §4.6）。
 */
public final class TranspondMessageContent {

    private int conversation;
    private String messageId;
    private String clientMsgId;
    private int readPersonCount;

    public int getConversation() {
        return conversation;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public int getReadPersonCount() {
        return readPersonCount;
    }
}
