package com.freddy.chat.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * @deprecated 请改用 {@link com.freddy.im.call.CallSignalSender}（已迁至 im_lib，供通话页直接调用）。
 */
@Deprecated
public final class CallSignalSender {

    public static final String MEDIA_AUDIO = com.freddy.im.call.CallSignalSender.MEDIA_AUDIO;
    public static final String MEDIA_VIDEO = com.freddy.im.call.CallSignalSender.MEDIA_VIDEO;
    public static final String MODE_SFU = com.freddy.im.call.CallSignalSender.MODE_SFU;
    public static final String MODE_MESH = com.freddy.im.call.CallSignalSender.MODE_MESH;

    private CallSignalSender() {
    }

    public static boolean invite(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return com.freddy.im.call.CallSignalSender.invite(callId, toUserId, roomName, video);
    }

    public static boolean invite(@NonNull String callId,
                                 @NonNull List<String> toUserIds,
                                 @Nullable String roomName,
                                 boolean video,
                                 @Nullable String groupId,
                                 @Nullable String mode) {
        return com.freddy.im.call.CallSignalSender.invite(callId, toUserIds, roomName, video, groupId, mode);
    }

    public static boolean cancel(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return com.freddy.im.call.CallSignalSender.cancel(callId, toUserId, roomName, video);
    }

    public static boolean hangup(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return com.freddy.im.call.CallSignalSender.hangup(callId, toUserId, roomName, video);
    }

    public static boolean hangup(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video,
                                 @Nullable String groupId) {
        return com.freddy.im.call.CallSignalSender.hangup(callId, toUserId, roomName, video, groupId);
    }

    public static boolean hangup(@NonNull String callId,
                                 @NonNull List<String> toUserIds,
                                 @Nullable String roomName,
                                 boolean video,
                                 @Nullable String groupId) {
        return com.freddy.im.call.CallSignalSender.hangup(callId, toUserIds, roomName, video, groupId);
    }

    public static boolean reject(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return com.freddy.im.call.CallSignalSender.reject(callId, toUserId, roomName, video);
    }

    public static boolean reject(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video,
                                 @Nullable String groupId) {
        return com.freddy.im.call.CallSignalSender.reject(callId, toUserId, roomName, video, groupId);
    }

    public static boolean ringing(@NonNull String callId,
                                  @NonNull String toUserId,
                                  @Nullable String roomName,
                                  boolean video) {
        return com.freddy.im.call.CallSignalSender.ringing(callId, toUserId, roomName, video);
    }

    public static boolean accept(@NonNull String callId,
                                 @NonNull String toUserId,
                                 @Nullable String roomName,
                                 boolean video) {
        return com.freddy.im.call.CallSignalSender.accept(callId, toUserId, roomName, video);
    }

    public static boolean busy(@NonNull String callId,
                               @NonNull String toUserId,
                               @Nullable String roomName,
                               boolean video) {
        return com.freddy.im.call.CallSignalSender.busy(callId, toUserId, roomName, video);
    }

    public static boolean end(@NonNull String callId,
                              @NonNull String toUserId,
                              @Nullable String roomName,
                              boolean video) {
        return com.freddy.im.call.CallSignalSender.end(callId, toUserId, roomName, video);
    }

    public static boolean send(@NonNull String callId,
                               int type,
                               @Nullable List<String> toUserIds,
                               @Nullable String roomName,
                               @Nullable String mediaType,
                               @Nullable String groupId,
                               @Nullable String mode,
                               @Nullable String payload) {
        return com.freddy.im.call.CallSignalSender.send(
                callId, type, toUserIds, roomName, mediaType, groupId, mode, payload);
    }
}
