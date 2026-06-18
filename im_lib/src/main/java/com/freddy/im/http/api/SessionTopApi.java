package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/conversation/sessionTop}（Query: sessionId, flag）。 */
public final class SessionTopApi implements IRequestApi {

    private String sessionId;
    private boolean flag;

    @NonNull
    @Override
    public String getApi() {
        String id = sessionId != null ? sessionId : "";
        return "api/im/conversation/sessionTop?sessionId="
                + Uri.encode(id)
                + "&flag="
                + flag;
    }

    public SessionTopApi setSessionId(@NonNull String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public SessionTopApi setFlag(boolean flag) {
        this.flag = flag;
        return this;
    }
}
