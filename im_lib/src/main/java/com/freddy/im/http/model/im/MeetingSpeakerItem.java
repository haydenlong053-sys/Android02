package com.freddy.im.http.model.im;

/**
 * 会议麦上 / 申请名单条目（字段按服务端实际返回做宽松解析）。
 */
@SuppressWarnings("unused")
public class MeetingSpeakerItem implements java.io.Serializable {

    private String userId;
    private String idNo;
    private String nickname;
    private String name;
    private String icon;
    private String avatar;
    private String role;
    private Integer seatIndex;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(Integer seatIndex) {
        this.seatIndex = seatIndex;
    }

    public String displayName() {
        if (nickname != null && !nickname.isEmpty()) {
            return nickname;
        }
        return name;
    }

    public String displayIcon() {
        if (icon != null && !icon.isEmpty()) {
            return icon;
        }
        return avatar;
    }
}
