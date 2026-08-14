package com.freddy.im.http.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code api/im/group/getGroupQrCode/{groupId}} */
public final class GetGroupQrCodeApi implements IRequestApi {

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        String id = groupId != null ? groupId : "";
        return "im/group/getGroupQrCode/" + Uri.encode(id);
    }

    public GetGroupQrCodeApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }
}
