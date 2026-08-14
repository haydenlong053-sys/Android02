package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * GET {@code im/message/reportRead/{messageId}}：上报消息已读。
 * <p>
 * {@code messageId} 可含 {@code .} 等字符，路径段需 URL 编码。
 */
public final class ReportMessageReadApi implements IRequestApi {

    private String messageId;

    @NonNull
    @Override
    public String getApi() {
        String id = messageId != null ? messageId : "";
        return "im/message/reportRead/" + Uri.encode(id);
    }

    public ReportMessageReadApi setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
        return this;
    }
}
