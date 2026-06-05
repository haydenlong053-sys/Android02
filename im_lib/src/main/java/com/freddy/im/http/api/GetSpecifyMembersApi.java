package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * POST {@code /im/group/getSpecifyMembers}：获取指定群成员（文档 §12）。
 * <p>
 * 请求体 {@code MemberQueryReq}：{@code groupId}、{@code members}（userId 列表）。
 */
public final class GetSpecifyMembersApi implements IRequestApi {

    private String groupId;
    private List<String> members;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/getSpecifyMembers";
    }

    public GetSpecifyMembersApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public GetSpecifyMembersApi setMembers(@NonNull List<String> members) {
        this.members = members;
        return this;
    }
}
