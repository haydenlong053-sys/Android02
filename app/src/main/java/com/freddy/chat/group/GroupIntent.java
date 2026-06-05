package com.freddy.chat.group;

import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.GroupVo;

/**
 * 群管理页面 Intent 参数。
 */
public final class GroupIntent {

    public static final String EXTRA_GROUP_ID = "extra_group_id";
    public static final String EXTRA_GROUP_VO = "extra_group_vo";
    public static final String EXTRA_IS_OWNER = "extra_is_owner";

    private GroupIntent() {
    }

    public static void put(@NonNull Intent intent, @NonNull String groupId) {
        intent.putExtra(EXTRA_GROUP_ID, groupId);
    }

    public static void put(@NonNull Intent intent, @NonNull String groupId, @Nullable GroupVo groupVo) {
        put(intent, groupId);
        if (groupVo != null) {
            intent.putExtra(EXTRA_GROUP_VO, groupVo);
        }
    }

    @Nullable
    public static String getGroupId(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(EXTRA_GROUP_ID);
    }

    @Nullable
    public static GroupVo getGroupVo(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra(EXTRA_GROUP_VO, GroupVo.class);
        }
        Object extra = intent.getSerializableExtra(EXTRA_GROUP_VO);
        if (extra instanceof GroupVo) {
            return (GroupVo) extra;
        }
        return null;
    }

    public static boolean isOwner(@Nullable Intent intent) {
        return intent != null && intent.getBooleanExtra(EXTRA_IS_OWNER, false);
    }

    public static void setOwner(@NonNull Intent intent, boolean owner) {
        intent.putExtra(EXTRA_IS_OWNER, owner);
    }
}
