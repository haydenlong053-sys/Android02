package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code api/im/group/getGroupWelcome/{groupId}} */
public final class GetGroupWelcomeApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        String id = groupId != null ? groupId : "";
        return "im/group/getGroupWelcome/" + Uri.encode(id);
    }

    public GetGroupWelcomeApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
