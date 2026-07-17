package com.freddy.im.db.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 群成员表（Realm）；主键 {@code memberKey = groupId + "_" + userId}。
 */
public class GroupMemberEntity extends RealmObject {

    @PrimaryKey
    private String memberKey;
    private String groupId;
    private String userId;
    private String companyId;
    private String nickname;
    /** 群内昵称 */
    private String groupNickname;
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
    /** 软删除：退群 / 被踢 */
    private boolean deleted;

    public String getMemberKey() {
        return memberKey;
    }

    public void setMemberKey(String memberKey) {
        this.memberKey = memberKey;
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

    public String getGroupNickname() {
        return groupNickname;
    }

    public void setGroupNickname(String groupNickname) {
        this.groupNickname = groupNickname;
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
