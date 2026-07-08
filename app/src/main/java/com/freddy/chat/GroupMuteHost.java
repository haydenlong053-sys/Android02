package com.freddy.chat;

import androidx.annotation.NonNull;

import java.util.Set;

/** 消息列表页群发言禁言，由 {@link MessageListActivity} 实现。 */
public interface GroupMuteHost {

    /** 拉取群禁言状态（全员开关 + 被禁言成员）并刷新底栏。 */
    void fetchGroupMuteFromServer();

    /**
     * 进入群聊页已拉取群详情时复用 {@link com.freddy.im.http.model.im.GroupVo}，仅补拉被禁言成员列表。
     */
    void applyGroupDetailForMute(@NonNull com.freddy.im.http.model.im.GroupVo group);

    boolean isGroupAllMuted();

    @NonNull
    Set<String> getMutedMemberIds();

    /** 根据当前角色刷新输入框禁言态。 */
    void applyGroupMuteToInput(int myGroupRole);
}
