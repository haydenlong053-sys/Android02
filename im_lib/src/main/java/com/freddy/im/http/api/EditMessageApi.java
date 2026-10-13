package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code im/message/edit}：编辑消息（文档 § msgEdit）。
 * <p>
 * 请求体：{@code messageId} + {@code content}（纯文本，服务端再包成 {@code {"msg":"..."}}）。
 */
public final class EditMessageApi implements IRequestApi {

    private String messageId;
    private String content;

    @NonNull
    @Override
    public String getApi() {
        return "im/message/edit";
    }

    public EditMessageApi setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
        return this;
    }

    public EditMessageApi setContent(@NonNull String content) {
        this.content = content;
        return this;
    }
}
