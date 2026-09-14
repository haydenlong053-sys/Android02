package com.freddy.im.meeting;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 群 ID ↔ LiveKit 会议会话缓存（供 MEET_START 信令与 MeetPlugin 入会使用）。
 */
public final class GroupMeetSessionStore {

    public static final class Session {
        public final String groupId;
        public final String meetingId;
        @Nullable
        public final String roomName;
        @Nullable
        public final String title;

        public Session(String groupId, String meetingId, @Nullable String roomName, @Nullable String title) {
            this.groupId = groupId;
            this.meetingId = meetingId;
            this.roomName = roomName;
            this.title = title;
        }
    }

    private static final ConcurrentHashMap<String, Session> BY_GROUP = new ConcurrentHashMap<>();

    private GroupMeetSessionStore() {
    }

    public static void put(String groupId, String meetingId, @Nullable String roomName, @Nullable String title) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(meetingId)) {
            return;
        }
        BY_GROUP.put(groupId, new Session(groupId, meetingId, roomName, title));
    }

    @Nullable
    public static Session get(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        return BY_GROUP.get(groupId);
    }

    public static void remove(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        BY_GROUP.remove(groupId);
    }

    public static void clear() {
        BY_GROUP.clear();
    }
}
