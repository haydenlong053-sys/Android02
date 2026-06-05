package com.freddy.im.http.model.im;

/** {@code inviteInGroup} 请求体成员项。 */
public class InviteMemberItem {

    private String userId;

    public InviteMemberItem() {
    }

    public InviteMemberItem(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
