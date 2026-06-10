package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * POST {@code api/im/message/transpond}：转发消息（文档 §4.6）。
 */
public final class TranspondMessageApi implements IRequestApi {

    private List<String> msgIdlist;
    private String to;
    private int conversation;
    @Nullable
    private String contentType;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/message/transpond";
    }

    public TranspondMessageApi setMsgIdlist(@NonNull List<String> msgIdlist) {
        this.msgIdlist = msgIdlist;
        return this;
    }

    public TranspondMessageApi setTo(@NonNull String to) {
        this.to = to;
        return this;
    }

    public TranspondMessageApi setConversation(int conversation) {
        this.conversation = conversation;
        return this;
    }

    public TranspondMessageApi setContentType(@Nullable String contentType) {
        this.contentType = contentType;
        return this;
    }
}
