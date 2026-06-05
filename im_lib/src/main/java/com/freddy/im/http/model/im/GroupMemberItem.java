package com.freddy.im.http.model.im;

/**
 * 群成员 DTO（HTTP / 本地库 / UI 共用）。
 */
@SuppressWarnings("unused")
public class GroupMemberItem {

    private String groupId;
    private String userId;
    private String companyId;
    private String nickname;
    private String headPortrait;
    private int statusInGroup;
    private int role;
    private long joinTime;
    private String userExtAttrs;
    private boolean deleted;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }

    public int getStatusInGroup() {
        return statusInGroup;
    }

    public void setStatusInGroup(int statusInGroup) {
        this.statusInGroup = statusInGroup;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public String getUserExtAttrs() {
        return userExtAttrs;
    }

    public void setUserExtAttrs(String userExtAttrs) {
        this.userExtAttrs = userExtAttrs;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
