package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/** POST {@code api/im/group/addMembers} */
public final class GroupFriendsApi implements IRequestApi {

    private String groupId;
    private String id;
    private String key;
    private String name;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/friend/friendList";
    }

    public GroupFriendsApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public GroupFriendsApi setId(String id) {
        this.id = id;
        return this;
    }

    public GroupFriendsApi setKey(String key) {
        this.key = key;
        return this;
    }

    public GroupFriendsApi setName(String name) {
        this.name = name;
        return this;
    }
}
