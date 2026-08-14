package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/** POST {@code api/im/group/setManager} */
public final class SetGroupManagerApi implements IRequestApi {

    private String groupId;
    private List<String> userIdList;
    private Boolean manager;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/setManager";
    }

    public SetGroupManagerApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public SetGroupManagerApi setUserIdList(@NonNull List<String> userIdList) {
        this.userIdList = userIdList;
        return this;
    }

    public SetGroupManagerApi setManager(boolean manager) {
        this.manager = manager;
        return this;
    }
}
