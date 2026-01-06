package io.agora.auikit.model;

import android.text.TextUtils;

public enum AUICustomMsgBroadType {
    /**
     * 系统消息 成员加入
     */
    AUIChatRoomJoinedMember("AUIChatRoomJoinedMember"),

    ;

    private String name;

    AUICustomMsgBroadType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AUICustomMsgBroadType fromName(String name) {
        for (AUICustomMsgBroadType type : AUICustomMsgBroadType.values()) {
            if(TextUtils.equals(type.getName(), name)) {
                return type;
            }
        }
        return null;
    }
}
