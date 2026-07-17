package com.freddy.im.db.mapper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.entity.GroupMemberEntity;
import com.freddy.im.http.model.im.GroupMemberItem;

/**
 * {@link GroupMemberItem} 与 {@link GroupMemberEntity} 互转。
 */
public final class GroupMemberRealmMapper {

    private GroupMemberRealmMapper() {
    }

    @NonNull
    public static String memberKey(@NonNull String groupId, @NonNull String userId) {
        return groupId + "_" + userId;
    }

    @NonNull
    public static GroupMemberEntity toEntity(@NonNull GroupMemberItem item) {
        GroupMemberEntity entity = new GroupMemberEntity();
        String groupId = item.getGroupId() != null ? item.getGroupId() : "";
        String userId = item.getUserId() != null ? item.getUserId() : "";
        entity.setMemberKey(memberKey(groupId, userId));
        entity.setGroupId(groupId);
        entity.setUserId(userId);
        entity.setCompanyId(item.getCompanyId());
        entity.setNickname(item.getNickname());
        entity.setGroupNickname(item.getGroupNickname());
        entity.setUsername(item.getUsername());
        entity.setIdNo(item.getIdNo());
        entity.setHeadPortrait(item.getHeadPortrait());
        entity.setStatusInGroup(item.getStatusInGroup());
        entity.setRole(item.getRole());
        entity.setJoinTime(item.getJoinTime());
        entity.setRegisterTime(item.getRegisterTime());
        entity.setLianghao(item.getLianghao());
        entity.setIsvip(item.getIsvip());
        entity.setGrade(item.getGrade());
        entity.setUserExtAttrs(item.getUserExtAttrs());
        entity.setDeleted(item.isDeleted());
        return entity;
    }

    @Nullable
    public static GroupMemberItem toItem(@Nullable GroupMemberEntity entity) {
        if (entity == null) {
            return null;
        }
        GroupMemberItem item = new GroupMemberItem();
        item.setGroupId(entity.getGroupId());
        item.setUserId(entity.getUserId());
        item.setCompanyId(entity.getCompanyId());
        item.setNickname(entity.getNickname());
        item.setGroupNickname(entity.getGroupNickname());
        item.setUsername(entity.getUsername());
        item.setIdNo(entity.getIdNo());
        item.setHeadPortrait(entity.getHeadPortrait());
        item.setStatusInGroup(entity.getStatusInGroup());
        item.setRole(entity.getRole());
        item.setJoinTime(entity.getJoinTime());
        item.setRegisterTime(entity.getRegisterTime());
        item.setLianghao(entity.getLianghao());
        item.setIsvip(entity.getIsvip());
        item.setGrade(entity.getGrade());
        item.setUserExtAttrs(entity.getUserExtAttrs());
        item.setDeleted(entity.isDeleted());
        return item;
    }
}
