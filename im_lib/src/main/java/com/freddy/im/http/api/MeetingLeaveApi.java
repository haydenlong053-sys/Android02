package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/meeting/leave}：退出会议。 */
public final class MeetingLeaveApi implements IRequestApi {

    @Nullable
    private String meetingId;
    @Nullable
    private String roomName;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/meeting/leave";
    }

    public MeetingLeaveApi setMeetingId(@Nullable String meetingId) {
        this.meetingId = meetingId;
        return this;
    }

    public MeetingLeaveApi setRoomName(@Nullable String roomName) {
        this.roomName = roomName;
        return this;
    }
}
