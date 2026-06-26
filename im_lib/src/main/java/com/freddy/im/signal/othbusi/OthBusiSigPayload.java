package com.freddy.im.signal.othbusi;

import androidx.annotation.NonNull;

import com.freddy.im.signal.ImSignalProtos;

/**
 * 0x0501 推送 body 解析结果：{@link ImSignalProtos.OthBusiSig}。
 */
public final class OthBusiSigPayload {

    private final int code;
    @NonNull
    private final String data;

    public OthBusiSigPayload(int code, @NonNull String data) {
        this.code = code;
        this.data = data;
    }

    public static OthBusiSigPayload from(@NonNull ImSignalProtos.OthBusiSig sig) {
        return new OthBusiSigPayload(sig.getCode(), sig.getData());
    }

    public int getCode() {
        return code;
    }

    @NonNull
    public String getData() {
        return data;
    }
}
