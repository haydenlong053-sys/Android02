package com.freddy.im.signal.othbusi;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 0x0501 OthBusiSig 按 code 路由到已注册的 {@link OthBusiSigHandler}。
 */
public final class OthBusiSigDispatcher {

    private static final String TAG = "OthBusiSigDispatcher";

    private final SparseArray<OthBusiSigHandler> handlers = new SparseArray<>();

    public void register(@NonNull OthBusiSigHandler handler) {
        handlers.put(handler.code(), handler);
    }

    /**
     * 解析 body 并按 code 分发；未注册的 code 仅打日志。
     */
    public void dispatch(@Nullable byte[] body) {
        OthBusiSigPayload payload = OthBusiSigHelper.parse(body);
        if (payload == null) {
            return;
        }
        OthBusiSigHandler handler = handlers.get(payload.getCode());
        if (handler == null) {
            Log.w(TAG, "未注册的 OthBusiSig code=" + payload.getCode());
            return;
        }
        try {
            if (!handler.handle(payload.getData())) {
                Log.w(TAG, "OthBusiSig 处理未生效 code=" + payload.getCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "OthBusiSig 处理异常 code=" + payload.getCode(), e);
        }
    }
}
