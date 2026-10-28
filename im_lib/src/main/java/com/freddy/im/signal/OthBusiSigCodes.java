package com.freddy.im.signal;

/**
 * {@link ImSignalProtos.OthBusiSig} 多端约定业务 code。
 */
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
     * 群置顶消息变更（文档 groupPinned），data 含
     * {@code kind=group_pin,groupId,action,messageId,operator}；客户端重拉 pinnedMessages。
     */
    public static final int GROUP_PIN = 1010;

    /**
     * 会话分组变更（文档 convFolders），data 为
     * {@code {"kind":"conv_folder","version":8}}；本地 version 已 ≥ 则忽略，否则重拉。
     */
    public static final int CONV_FOLDER = 1011;

    /**
     * 会议礼物弹幕（cmd=OTHER_BUSI / 0x0501，code=2001）。
     * data 为 {@code {"giftType":1|2,"groupId":"...","fromUserId":"...","toUserId":"...","timestamp":0}}，
     * giftType：1=鲜花，2=鼓掌。
     */
    public static final int MEETING_GIFT = 2001;

    /**
     * 信用分变更，data 为 JSON 数组
     * {@code [{"u":"userId","s":"100.58"}, ...]}（{@code u}=userId，{@code s}=信用分）。
     */
    public static final int CREDIT_SCORE = 3001;

    // ---------- 会议室（与野火 MessageContentBroadType 4010~4040 对齐，勿跨模块引用 chat-client） ----------

    /** 会议开始，data 为 {@code {"groupIdNo":"..."}}。 */
    public static final int MEET_START = 4010;

    /** 会议结束，data 为 {@code {"groupIdNo":"..."}}。 */
    public static final int MEET_END = 4020;

    /** 上麦申请（推送给房主/管理员），data 为 {@code {"applicantUserId":"...","groupIdNo":"..."}}。 */
    public static final int MEET_UP_MIC_APPLY = 4030;

    /** 麦位变更，data 为 {@code {"refreshType":"micSeat","groupIdNo":"..."}}。 */
    public static final int MEET_SEAT_CHANGE = 4040;

    /** 会议倒计时提醒（推送给房主），data 为 {@code {"time":"分钟","amount":"DUALX价格"}}，对齐野火 6011。 */
    public static final int MEET_PRE_END_TIP = 6011;

    /**
     * 消息编辑（文档 msgEdit），data 含
     * {@code kind=msg_edit,messageId,content,customInfo,editedAt,status}（可选 groupId）。
     */
    public static final int MSG_EDIT = 6001;

    private OthBusiSigCodes() {
    }
}
