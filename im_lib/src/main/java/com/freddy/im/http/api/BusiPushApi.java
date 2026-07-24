package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code api/im/notify/busiPush}：业务推送（如会议礼物弹幕 code=2001）。
 * <p>
 * 请求体：{@code groupId}、{@code code}、{@code data}（JSON 字符串）。
 */
public final class BusiPushApi implements IRequestApi {

    private String groupId;
    private int code;
    @Nullable
    private String data;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/notify/busiPush";
    }

    public BusiPushApi setGroupId(@Nullable String groupId) {
        this.groupId = groupId;
        return this;
    }

    public BusiPushApi setCode(int code) {
        this.code = code;
        return this;
    }

    public BusiPushApi setData(@Nullable String data) {
        this.data = data;
        return this;
    }
}
