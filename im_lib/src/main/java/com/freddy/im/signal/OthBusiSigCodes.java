package com.freddy.im.signal;

/** {@link ImSignalProtos.OthBusiSig} 多端约定业务 code。 */
public final class OthBusiSigCodes {

    /** 群公告变更通知，data 为 {@code {"groupId":"..."}}。 */
    public static final int GROUP_NOTICE = 1001;

    /** 全员禁言变更，data 为 {@code {"groupId":"...","allMute":0|1,"operator":"..."}}。 */
    public static final int GROUP_ALL_MUTE = 1002;

    /** 指定成员禁言变更（推送给被禁言成员），data 为 {@code {"groupId":"...","mute":0|1,"operator":"..."}}。 */
    public static final int GROUP_MEMBER_MUTE = 1003;

    private OthBusiSigCodes() {
    }
}
