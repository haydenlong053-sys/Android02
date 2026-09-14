package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code api/im/meeting/speak/*}：上下麦。
 * 路径由构造参数决定。
 */
public final class MeetingSpeakApi implements IRequestApi {

    public static final String APPLY = "api/im/meeting/speak/apply";
    public static final String CANCEL = "api/im/meeting/speak/cancel";
    public static final String APPROVE = "api/im/meeting/speak/approve";
    public static final String REJECT = "api/im/meeting/speak/reject";
    public static final String INVITE = "api/im/meeting/speak/invite";
    public static final String LEAVE = "api/im/meeting/speak/leave";
    public static final String KICK = "api/im/meeting/speak/kick";

    private final String apiPath;
    @Nullable
    private String meetingId;
    @Nullable
    private String roomName;
    @Nullable
    private String targetUserId;
    @Nullable
    private String fromUsername;
    @Nullable
    private String fromUserIcon;

    public MeetingSpeakApi(@NonNull String apiPath) {
        this.apiPath = apiPath;
    }

    @NonNull
    @Override
    public String getApi() {
        return apiPath;
    }

    public MeetingSpeakApi setMeetingId(@Nullable String meetingId) {
        this.meetingId = meetingId;
        return this;
    }

    public MeetingSpeakApi setRoomName(@Nullable String roomName) {
        this.roomName = roomName;
        return this;
    }

    public MeetingSpeakApi setTargetUserId(@Nullable String targetUserId) {
        this.targetUserId = targetUserId;
        return this;
    }

    public MeetingSpeakApi setFromUsername(@Nullable String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    public MeetingSpeakApi setFromUserIcon(@Nullable String fromUserIcon) {
        this.fromUserIcon = fromUserIcon;
        return this;
    }
}
