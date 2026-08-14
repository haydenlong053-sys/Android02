package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/** POST {@code api/im/group/kickOut} */
public final class KickOutGroupApi implements IRequestApi {

    private String groupId;
    private List<String> members;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/kickOut";
    }

    public KickOutGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public KickOutGroupApi setMembers(List<String> members) {
        this.members = members;
        return this;
    }
}
