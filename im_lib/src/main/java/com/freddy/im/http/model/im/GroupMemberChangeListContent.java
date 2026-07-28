package com.freddy.im.http.model.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * POST {@code /im/group/changeMemberList} 响应 {@code content}。
 * <p>
 * 成员项含 {@code delFlag}：0 正常（upsert），1 删除（软删）。
 */
@SuppressWarnings("unused")
public class GroupMemberChangeListContent {

    private String groupId;
    private List<GroupUserVo> members;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @NonNull
    public List<GroupUserVo> getMembers() {
        return members != null ? members : Collections.emptyList();
    }

    public void setMembers(@Nullable List<GroupUserVo> members) {
        this.members = members;
    }
}
