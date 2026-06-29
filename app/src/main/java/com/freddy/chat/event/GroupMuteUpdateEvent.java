package com.freddy.chat.event;

import androidx.annotation.NonNull;

/**
 * 群禁言状态变更；{@link Events#GROUP_MUTE_UPDATED} 载荷。
 */
public final class GroupMuteUpdateEvent {

    /** 未携带 allMute，需走 HTTP 刷新 */
    public static final int REFRESH_FROM_SERVER = -1;

    @NonNull
    private final String groupId;
    private final int allMute;

    public GroupMuteUpdateEvent(@NonNull String groupId, int allMute) {
        this.groupId = groupId;
        this.allMute = allMute;
    }

    @NonNull
    public String getGroupId() {
        return groupId;
    }

    /** 0 未开启，1 开启；{@link #REFRESH_FROM_SERVER} 表示需 HTTP 拉取 */
    public int getAllMute() {
        return allMute;
    }

    public boolean shouldRefreshFromServer() {
        return allMute < 0;
    }

    public boolean isAllMuted() {
        return allMute == 1;
    }
}
