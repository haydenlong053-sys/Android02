package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code im/group/pinnedMessages/{groupId}}（文档 § groupPinned）。 */
public final class GetPinnedMessagesApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        String id = groupId != null ? groupId : "";
        return "im/group/pinnedMessages/" + Uri.encode(id);
    }

    public GetPinnedMessagesApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
