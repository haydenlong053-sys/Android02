package com.freddy.im.group;

/**
 * 群成员角色（自研 IM 文档）：0 群主、1 管理员、2 普通成员。
 */
public final class GroupRole {

    public static final int OWNER = 0;
    public static final int ADMIN = 1;
    public static final int MEMBER = 2;

    private GroupRole() {
    }

    public static boolean isOwner(int role) {
        return role == OWNER;
    }

    public static boolean isAdmin(int role) {
        return role == ADMIN;
    }

    public static boolean isOwnerOrAdmin(int role) {
        return role == OWNER || role == ADMIN;
    }

    /** 映射野火 UI 角标 type：群主 "1"、管理员 "2"、成员 ""。 */
    public static String toWildfireUiType(int role) {
        if (role == OWNER) {
            return "1";
        }
        if (role == ADMIN) {
            return "2";
        }
        return "";
    }
}
