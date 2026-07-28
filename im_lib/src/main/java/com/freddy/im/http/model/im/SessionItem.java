package com.freddy.im.http.model.im;


import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 会话列表中单条会话（字段与接口 JSON 对齐，未用到的可为 null）。
 */
@SuppressWarnings("unused")
public class SessionItem implements Serializable {
    private String sessionId;
    private String sessionName;
    private String username;
    private int conversation;
    private long createTime;
    private String icon;
    private int memCount;
    /** 群成员变更时间（来自群详情 {@code memChgTime}，毫秒） */
    private long memChgTime;
    private int unreadCount;
    private int unPullCount;
    private int internalFlag;
    private long topNum;
    /**
     * 免打扰：接口多为 {@code 0/1}；用 {@link Boolean} 区分「未下发」与「明确 false」，
     * 避免 getList 缺字段时把本地免打扰冲掉。
     */
    @Nullable
    private Boolean muteNotify;
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

    /** 最近一条消息 ID（本地同步 / 可选服务端字段） */
    private String lastMessageId;
    /** 最近一条消息内容 */
    private String lastMessageContent;
    /** 最近一条消息内容类型 */
    private String lastContentType;
    /** 最近一条消息 customInfo（来自 {@code ImMessageItem#getCustomInfo()}） */
    private String lastCustomInfo;
    /** 最近一条消息类型（{@code msgType} / {@code contentType}） */
    private int lastMsgType;
    /** 最近一条消息业务状态：0 正常，1 撤回 */
    private int lastMessageStatus;
    /** 最近一条消息时间（用于会话列表排序） */
    private long lastMessageTime;

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

    public int getConversation() {
        return conversation;
    }

    public void setConversation(int conversation) {
        this.conversation = conversation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setMemCount(int memCount) {
        this.memCount = memCount;
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
        return Boolean.TRUE.equals(muteNotify);
    }

    @Nullable
    public Boolean getMuteNotify() {
        return muteNotify;
    }

    public void setMuteNotify(boolean muteNotify) {
        this.muteNotify = muteNotify;
    }

    public void setMuteNotify(@Nullable Boolean muteNotify) {
        this.muteNotify = muteNotify;
    }

    public String getReadedPosition() {
        return readedPosition;
    }

    public void setReadedPosition(String readedPosition) {
        this.readedPosition = readedPosition;
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

    public String getSelfReadPosition() {
        return selfReadPosition;
    }

    public String getIcon() {
        return icon;
    }

    public int getMemCount() {
        return memCount;
    }

    public long getMemChgTime() {
        return memChgTime;
    }

    public void setMemChgTime(long memChgTime) {
        this.memChgTime = memChgTime;
    }

    public int getUnreadCount() {
        return unreadCount;
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

    public int getLastMessageStatus() {
        return lastMessageStatus;
    }

    public void setLastMessageStatus(int lastMessageStatus) {
        this.lastMessageStatus = lastMessageStatus;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnPullCount() {
        return unPullCount;
    }

    public void setUnPullCount(int unPullCount) {
        this.unPullCount = unPullCount;
    }
}
