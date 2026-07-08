package com.freddy.chat.event;

import androidx.annotation.NonNull;

/**
 * 群禁言状态变更；{@link Events#GROUP_MUTE_UPDATED} 载荷。
 */
public final class GroupMuteUpdateEvent {

    /** 未携带 allMute，需走 HTTP 刷新 */
    public static final int REFRESH_FROM_SERVER = -1;

    /** 未携带成员禁言态 */
    private static final int MEMBER_MUTE_NOT_SET = -1;

    @NonNull
    private final String groupId;
    private final int allMute;
    private final int memberMute;

    public GroupMuteUpdateEvent(@NonNull String groupId, int allMute) {
        this(groupId, allMute, MEMBER_MUTE_NOT_SET);
    }

    private GroupMuteUpdateEvent(@NonNull String groupId, int allMute, int memberMute) {
        this.groupId = groupId;
        this.allMute = allMute;
        this.memberMute = memberMute;
    }

    @NonNull
    public static GroupMuteUpdateEvent memberMute(@NonNull String groupId, int mute) {
        return new GroupMuteUpdateEvent(groupId, REFRESH_FROM_SERVER, mute);
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
        return !hasMemberMute() && allMute < 0;
    }

    public boolean isAllMuted() {
        return allMute == 1;
    }

    /** 0x0501 code=1003：信令携带当前用户成员禁言态。 */
    public boolean hasMemberMute() {
        return memberMute == 0 || memberMute == 1;
    }

    public boolean isMemberMuted() {
        return memberMute == 1;
    }
}
