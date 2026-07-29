package com.freddy.im.call;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.IMSClientFactory;
import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.signal.ImSignalProtos;
import com.google.protobuf.ByteString;

import java.util.Collections;
import java.util.List;

/**
 * 通话 IM 信令发送封装（cmd={@link ImSignalProtos.Cmd#CALL_SIGNAL} / 0x0502）。
 */
public final class CallSignalSender {

    private static final String TAG = "CallSignalSender";

    public static final String MEDIA_AUDIO = "audio";
    public static final String MEDIA_VIDEO = "video";
    public static final String MODE_SFU = "sfu";
    public static final String MODE_MESH = "mesh";

    private CallSignalSender() {
    }

    /** 发起 / 邀请通话（CALL_INVITE）。 */
    public static boolean invite(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return invite(callId, Collections.singletonList(toUserId), roomName, video, null, null);
    }

    public static boolean invite(@NonNull String callId,
                                 @NonNull List<String> toUserIds,
                                 @Nullable String roomName,
                                 boolean video,
                                 @Nullable String groupId,
                                 @Nullable String mode) {
        return send(
                callId,
                ImSignalProtos.CallSignalType.CALL_INVITE_VALUE,
                toUserIds,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                groupId,
                TextUtils.isEmpty(mode) ? MODE_SFU : mode,
                null
        );
    }

    /** 主叫接通前取消（CALL_CANCEL）。 */
    public static boolean cancel(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_CANCEL_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /** 挂断 / 离开本通（CALL_HANGUP）。 */
    public static boolean hangup(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_HANGUP_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /** 被叫拒绝（CALL_REJECT=3）。 */
    public static boolean reject(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_REJECT_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /** 被叫振铃回执（CALL_RINGING）。 */
    public static boolean ringing(@NonNull String callId,
                                  @NonNull String toUserId,
                                  @Nullable String roomName,
                                  boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_RINGING_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /** 被叫接听（CALL_ACCEPT）。 */
    public static boolean accept(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_ACCEPT_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /** 忙线（CALL_BUSY）。 */
    public static boolean busy(@NonNull String callId,
                               @NonNull String toUserId,
                               @Nullable String roomName,
                               boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_BUSY_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /** 整通结束（CALL_END）。 */
    public static boolean end(@NonNull String callId,
                              @NonNull String toUserId,
                              @Nullable String roomName,
                              boolean video) {
        return sendSingle(
                callId,
                ImSignalProtos.CallSignalType.CALL_END_VALUE,
                toUserId,
                roomName,
                video ? MEDIA_VIDEO : MEDIA_AUDIO,
                null
        );
    }

    /**
     * 通用发送入口。
     *
     * @param type {@link ImSignalProtos.CallSignalType} 对应 VALUE
     * @return 是否已提交给 IMS（不代表对端已收到）
     */
    public static boolean send(@NonNull String callId,
                               int type,
                               @Nullable List<String> toUserIds,
                               @Nullable String roomName,
                               @Nullable String mediaType,
                               @Nullable String groupId,
                               @Nullable String mode,
                               @Nullable String payload) {
        if (TextUtils.isEmpty(callId)) {
            Log.e(TAG, "callId 为空，跳过发送 type=" + type);
            return false;
        }
        IMSClientInterface client = IMSClientFactory.getIMSClient();
        if (client == null || client.isClosed()) {
            Log.e(TAG, "IMS 未就绪，跳过发送 type=" + type);
            return false;
        }
        String fromUserId = MMKVBroadUtils.Companion.getRealUserId();
        if (TextUtils.isEmpty(fromUserId)) {
            Log.e(TAG, "fromUserId 为空，跳过发送 type=" + type);
            return false;
        }

        ImSignalProtos.CallSignal.Builder body = ImSignalProtos.CallSignal.newBuilder()
                .setCallId(callId)
                .setType(type)
                .setFromUserId(fromUserId)
                .setRoomName(roomName)
                .setTimestamp(System.currentTimeMillis());

        String fromUsername = MMKVBroadUtils.Companion.getUserName();
        if (!TextUtils.isEmpty(fromUsername)) {
            body.setFromUsername(fromUsername);
        }
        String fromUserIcon = MMKVBroadUtils.Companion.getUserImg();
        if (!TextUtils.isEmpty(fromUserIcon)) {
            body.setFromUserIcon(fromUserIcon);
        }
        if (!TextUtils.isEmpty(mediaType)) {
            body.setMediaType(mediaType);
        }
        if (!TextUtils.isEmpty(mode)) {
            body.setMode(mode);
        }
        if (!TextUtils.isEmpty(roomName)) {
            body.setRoomName(roomName);
        } else {
            body.setRoomName(callId);
        }
        if (!TextUtils.isEmpty(groupId)) {
            body.setGroupId(groupId);
        }
        if (!TextUtils.isEmpty(payload)) {
            body.setPayload(payload);
        }
        if (toUserIds != null && !toUserIds.isEmpty()) {
            if (toUserIds.size() == 1 && !TextUtils.isEmpty(toUserIds.get(0))) {
                body.setToUserId(toUserIds.get(0));
            }
            for (String uid : toUserIds) {
                if (!TextUtils.isEmpty(uid)) {
                    body.addToUserIds(uid);
                }
            }
        }

        ImSignalProtos.ClientRequestHeader.Builder header = ImSignalProtos.ClientRequestHeader.newBuilder()
                .setVersion(1)
                .setUserId(fromUserId)
                .setClientType(ImSignalProtos.ClientType.ANDROID_VALUE)
                .setCmd(ImSignalProtos.Cmd.CALL_SIGNAL_VALUE);
        String token = MMKVBroadUtils.Companion.getMyImToken();
        if (!TextUtils.isEmpty(token)) {
            header.setToken(token);
        }

        ImSignalProtos.SignalRequest request = ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(header.build())
                .setBody(ByteString.copyFrom(body.build().toByteArray()))
                .build();

        Log.d(TAG, "send cmd=0x0502 type=" + type
                + " callId=" + callId
                + " to=" + (toUserIds == null ? "" : toUserIds)
                + " roomName=" + body.getRoomName()
                + " mediaType=" + mediaType);
        client.sendSignalRequest(request, false);
        return true;
    }

    private static boolean sendSingle(@NonNull String callId,
                                      int type,
                                      @NonNull String toUserId,
                                      @Nullable String roomName,
                                      @Nullable String mediaType,
                                      @Nullable String payload) {
        if (TextUtils.isEmpty(toUserId)) {
            Log.e(TAG, "toUserId 为空，跳过发送 type=" + type);
            return false;
        }
        return send(
                callId,
                type,
                Collections.singletonList(toUserId),
                roomName,
                mediaType,
                null,
                MODE_SFU,
                payload
        );
    }
}
