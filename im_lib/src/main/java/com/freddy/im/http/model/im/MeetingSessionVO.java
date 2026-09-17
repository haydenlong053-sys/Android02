package com.freddy.im.http.model.im;

import java.util.List;

/**
 * {@code /im/meeting/*} 多数接口响应 {@code content}（MeetingSessionVO）。
 * <p>
 * 真实字段示例：{@code speakers}/{@code pending} 为 userId 字符串数组，不是对象。
 */
@SuppressWarnings("unused")
public class MeetingSessionVO implements java.io.Serializable {

    private String url;
    private String token;
    private String roomName;
    private String meetingId;
    private String title;
    /** simple / live */
    private String mode;
    /** audio / video */
    private String mediaType;
    private String hostId;
    /** host / speaker / audience / pending */
    private String myRole;
    private Boolean canPublish;
    private Integer maxSpeakers;
    private Long createdAt;
    private Long endedAt;
    private Integer duration;
    /** 麦上 userId 列表，如 ["179185..."] */
    private List<String> speakers;
    /** 上麦申请 userId 列表（接口字段名 pending） */
    private List<String> pending;
    private Integer activeCount;


    public Integer getActiveCount() {
        return activeCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public Boolean getCanPublish() {
        return canPublish;
    }

    public boolean isCanPublish() {
        return canPublish != null && canPublish;
    }

    public void setCanPublish(Boolean canPublish) {
        this.canPublish = canPublish;
    }

    public Integer getMaxSpeakers() {
        return maxSpeakers;
    }

    public void setMaxSpeakers(Integer maxSpeakers) {
        this.maxSpeakers = maxSpeakers;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Long endedAt) {
        this.endedAt = endedAt;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<String> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<String> speakers) {
        this.speakers = speakers;
    }

    public List<String> getPending() {
        return pending;
    }

    public void setPending(List<String> pending) {
        this.pending = pending;
    }
}
