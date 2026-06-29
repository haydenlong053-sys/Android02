package com.freddy.im.signal.othbusi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 0x0501 {@link com.freddy.im.signal.ImSignalProtos.OthBusiSig} 按 code 分发的业务处理器。
 * <p>
 * 新增业务：实现本接口并在应用层 {@link OthBusiSigDispatcher#register} 即可。
 */
public interface OthBusiSigHandler {

    /** 支持的 {@link OthBusiSigPayload#getCode()}。 */
    int code();

    /**
     * @param data {@link OthBusiSigPayload#getData()} 原始 JSON 字符串
     * @return true 表示已识别并处理
     */
    boolean handle(@Nullable String data);
}
