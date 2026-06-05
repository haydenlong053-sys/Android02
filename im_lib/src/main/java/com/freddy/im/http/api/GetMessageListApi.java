package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code im/message/getList}：按锚点拉取消息列表。
 * <p>
 * 全局头由 {@link com.freddy.im.http.ImHttpConfig} 注入；请求体仅含 messageId、anchor、op。
 */
public final class GetMessageListApi implements IRequestApi {

    private String messageId;
    private Integer anchor;
    private Integer op;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/message/getList";
    }

    public GetMessageListApi setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public GetMessageListApi setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public GetMessageListApi setOp(int op) {
        this.op = op;
        return this;
    }
}
