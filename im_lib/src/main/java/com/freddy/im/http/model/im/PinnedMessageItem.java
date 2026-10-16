package com.freddy.im.http.model.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 本地 / UI 用的群置顶消息快照（与 {@link GroupPinnedMessageRes} 对齐）。
 */
public class PinnedMessageItem implements Serializable {

    private String groupId;
    private String messageId;
    private String sender;
    private String contentType;
    private String content;
    private String customInfo;
    private long sendTime;
    private String pinnedBy;
    private long pinTime;

    @Nullable
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nullable String groupId) {
        this.groupId = groupId;
    }

    @Nullable
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@Nullable String messageId) {
        this.messageId = messageId;
    }

    @Nullable
    public String getSender() {
        return sender;
    }

    public void setSender(@Nullable String sender) {
        this.sender = sender;
    }

    @Nullable
    public String getContentType() {
        return contentType;
    }

    public void setContentType(@Nullable String contentType) {
        this.contentType = contentType;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    @Nullable
    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(@Nullable String customInfo) {
        this.customInfo = customInfo;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    @Nullable
    public String getPinnedBy() {
        return pinnedBy;
    }

    public void setPinnedBy(@Nullable String pinnedBy) {
        this.pinnedBy = pinnedBy;
    }

    public long getPinTime() {
        return pinTime;
    }

    public void setPinTime(long pinTime) {
        this.pinTime = pinTime;
    }

    @NonNull
    public String clientMsgId() {
        return "pin:" + (groupId != null ? groupId : "") + ":" + (messageId != null ? messageId : "");
    }
}
