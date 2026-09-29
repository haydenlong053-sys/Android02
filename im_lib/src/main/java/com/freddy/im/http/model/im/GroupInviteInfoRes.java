package com.freddy.im.http.model.im;

/**
 * GET {@code api/im/group/inviteInfo/{code}} 响应 content。
 * 预览字段：群名 / 头像 / 人数 / needVerify / alreadyMember（加入前不暴露成员名单）。
 */
public class GroupInviteInfoRes {

    /** 与群详情一致时可能直接叫 id */
    private String groupId;
    private String name;
    private String icon;
    private int memCount;
    private boolean needVerify;
    private boolean alreadyMember;


    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }


    public String getIcon() {
        return icon;
    }

    public int getMemCount() {
        return memCount;
    }

    public boolean isNeedVerify() {
        return needVerify;
    }

    public boolean isAlreadyMember() {
        return alreadyMember;
    }

    /** 进群聊用的群 ID。 */
    public String resolveGroupId() {
        if (groupId != null && !groupId.isEmpty()) {
            return groupId;
        }
        return "";
    }

    /** 弹框描述用群名。 */
    public String displayGroupName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return "";
    }
}
