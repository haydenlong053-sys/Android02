package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code /im/group/changeMemberList}：按 {@code since} 拉取群成员增量变更。
 * <p>
 * 请求体：{@code groupId}、{@code since}（本地已对齐的 memChgTime，毫秒）。
 */
public final class ChangeGroupMemberListApi implements IRequestApi {

    private String groupId;
    private long since;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/changeMemberList";
    }

    public ChangeGroupMemberListApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public ChangeGroupMemberListApi setSince(long since) {
        this.since = since;
        return this;
    }
}
