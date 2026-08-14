package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code api/im/group/getGroupByCodeId/{codeId}} */
public final class GetGroupByCodeIdApi implements IRequestApi {

    private String codeId;

    @NonNull
    @Override
    public String getApi() {
        String id = codeId != null ? codeId : "";
        return "im/group/getGroupByCodeId/" + Uri.encode(id);
    }

    public GetGroupByCodeIdApi setCodeId(@NonNull String codeId) {
        this.codeId = codeId;
        return this;
    }
}
