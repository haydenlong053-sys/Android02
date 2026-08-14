package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/getList} */
public final class GetGroupListApi implements IRequestApi {

    private Integer anchor;
    private Integer isContainSingle;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/getList";
    }

    public GetGroupListApi setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public GetGroupListApi setIsContainSingle(int isContainSingle) {
        this.isContainSingle = isContainSingle;
        return this;
    }
}
