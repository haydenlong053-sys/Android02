package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.freddy.im.http.model.im.InviteMemberItem;
import com.hjq.http.config.IRequestApi;

import java.util.List;

/** POST {@code api/im/group/inviteInGroup} */
public final class InviteInGroupApi implements IRequestApi {

    private String groupId;
    private List<InviteMemberItem> inviteMemberList;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/inviteInGroup";
    }

    public InviteInGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public InviteInGroupApi setInviteMemberList(List<InviteMemberItem> inviteMemberList) {
        this.inviteMemberList = inviteMemberList;
        return this;
    }
}
