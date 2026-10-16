package com.freddy.im.http.model.im;

/**
 * GET {@code im/group/pinnedMessages/{groupId}} 列表项（置顶瞬间快照）。
 */
@SuppressWarnings("unused")
public class GroupPinnedMessageRes {

    private String messageId;
    private String sender;
    private String contentType;
    private String content;
    private String customInfo;
    private long sendTime;
    private String pinnedBy;
    private long pinTime;

    public String getMessageId() {
        return messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }

    public String getCustomInfo() {
        return customInfo;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getPinnedBy() {
        return pinnedBy;
    }

    public long getPinTime() {
        return pinTime;
    }
}
