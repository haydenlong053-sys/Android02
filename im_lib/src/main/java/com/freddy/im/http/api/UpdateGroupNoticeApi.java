package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/updateGroupNotice} */
public final class UpdateGroupNoticeApi implements IRequestApi {

    private String groupId;
    private String content;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/updateGroupNotice";
    }

    public UpdateGroupNoticeApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public UpdateGroupNoticeApi setContent(String content) {
        this.content = content;
        return this;
    }
}
