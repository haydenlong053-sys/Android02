package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code api/im/group/getAllMembers/{groupId}} */
public final class GetAllGroupMembersApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        String id = groupId != null ? groupId : "";
        return "im/group/getAllMembers/" + Uri.encode(id);
    }

    public GetAllGroupMembersApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
