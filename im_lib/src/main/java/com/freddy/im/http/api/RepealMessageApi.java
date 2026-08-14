package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * GET {@code api/im/message/repeal/{messageId}}：撤回消息（文档 §4.4）。
 */
public final class RepealMessageApi implements IRequestApi {

    private String messageId;

    @NonNull
    @Override
    public String getApi() {
        String id = messageId != null ? messageId : "";
        return "im/message/repeal/" + Uri.encode(id);
    }

    public RepealMessageApi setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
        return this;
    }
}
