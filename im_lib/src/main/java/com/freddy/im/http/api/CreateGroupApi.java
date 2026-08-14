package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * POST {@code /im/group/create}：创建群组（{@code ~/Downloads/api/GroupController-API.md} §1）。
 */
public final class CreateGroupApi implements IRequestApi {

    private String name;
    private List<String> members;
    private Integer conversation;
    private Integer internalFlag;
    private Integer clientType;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/create";
    }

    public CreateGroupApi setName(String name) {
        this.name = name;
        return this;
    }

    public CreateGroupApi setMembers(List<String> members) {
        this.members = members;
        return this;
    }

    public CreateGroupApi setConversation(int conversation) {
        this.conversation = conversation;
        return this;
    }

    public CreateGroupApi setInternalFlag(int internalFlag) {
        this.internalFlag = internalFlag;
        return this;
    }

    public CreateGroupApi setClientType(int clientType) {
        this.clientType = clientType;
        return this;
    }
}
