package com.freddy.im.db.entity;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * 群置顶消息快照表（与普通消息表分离；本地删消息不级联）。
 * 主键 {@code pinKey = groupId + "_" + messageId}。
 */
public class PinnedMessageEntity extends RealmObject {

    @PrimaryKey
    private String pinKey;

    @Index
    private String groupId;

    private String messageId;
    private String sender;
    private String contentType;
    private String content;
    private String customInfo;
    private long sendTime;
    private String pinnedBy;
    @Index
    private long pinTime;

    public static String buildKey(String groupId, String messageId) {
        return (groupId != null ? groupId : "") + "_" + (messageId != null ? messageId : "");
    }

    public String getPinKey() {
        return pinKey;
    }

    public void setPinKey(String pinKey) {
        this.pinKey = pinKey;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.customInfo = customInfo;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getPinnedBy() {
        return pinnedBy;
    }

    public void setPinnedBy(String pinnedBy) {
        this.pinnedBy = pinnedBy;
    }

    public long getPinTime() {
        return pinTime;
    }

    public void setPinTime(long pinTime) {
        this.pinTime = pinTime;
    }
}
