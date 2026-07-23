package com.freddy.im.signal;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.TimeBroadBroadUtils;
import com.freddy.im.IMSClientFactory;
import com.freddy.im.interf.IMSClientInterface;
import com.google.gson.JsonObject;

/**
 * 其他业务信令发送封装（cmd={@link ImSignalProtos.Cmd#OTHER_BUSI} / 0x0501）。
 * <p>
 * body 为 {@link ImSignalProtos.OthBusiSig}：{@code code} + JSON {@code data}。
 * <pre>
 * // 会议礼物 code=2001，giftType：1=鲜花 2=鼓掌
 * OthBusiSignalSender.sendMeetingGift(1, groupId, null);
 * </pre>
 */
public final class OthBusiSignalSender {

    private static final String TAG = "OthBusiSignalSender";

    /** 鲜花 */
    public static final int GIFT_FLOWER = 1;
    /** 鼓掌 */
    public static final int GIFT_ACCLAMATION = 2;

    private OthBusiSignalSender() {
    }

    /**
     * 发送会议礼物信令（code={@link OthBusiSigCodes#MEETING_GIFT} / 2001）。
     *
     * @param giftType 1=鲜花，2=鼓掌
     * @param groupId  群/会议室 ID
     * @param toUserId 目标用户（可空，群广播时可不填）
     */
    public static boolean sendMeetingGift(int giftType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId) {
        return sendMeetingGift(giftType, groupId, toUserId, System.currentTimeMillis());
    }

    public static boolean sendMeetingGift(int giftType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId,
                                          long timestamp) {
        if (giftType != GIFT_FLOWER && giftType != GIFT_ACCLAMATION) {
            Log.e(TAG, "非法 giftType=" + giftType + "，仅支持 1=鲜花 / 2=鼓掌");
            return false;
        }
        String fromUserId = resolveFromUserId();
        JsonObject data = new JsonObject();
        String gid = groupId == null ? "" : groupId;
        data.addProperty("groupId", gid);
        data.addProperty("groupIdNo", gid);
        data.addProperty("fromUserId", fromUserId == null ? "" : fromUserId);
        data.addProperty("fromUserName", fromUserId == null ? "" : fromUserId);
        // 与接收端约定：giftType 1=鲜花 2=鼓掌（兼容旧字段 type）
        data.addProperty("giftType", giftType);
        data.addProperty("type", giftType);
        data.addProperty("toUserId",toUserId == null ? "" : toUserId);
        data.addProperty("timestamp", timestamp > 0L ? timestamp : TimeBroadBroadUtils.INSTANCE.getNowTime());
        return send(OthBusiSigCodes.MEETING_GIFT, data.toString());
    }

    /**
     * 通用 OthBusiSig 发送。
     *
     * @param code     业务 code，如 {@link OthBusiSigCodes#MEETING_GIFT} / 2001
     * @param dataJson data 字段 JSON 字符串
     * @return 是否已提交给 IMS
     */
    public static boolean send(int code, @Nullable String dataJson) {
        if (TextUtils.isEmpty(dataJson)) {
            Log.e(TAG, "dataJson 为空，跳过发送 code=" + code);
            return false;
        }
        IMSClientInterface client = IMSClientFactory.getIMSClient();
        if (client == null || client.isClosed()) {
            Log.e(TAG, "IMS 未就绪，跳过发送 code=" + code);
            return false;
        }
        String fromUserId = resolveFromUserId();
        if (TextUtils.isEmpty(fromUserId)) {
            Log.e(TAG, "fromUserId 为空，跳过发送 code=" + code);
            return false;
        }

        ImSignalProtos.OthBusiSig body = ImSignalProtos.OthBusiSig.newBuilder()
                .setCode(code)
                .setData(dataJson)
                .build();

        ImSignalProtos.ClientRequestHeader.Builder header = ImSignalProtos.ClientRequestHeader.newBuilder()
                .setVersion(1)
                .setUserId(fromUserId)
                .setClientType(ImSignalProtos.ClientType.ANDROID_VALUE)
                .setCmd(ImSignalProtos.Cmd.OTHER_BUSI_VALUE);
        String token = MMKVBroadUtils.Companion.getMyImToken();
        if (!TextUtils.isEmpty(token)) {
            header.setToken(token);
        }

        ImSignalProtos.SignalRequest request = ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(header.build())
                .setBody(body.toByteString())
                .build();

        eLogD(TAG, "send OTHER_BUSI(0x0501) code=" + code + " data=" + dataJson);
        client.sendSignalRequest(request, false);
        return true;
    }

    @Nullable
    private static String resolveFromUserId() {
        return MMKVBroadUtils.Companion.getRealUserId();
    }
}
