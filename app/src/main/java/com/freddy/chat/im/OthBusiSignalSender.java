package com.freddy.chat.im;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.signal.ImSignalProtos;
import com.freddy.im.signal.OthBusiSigCodes;
import com.google.gson.JsonObject;

/**
 * 其他业务信令发送封装（cmd={@link ImSignalProtos.Cmd#OTHER_BUSI} / 0x0501）。
 * <p>
 * body 为 {@link ImSignalProtos.OthBusiSig}：{@code code} + JSON {@code data}。
 * <pre>
 * // 会议礼物 code=2001
 * OthBusiSignalSender.sendMeetingGift(jsonType, groupId, toUserId);
 *
 * // 通用
 * OthBusiSignalSender.send(code, dataJson);
 * </pre>
 */
public final class OthBusiSignalSender {

    private static final String TAG = "OthBusiSignalSender";

    public static final String GIFT_TYPE_MEETING = "meeting_gift";

    private OthBusiSignalSender() {
    }

    /**
     * 发送会议礼物信令（code={@link OthBusiSigCodes#MEETING_GIFT} / 2001）。
     *
     * @param jsonType  业务 type 字段
     * @param groupId   群/会议室 ID
     * @param toUserId  目标用户
     */
    public static boolean sendMeetingGift(@Nullable String jsonType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId) {
        return sendMeetingGift(jsonType, groupId, toUserId, GIFT_TYPE_MEETING, System.currentTimeMillis());
    }

    public static boolean sendMeetingGift(@Nullable String jsonType,
                                          @Nullable String groupId,
                                          @Nullable String toUserId,
                                          @Nullable String giftType,
                                          long timestamp) {
        String fromUserId = resolveFromUserId();
        JsonObject data = new JsonObject();
        data.addProperty("type", jsonType == null ? "" : jsonType);
        String gid = groupId == null ? "" : groupId;
        data.addProperty("groupId", gid);
        // 与会议室其它信令对齐，便于服务端按群广播
        data.addProperty("groupIdNo", gid);
        data.addProperty("fromUserId", fromUserId == null ? "" : fromUserId);
        data.addProperty("giftType", TextUtils.isEmpty(giftType) ? GIFT_TYPE_MEETING : giftType);
        data.addProperty("toUserId", toUserId == null ? "" : toUserId);
        data.addProperty("timestamp", timestamp > 0L ? timestamp : System.currentTimeMillis());
        return send(OthBusiSigCodes.MEETING_GIFT, data.toString());
    }

    /**
     * 通用 OthBusiSig 发送。
     *
     * @param code     业务 code，如 {@link OthBusiSigCodes#MEETING_GIFT}
     * @param dataJson data 字段 JSON 字符串
     * @return 是否已提交给 IMS
     */
    public static boolean send(int code, @Nullable String dataJson) {
        if (TextUtils.isEmpty(dataJson)) {
            Log.e(TAG, "dataJson 为空，跳过发送 code=" + code);
            return false;
        }
        IMSClientBootstrap bootstrap = IMSClientBootstrap.getInstance();
        if (!bootstrap.isActive()) {
            Log.e(TAG, "IMS 未激活，跳过发送 code=" + code);
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
        String token = bootstrap.getBindToken();
        if (!TextUtils.isEmpty(token)) {
            header.setToken(token);
        }

        ImSignalProtos.SignalRequest request = ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(header.build())
                .setBody(body.toByteString())
                .build();

        Log.d(TAG, "send OTHER_BUSI code=" + code + " data=" + dataJson);
        bootstrap.sendSignalRequest(request, false);
        return true;
    }

    @Nullable
    private static String resolveFromUserId() {
        String fromUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(fromUserId)) {
            fromUserId = MMKVBroadUtils.Companion.getRealUserId();
        }
        return fromUserId;
    }
}
