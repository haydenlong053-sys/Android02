package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/joinByInvite} */
public final class JoinByInviteApi implements IRequestApi {

    private String code;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/joinByInvite";
    }

    public JoinByInviteApi setCode(String code) {
        this.code = code;
        return this;
    }
}
