package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/leave} */
public final class LeaveGroupApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/leave";
    }

    public LeaveGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }
}
