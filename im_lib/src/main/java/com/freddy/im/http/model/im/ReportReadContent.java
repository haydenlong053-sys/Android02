package com.freddy.im.http.model.im;

/**
 * GET {@code im/message/reportRead/{messageId}} 响应 {@code content}。
 */
public final class ReportReadContent {

    private String messageId;

    public String getMessageId() {
        return messageId;
    }
}
