package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code /im/group/memberList}：分页获取群成员（文档 §3）。
 * <p>
 * 请求体：{@code groupId}（必填）、{@code anchor}（可选）、{@code count}（可选，默认 100）。
 */
public final class GetGroupMemberListApi implements IRequestApi {

    private String groupId;
    /** 首页可不传；后续页使用上一页响应中的 {@code anchor} */
    private Integer anchor;
    private Integer count;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/memberList";
    }

    public GetGroupMemberListApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public GetGroupMemberListApi setAnchor(@Nullable Integer anchor) {
        this.anchor = anchor;
        return this;
    }

    public GetGroupMemberListApi setCount(int count) {
        this.count = count;
        return this;
    }
}
