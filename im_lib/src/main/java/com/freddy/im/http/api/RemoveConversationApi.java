package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * GET {@code im/conversation/remove/{sessionId}}：移除会话。
 */
public final class RemoveConversationApi implements IRequestApi {

    private String sessionId;

    @NonNull
    @Override
    public String getApi() {
        String id = sessionId != null ? sessionId : "";
        return "api/im/conversation/remove/" + Uri.encode(id);
    }

    public RemoveConversationApi setSessionId(@NonNull String sessionId) {
        this.sessionId = sessionId;
        return this;
    }
}
