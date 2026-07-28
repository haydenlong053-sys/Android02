package com.freddy.im.db;

import androidx.annotation.NonNull;

import com.freddy.im.http.model.im.GroupMemberItem;

import java.util.Collections;
import java.util.List;

/** 本地群成员分页结果。 */
public final class GroupMemberPageResult {

    @NonNull
    public final List<GroupMemberItem> members;
    public final int total;
    public final int myRole;

    public GroupMemberPageResult(@NonNull List<GroupMemberItem> members, int total, int myRole) {
        this.members = members != null ? members : Collections.emptyList();
        this.total = Math.max(0, total);
        this.myRole = myRole;
    }
}
