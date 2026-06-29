package com.freddy.im.http;

/** 群禁言等业务错误码（与发消息 / 管理接口一致）。 */
public final class ImGroupBizErrorCodes {

    /** 群内已开启全员禁言（普通成员发消息）。 */
    public static final int GROUP_ALL_MUTED = 2017;
    /** 当前用户被单独禁言。 */
    public static final int GROUP_MEMBER_MUTED = 2018;
    /** 无权限：仅群主或管理员可操作禁言。 */
    public static final int NO_MUTE_PERMISSION = 3035;

    private ImGroupBizErrorCodes() {
    }

    public static boolean isMuteSendError(int errorCode) {
        return errorCode == GROUP_ALL_MUTED || errorCode == GROUP_MEMBER_MUTED;
    }
}
