package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/meeting/end}：散会（仅发起人）。 */
public final class MeetingEndApi implements IRequestApi {

    @Nullable
    private String meetingId;
    @Nullable
    private String roomName;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/meeting/end";
    }

    public MeetingEndApi setMeetingId(@Nullable String meetingId) {
        this.meetingId = meetingId;
        return this;
    }

    public MeetingEndApi setRoomName(@Nullable String roomName) {
        this.roomName = roomName;
        return this;
    }
}
