package com.freddy.im.signal.othbusi;

import androidx.annotation.Nullable;

import com.freddy.im.signal.ImSignalProtos;

/**
 * 0x0501 {@link ImSignalProtos.OthBusiSig} body 解析。
 */
public final class OthBusiSigHelper {

    private OthBusiSigHelper() {
    }

    @Nullable
    public static OthBusiSigPayload parse(@Nullable byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            ImSignalProtos.OthBusiSig sig = ImSignalProtos.OthBusiSig.parseFrom(body);
            return OthBusiSigPayload.from(sig);
        } catch (Exception ignored) {
            return null;
        }
    }
}
