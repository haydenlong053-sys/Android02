package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/conversation/muteNotify}（Query: sessionId, flag）。 */
public final class MuteNotifyApi implements IRequestApi {

    private String sessionId;
    private boolean flag;

    @NonNull
    @Override
    public String getApi() {
        String id = sessionId != null ? sessionId : "";
        // 与协议 ConvMultiSync.muteNotify（int32）一致，服务端按 1/0 解析
        return "api/im/conversation/muteNotify?sessionId="
                + Uri.encode(id)
                + "&flag="
                + (flag ? "1" : "0");
    }

    public MuteNotifyApi setSessionId(@NonNull String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public MuteNotifyApi setFlag(boolean flag) {
        this.flag = flag;
        return this;
    }
}
