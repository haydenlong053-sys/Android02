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
    private String username;
    private String idNo;
    private String headPortrait;
    private int statusInGroup;
    private int role;
    private long joinTime;
    private long registerTime;
    private String lianghao;
    private String isvip;
    private String grade;
    private String userExtAttrs;
    private boolean deleted;

    /** 消息列表展示名：nickname → username → userId */
    public String getDisplayName() {
        if (nickname != null && !nickname.isEmpty()) {
            return nickname.trim();
        }
        if (username != null && !username.isEmpty()) {
            return username.trim();
        }
        return userId != null ? userId : "";
    }

    public boolean isVipMember() {
        return "1".equals(isvip);
    }

    public boolean isNiceNumMember() {
        return lianghao != null && !lianghao.isEmpty();
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
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

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public String getLianghao() {
        return lianghao;
    }

    public void setLianghao(String lianghao) {
        this.lianghao = lianghao;
    }

    public String getIsvip() {
        return isvip;
    }

    public void setIsvip(String isvip) {
        this.isvip = isvip;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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
