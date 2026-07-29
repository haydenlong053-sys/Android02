package com.freddy.im.db.mapper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.GroupUserVo;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP {@link GroupUserVo} → {@link GroupMemberItem}。
 */
public final class GroupUserVoMapper {

    private GroupUserVoMapper() {
    }

    @NonNull
    public static List<GroupMemberItem> toItems(@NonNull String groupId,
                                                @Nullable List<GroupUserVo> members) {
        List<GroupMemberItem> out = new ArrayList<>();
        if (members == null) {
            return out;
        }
        for (GroupUserVo vo : members) {
            GroupMemberItem item = toItem(groupId, vo);
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }

    @Nullable
    public static GroupMemberItem toItem(@NonNull String groupId, @Nullable GroupUserVo vo) {
        if (vo == null || TextUtils.isEmpty(vo.getUserId())) {
            return null;
        }
        GroupMemberItem item = new GroupMemberItem();
        item.setGroupId(groupId);
        item.setUserId(vo.getUserId().trim());
        item.setCompanyId(vo.getCompanyId());
        item.setNickname(vo.getNickname());
        item.setGroupNickname(vo.getGroupNickname());
        item.setUsername(vo.getUsername());
        item.setIdNo(vo.getIdNo());
        item.setHeadPortrait(vo.getHeadPortrait());
        item.setStatusInGroup(vo.getStatusInGroup());
        item.setRole(vo.getRole());
        item.setJoinTime(vo.getJoinTime() != null ? vo.getJoinTime() : 0L);
        item.setRegisterTime(vo.getRegisterTime() != null ? vo.getRegisterTime() : 0L);
        item.setLianghao(vo.getLianghao());
        item.setIsvip(vo.getIsvip());
        item.setGrade(formatGrade(vo.getGrade()));
        item.setUserExtAttrs(vo.getUserExtAttrs());
        item.setDeleted(vo.isDeletedFlag());
        return item;
    }

    @Nullable
    private static String formatGrade(@Nullable Double grade) {
        if (grade == null) {
            return null;
        }
        return String.valueOf(grade);
    }
}
