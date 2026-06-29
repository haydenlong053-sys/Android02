package com.freddy.im.group;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** 群发言禁言状态判定（对齐野火 {@code updateGroupMuteStatus}）。 */
public final class GroupMuteEvaluator {

    public enum UiState {
        ENABLED,
        ALL_MUTED,
        SELF_MUTED
    }

    private GroupMuteEvaluator() {
    }

    @NonNull
    public static UiState evaluate(int myRole,
                                   @Nullable String currentUserId,
                                   boolean allMute,
                                   @Nullable Set<String> mutedUserIds) {
        if (allMute && !GroupRole.isOwnerOrAdmin(myRole)) {
            return UiState.ALL_MUTED;
        }
        if (!TextUtils.isEmpty(currentUserId)
                && mutedUserIds != null
                && mutedUserIds.contains(currentUserId)) {
            return UiState.SELF_MUTED;
        }
        return UiState.ENABLED;
    }

    @NonNull
    public static Set<String> copyMutedIds(@Nullable Iterable<String> userIds) {
        if (userIds == null) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (String id : userIds) {
            if (!TextUtils.isEmpty(id)) {
                set.add(id);
            }
        }
        return set;
    }
}
