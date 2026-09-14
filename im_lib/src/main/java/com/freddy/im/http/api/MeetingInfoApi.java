package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/**
 * GET {@code api/im/meeting/info}：会议信息。
 * query：meetingId 或 roomName。
 */
public final class MeetingInfoApi implements IRequestApi {

    @Nullable
    private String meetingId;
    @Nullable
    private String roomName;

    @NonNull
    @Override
    public String getApi() {
        if (meetingId != null && !meetingId.isEmpty()) {
            return "api/im/meeting/info?meetingId=" + meetingId;
        }
        if (roomName != null && !roomName.isEmpty()) {
            return "api/im/meeting/info?roomName=" + roomName;
        }
        return "api/im/meeting/info";
    }

    public MeetingInfoApi setMeetingId(@Nullable String meetingId) {
        this.meetingId = meetingId;
        return this;
    }

    public MeetingInfoApi setRoomName(@Nullable String roomName) {
        this.roomName = roomName;
        return this;
    }
}
