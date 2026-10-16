package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code im/group/pinMessage}：置顶群消息（文档 § groupPinned）。
 * 请求体：{@code groupId} + {@code messageId}。
 */
public final class PinGroupMessageApi implements IRequestApi {

    private String groupId;
    private String messageId;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/pinMessage";
    }

    public PinGroupMessageApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public PinGroupMessageApi setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
        return this;
    }
}
