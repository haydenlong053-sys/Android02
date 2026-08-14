package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/transfer} */
public final class TransferGroupApi implements IRequestApi {

    private String groupId;
    private String newOwer;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/transfer";
    }

    public TransferGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public TransferGroupApi setNewOwer(String newOwer) {
        this.newOwer = newOwer;
        return this;
    }
}
