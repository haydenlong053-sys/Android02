package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code im/conversation/getList}：获取用户会话列表（已按活跃度排序）。
 * <p>
 * 请求体：{@code startTime}、{@code anchor}；鉴权头由 {@link com.freddy.im.http.ImHttpConfig} 配置。
 */
public final class GetConversationListApi implements IRequestApi {

    private Long startTime;
    private Integer anchor;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/conversation/getList";
    }

    public GetConversationListApi setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public GetConversationListApi setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }
}
