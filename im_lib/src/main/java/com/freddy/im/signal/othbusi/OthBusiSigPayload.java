package com.freddy.im.signal.othbusi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.signal.ImSignalProtos;

/**
 * 0x0501 推送 body 解析结果：{@link ImSignalProtos.OthBusiSig}。
 */
public final class OthBusiSigPayload {

    private final int code;
    @Nullable
    private final String data;

    public OthBusiSigPayload(int code, @Nullable String data) {
        this.code = code;
        this.data = data;
    }

    public static OthBusiSigPayload from(@NonNull ImSignalProtos.OthBusiSig sig) {
        return new OthBusiSigPayload(sig.getCode(), sig.getData());
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public String getData() {
        return data;
    }
}
