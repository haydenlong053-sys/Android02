package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code api/im/group/getGroupNotice/{groupId}} */
public final class GetGroupNoticeApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        String id = groupId != null ? groupId : "";
        return "im/group/getGroupNotice/" + Uri.encode(id);
    }

    public GetGroupNoticeApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
