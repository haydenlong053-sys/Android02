package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * GET {@code /im/group/detail/{groupId}}：获取群详情（{@code ~/Downloads/api/GroupController-API.md} §6）。
 */
public final class GetGroupDetailApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        String id = groupId != null ? groupId : "";
        return "im/group/detail/" + Uri.encode(id);
    }

    public GetGroupDetailApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
