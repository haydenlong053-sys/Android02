package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/disbanded} */
public final class DisbandGroupApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/disbanded";
    }

    public DisbandGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }
}
