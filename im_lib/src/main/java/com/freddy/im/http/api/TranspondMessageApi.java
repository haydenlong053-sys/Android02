package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.TranspondMessageParam;
import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * POST {@code api/im/message/transpond}：转发消息（文档 §4.6）。
 * <p>
 * 字段与 {@link TranspondMessageParam} 一一对应。
 */
public final class TranspondMessageApi implements IRequestApi {

    private List<String> msgIdlist;
    private int conversation;
    private String from;
    private String to;
    private String content;
    @Nullable
    private String contentType;
    private String clientMsgId;
    @Nullable
    private String customInfo;
    @Nullable
    private List<String> target;
    private String idempotentId;
    private int isCountRead;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/message/transpond";
    }

    @NonNull
    public static TranspondMessageApi from(@NonNull TranspondMessageParam param) {
        TranspondMessageApi api = new TranspondMessageApi()
                .setMsgIdlist(param.getMsgIdlist())
                .setConversation(param.getConversation())
                .setTo(param.getTo())
                .setFrom(param.getFrom())
                .setContent(param.getContent())
                .setContentType(param.getContentType())
                .setClientMsgId(param.getClientMsgId())
                .setCustomInfo(param.getCustomInfo())
                .setTarget(param.getTarget())
                .setIdempotentId(param.getIdempotentId())
                .setIsCountRead(param.getIsCountRead());
        return api;
    }

    public TranspondMessageApi setMsgIdlist(@NonNull List<String> msgIdlist) {
        this.msgIdlist = msgIdlist;
        return this;
    }

    public TranspondMessageApi setConversation(int conversation) {
        this.conversation = conversation;
        return this;
    }

    public TranspondMessageApi setFrom(@Nullable String from) {
        this.from = from;
        return this;
    }

    public TranspondMessageApi setTo(@NonNull String to) {
        this.to = to;
        return this;
    }

    public TranspondMessageApi setContent(@Nullable String content) {
        this.content = content;
        return this;
    }

    public TranspondMessageApi setContentType(@Nullable String contentType) {
        this.contentType = contentType;
        return this;
    }

    public TranspondMessageApi setClientMsgId(@Nullable String clientMsgId) {
        this.clientMsgId = clientMsgId;
        return this;
    }

    public TranspondMessageApi setCustomInfo(@Nullable String customInfo) {
        this.customInfo = customInfo;
        return this;
    }

    public TranspondMessageApi setTarget(@Nullable List<String> target) {
        this.target = target;
        return this;
    }

    public TranspondMessageApi setIdempotentId(@Nullable String idempotentId) {
        this.idempotentId = idempotentId;
        return this;
    }

    public TranspondMessageApi setIsCountRead(int isCountRead) {
        this.isCountRead = isCountRead;
        return this;
    }
}
