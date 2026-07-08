package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/searchMembers}（3.22 ES 模糊搜索群成员） */
public final class SearchMembersApi implements IRequestApi {

    private String keyword;
    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/searchMembers";
    }

    public SearchMembersApi setKeyword(@NonNull String keyword) {
        this.keyword = keyword;
        return this;
    }

    public SearchMembersApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
