package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code api/im/group/inviteInfo/{code}} */
public final class GetGroupInviteInfoApi implements IRequestApi {

    private String code;

    @NonNull
    @Override
    public String getApi() {
        String id = code != null ? code : "";
        return "im/group/inviteInfo/" + Uri.encode(id);
    }

    public GetGroupInviteInfoApi setCode(@NonNull String code) {
        this.code = code;
        return this;
    }
}
