package com.cboda.agora.service.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import com.cboda.agora.service.model.AUIUserBroadInfo;
import com.cboda.agora.service.service.callback.AUIBroadCallback;
import com.cboda.agora.service.service.callback.AUIUserListBroadCallback;


/**
 * 房间Service抽象协议
 */
public interface IAUIUserBroadService extends IAUICommonBroadService<IAUIUserBroadService.AUIUserRespObserver> {

    /**
     * 获取指定 userId 的用户信息，如果为 null，则获取房间内所有人的信息
     *
     * @param roomId     房间唯一id
     * @param userIdList 用户id列表
     * @param callback   成功/失败回调
     */
    void getUserInfoList(@NonNull String roomId, @Nullable List<String> userIdList, @Nullable AUIUserListBroadCallback callback);

    /**
     * 获取指定 userId 的用户信息
     *
     * @param userId     要获取的用户id
     */
    @Nullable
    AUIUserBroadInfo getUserInfo(@NonNull String userId);

    /**
     * 对自己静音/解除静音
     *
     * @param isMute   开关
     */
    void muteUserAudio(boolean isMute, @Nullable AUIBroadCallback callback);

    /**
     * 对自己禁摄像头/解禁摄像头
     *
     * @param isMute     开关
     */
    void muteUserVideo(boolean isMute, @Nullable AUIBroadCallback callback);

    /**
     * 踢人
     * @param userId
     * @param callback
     */
    void kickUser(@NonNull String userId, @Nullable AUIBroadCallback callback);

    interface AUIUserRespObserver {
        /**
         * 用户进入房间后获取到的所有用户信息
         *
         * @param roomId   房间唯一id
         * @param userList 所有用户信息
         */
        default void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUIUserBroadInfo> userList) {}

        /**
         * 用户进入房间回调
         *
         * @param roomId   房间唯一id
         * @param userInfo 用户信息
         */
        default void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserBroadInfo userInfo) {}

        /**
         * 用户离开房间回调
         *
         * @param roomId   房间唯一id
         * @param userInfo 用户信息
         */
        default void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserBroadInfo userInfo) {}

        /**
         * 自己被踢出房间回调
         * @param roomId 房间唯一id
         */
        default void onLocalUserKickedOut(@NonNull String roomId){}

        /**
         * 用户信息修改
         *
         * @param roomId   房间唯一id
         * @param userInfo 用户信息
         */
        default void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserBroadInfo userInfo) {}

        /**
         * 用户是否静音
         *
         * @param userId   用户唯一id
         * @param mute  是否禁用
         */
        default void onUserAudioMute(@NonNull String userId, boolean mute) {}

        /**
         * 用户是否禁用摄像头
         *
         * @param userId   用户唯一id
         * @param mute  是否禁用
         */
        default void onUserVideoMute(@NonNull String userId, boolean mute) {}

        default  void onNetworkQuality(String userId, int txQuality,int rxQuality) {}
    }
}
