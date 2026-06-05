package com.freddy.im.db.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 会话表（Realm）；与 {@link com.freddy.im.http.model.im.SessionItem} 通过 Mapper 转换。
 */
public class SessionEntity extends RealmObject {

    @PrimaryKey
    private String sessionId;
    private String sessionName;
    private String username;
    private int conversation;
    private long createTime;
    private String icon;
    private int memCount;
    private int unreadCount;
    private int internalFlag;
    private long topNum;
    private boolean muteNotify;
    private String readedPosition;
    private String selfReadPosition;
    private String busi;
    private String tags;
    private String permission;
    private String oppositeUserId;
    /** 群主 userId（接口字段名为 {@code owerId}） */
    private String owerId;
    private String oppositeCompanyId;
    private String extAttrs;
    private String userExtAttrs;
    private long lastMessageTime;
    private String lastMessageId;
    private String lastMessageContent;
    private String lastContentType;
    /** 最近一条消息的 customInfo（与 {@link com.freddy.im.http.model.im.ImMessageItem#getCustomInfo()} 一致） */
    private String lastCustomInfo;
    private int lastMsgType;
    /** 单聊：对方已读游标（messageId 末尾序列，该序列及之前己方消息视为已读） */
    private long peerReadUpToSeq;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getConversation() {
        return conversation;
    }

    public void setConversation(int conversation) {
        this.conversation = conversation;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getMemCount() {
        return memCount;
    }

    public void setMemCount(int memCount) {
        this.memCount = memCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getInternalFlag() {
        return internalFlag;
    }

    public void setInternalFlag(int internalFlag) {
        this.internalFlag = internalFlag;
    }

    public long getTopNum() {
        return topNum;
    }

    public void setTopNum(long topNum) {
        this.topNum = topNum;
    }

    public boolean isMuteNotify() {
        return muteNotify;
    }

    public void setMuteNotify(boolean muteNotify) {
        this.muteNotify = muteNotify;
    }

    public String getReadedPosition() {
        return readedPosition;
    }

    public void setReadedPosition(String readedPosition) {
        this.readedPosition = readedPosition;
    }

    public String getSelfReadPosition() {
        return selfReadPosition;
    }

    public void setSelfReadPosition(String selfReadPosition) {
        this.selfReadPosition = selfReadPosition;
    }

    public String getBusi() {
        return busi;
    }

    public void setBusi(String busi) {
        this.busi = busi;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getOppositeUserId() {
        return oppositeUserId;
    }

    public void setOppositeUserId(String oppositeUserId) {
        this.oppositeUserId = oppositeUserId;
    }

    public String getOwerId() {
        return owerId;
    }

    public void setOwerId(String owerId) {
        this.owerId = owerId;
    }

    public String getOppositeCompanyId() {
        return oppositeCompanyId;
    }

    public void setOppositeCompanyId(String oppositeCompanyId) {
        this.oppositeCompanyId = oppositeCompanyId;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public String getUserExtAttrs() {
        return userExtAttrs;
    }

    public void setUserExtAttrs(String userExtAttrs) {
        this.userExtAttrs = userExtAttrs;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public String getLastContentType() {
        return lastContentType;
    }

    public void setLastContentType(String lastContentType) {
        this.lastContentType = lastContentType;
    }

    public String getLastCustomInfo() {
        return lastCustomInfo;
    }

    public void setLastCustomInfo(String lastCustomInfo) {
        this.lastCustomInfo = lastCustomInfo;
    }

    public int getLastMsgType() {
        return lastMsgType;
    }

    public void setLastMsgType(int lastMsgType) {
        this.lastMsgType = lastMsgType;
    }

    public long getPeerReadUpToSeq() {
        return peerReadUpToSeq;
    }

    public void setPeerReadUpToSeq(long peerReadUpToSeq) {
        this.peerReadUpToSeq = peerReadUpToSeq;
    }
}
