package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/applyInGroup} */
public final class ApplyInGroupApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/applyInGroup";
    }

    public ApplyInGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }
}
