package com.freddy.im.http.model.im;

import java.util.List;

/**
 * 群成员 GroupUserVO（见 {@code ~/Downloads/api/GroupController-API.md}「数据模型」）。
 */
@SuppressWarnings("unused")
public class GroupUserVo {

    private String userId;
    private String companyId;
    /** 用户昵称 / 好友侧展示名（非群内专属） */
    private String nickname;
    /** 群内昵称（文档字段；群内展示优先） */
    private String groupNickname;
    /** 接口部分场景返回 username */
    private String username;
    private String idNo;
    private int statusInGroup;
    //角色 0:群主 1:管理员 2:普通成员
    private int role;
    private Long joinTime;
    private Long registerTime;
    private String lianghao;
    private String isvip;
    private Double grade;
    private String headPortrait;
    private List<String> busiRoles;
    private String userExtAttrs;

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

    /** 群内展示名：groupNickname → nickname → username → userId */
    public String getDisplayName() {
        if (groupNickname != null && !groupNickname.isEmpty()) {
            return groupNickname;
        }
        if (nickname != null && !nickname.isEmpty()) {
            return nickname;
        }
        if (username != null && !username.isEmpty()) {
            return username;
        }
        return userId != null ? userId : "";
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

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public Long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Long registerTime) {
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

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }

    public List<String> getBusiRoles() {
        return busiRoles;
    }

    public void setBusiRoles(List<String> busiRoles) {
        this.busiRoles = busiRoles;
    }

    public String getUserExtAttrs() {
        return userExtAttrs;
    }

    public void setUserExtAttrs(String userExtAttrs) {
        this.userExtAttrs = userExtAttrs;
    }
}
