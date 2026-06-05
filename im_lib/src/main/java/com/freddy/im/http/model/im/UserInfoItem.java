package com.freddy.im.http.model.im;

/**
 * 用户列表单项（5.2 {@code getUserList}）。
 */
@SuppressWarnings("unused")
public class UserInfoItem {

    private String userId;
    private String userName;
    /** 头像 URL，接口字段名可能为 icon / avatar / headImg / headUrl */
    private String icon;
    private String avatar;
    private String headImg;
    private String headUrl;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }
}
