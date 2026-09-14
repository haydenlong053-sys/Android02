package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/meeting/join}（{@code /token} 别名）：加入会议。 */
public final class MeetingJoinApi implements IRequestApi {

    @Nullable
    private String meetingId;
    @Nullable
    private String roomName;
    @Nullable
    private String name;
    @Nullable
    private String mediaType;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/meeting/join";
    }

    public MeetingJoinApi setMeetingId(@Nullable String meetingId) {
        this.meetingId = meetingId;
        return this;
    }

    public MeetingJoinApi setRoomName(@Nullable String roomName) {
        this.roomName = roomName;
        return this;
    }

    public MeetingJoinApi setName(@Nullable String name) {
        this.name = name;
        return this;
    }

    public MeetingJoinApi setMediaType(@Nullable String mediaType) {
        this.mediaType = mediaType;
        return this;
    }
}
