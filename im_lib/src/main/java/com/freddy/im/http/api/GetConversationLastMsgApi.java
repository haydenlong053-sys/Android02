package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code im/conversation/getLastMsg}：批量获取会话最后一条消息。
 * <p>
 * 请求体为 JSON 数组（sessionId 列表），需配合 {@code EasyHttp.post(...).json(List)} 使用。
 */
public final class GetConversationLastMsgApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "api/im/conversation/getLastMsg";
    }
}
