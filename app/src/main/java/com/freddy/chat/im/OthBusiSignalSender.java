package com.freddy.chat.im;

import androidx.annotation.Nullable;

/**
 * @deprecated 请改用 {@link com.freddy.im.signal.OthBusiSignalSender}（已迁至 im_lib，对齐 CallSignalSender 发送方式）。
 */
@Deprecated
public final class OthBusiSignalSender {

    /** @deprecated 使用 {@link com.freddy.im.signal.OthBusiSignalSender#GIFT_FLOWER} */
    @Deprecated
    public static final String GIFT_TYPE_MEETING = "meeting_gift";

    private OthBusiSignalSender() {
    }

    /** 会议礼物：giftType 1=鲜花 2=鼓掌，code=2001。 */
    public static boolean sendMeetingGift(int giftType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId) {
        return com.freddy.im.signal.OthBusiSignalSender.sendMeetingGift(giftType, groupId, toUserId);
    }

    /**
     * @deprecated 请改用 {@link #sendMeetingGift(int, String, String)}，giftType 传 1/2。
     */
    @Deprecated
    public static boolean sendMeetingGift(@Nullable String jsonType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId) {
        int giftType = 0;
        if (jsonType != null) {
            try {
                giftType = Integer.parseInt(jsonType.trim());
            } catch (NumberFormatException ignored) {
                giftType = 0;
            }
        }
        return com.freddy.im.signal.OthBusiSignalSender.sendMeetingGift(giftType, groupId, toUserId);
    }

    /**
     * @deprecated 请改用 {@link #sendMeetingGift(int, String, String)}。
     */
    @Deprecated
    public static boolean sendMeetingGift(@Nullable String jsonType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId,
                                          @Nullable String giftType,
                                          long timestamp) {
        int type = 0;
        if (jsonType != null) {
            try {
                type = Integer.parseInt(jsonType.trim());
            } catch (NumberFormatException ignored) {
                type = 0;
            }
        }
        return com.freddy.im.signal.OthBusiSignalSender.sendMeetingGift(type, groupId, toUserId, timestamp);
    }

    public static boolean send(int code, @Nullable String dataJson) {
        return com.freddy.im.signal.OthBusiSignalSender.send(code, dataJson);
    }
}
