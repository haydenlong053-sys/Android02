package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/modify} */
public final class ModifyGroupApi implements IRequestApi {

    private String groupId;
    private String name;
    private String icon;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/modify";
    }

    public ModifyGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public ModifyGroupApi setName(String name) {
        this.name = name;
        return this;
    }

    public ModifyGroupApi setIcon(String icon) {
        this.icon = icon;
        return this;
    }
}
