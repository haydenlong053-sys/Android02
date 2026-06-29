package com.freddy.chat;

import androidx.annotation.NonNull;

import java.util.Set;

/** 消息列表页群发言禁言，由 {@link MessageListActivity} 实现。 */
public interface GroupMuteHost {

    /** 拉取群禁言状态（全员开关 + 被禁言成员）并刷新底栏。 */
    void fetchGroupMuteFromServer();

    boolean isGroupAllMuted();

    @NonNull
    Set<String> getMutedMemberIds();

    /** 根据当前角色刷新输入框禁言态。 */
    void applyGroupMuteToInput(int myGroupRole);
}
