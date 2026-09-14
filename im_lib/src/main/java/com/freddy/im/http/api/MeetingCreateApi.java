package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/meeting/create}：发起会议（LiveKit）。 */
public final class MeetingCreateApi implements IRequestApi {

    /** simple / live，群语音会议室用 live */
    private String mode;
    private String title;
    /** audio / video */
    private String mediaType;
    private Integer maxSpeakers;
    private String fromUsername;
    private String fromUserIcon;
    /** 业务扩展：绑定群 ID，便于服务端推 MEET_START */
    @Nullable
    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/meeting/create";
    }

    public MeetingCreateApi setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public MeetingCreateApi setTitle(String title) {
        this.title = title;
        return this;
    }

    public MeetingCreateApi setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public MeetingCreateApi setMaxSpeakers(Integer maxSpeakers) {
        this.maxSpeakers = maxSpeakers;
        return this;
    }

    public MeetingCreateApi setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    public MeetingCreateApi setFromUserIcon(String fromUserIcon) {
        this.fromUserIcon = fromUserIcon;
        return this;
    }

    public MeetingCreateApi setGroupId(@Nullable String groupId) {
        this.groupId = groupId;
        return this;
    }
}
