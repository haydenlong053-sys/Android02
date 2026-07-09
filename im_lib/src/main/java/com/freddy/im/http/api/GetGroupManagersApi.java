package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/managers} */
public final class GetGroupManagersApi implements IRequestApi {

    private String groupId;
    private String userId;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/managers";
    }

    public GetGroupManagersApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public GetGroupManagersApi setUserId(@NonNull String userId) {
        this.userId = userId;
        return this;
    }
}
