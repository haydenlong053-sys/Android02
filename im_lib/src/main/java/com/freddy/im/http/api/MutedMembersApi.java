package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/mutedMembers} */
public final class MutedMembersApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/mutedMembers";
    }

    public MutedMembersApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
