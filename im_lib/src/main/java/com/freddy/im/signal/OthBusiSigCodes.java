package com.freddy.im.signal;

/** {@link ImSignalProtos.OthBusiSig} 多端约定业务 code。 */
public final class OthBusiSigCodes {

    /** 群公告变更通知，data 为 {@code {"groupId":"..."}}。 */
    public static final int GROUP_NOTICE = 1001;

    /** 全员禁言变更，data 为 {@code {"groupId":"...","allMute":0|1,"operator":"..."}}。 */
    public static final int GROUP_ALL_MUTE = 1002;

    /** 指定成员禁言变更（推送给被禁言成员），data 为 {@code {"groupId":"...","mute":0|1,"operator":"..."}}。 */
    public static final int GROUP_MEMBER_MUTE = 1003;

    /** 群管理员角色变更，data 为 {@code {"groupId":"...","userIdList":[...],"newRole":1|2}}（1 管理员，2 普通成员）。 */
    public static final int GROUP_MANAGER_ROLE = 1004;

    /**
     * 会议礼物等业务通知，data 为
     * {@code {"type":"...","groupId":"...","fromUserId":"...","giftType":"meeting_gift","toUserId":"...","timestamp":0}}。
     */
    public static final int MEETING_GIFT = 2001;

    // ---------- 会议室（与野火 MessageContentBroadType 4010~4040 对齐，勿跨模块引用 chat-client） ----------

    /** 会议开始，data 为 {@code {"groupIdNo":"..."}}。 */
    public static final int MEET_START = 4010;

    /** 会议结束，data 为 {@code {"groupIdNo":"..."}}。 */
    public static final int MEET_END = 4020;

    /** 上麦申请（推送给房主/管理员），data 为 {@code {"applicantUserId":"...","groupIdNo":"..."}}。 */
    public static final int MEET_UP_MIC_APPLY = 4030;

    /** 麦位变更，data 为 {@code {"refreshType":"micSeat","groupIdNo":"..."}}。 */
    public static final int MEET_SEAT_CHANGE = 4040;

    /** 会议倒计时提醒（推送给房主），data 为 {@code {"time":"分钟","amount":"ODIC价格"}}，对齐野火 6011。 */
    public static final int MEET_PRE_END_TIP = 6011;

    private OthBusiSigCodes() {
    }
}
