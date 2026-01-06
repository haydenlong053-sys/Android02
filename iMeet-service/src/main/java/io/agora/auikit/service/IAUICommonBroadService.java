package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.auikit.model.AUIRoomBroadContext;
import io.agora.auikit.service.arbiter.AUIBroadArbiter;
import io.agora.auikit.service.callback.AUIBroadCallback;

public interface IAUICommonBroadService<Observer> {

    /**
     * 绑定响应事件回调，可绑定多个
     *
     * @param observer 响应事件回调
     */
    void registerRespObserver(@Nullable Observer observer);

    /**
     * 解绑响应事件回调
     *
     * @param observer 响应事件回调
     */
    void unRegisterRespObserver(@Nullable Observer observer);

    default void deInitService(@Nullable AUIBroadCallback completion) {}

    default void initService(@Nullable AUIBroadCallback completion) {}

    default void cleanUserInfo(@NonNull String userId, @Nullable AUIBroadCallback completion) {}

    /**
     * room setup success
     */
    default void serviceDidLoad(){}

    /** 获取当前房间上下文
     *
     * @return
     */
    default @NonNull AUIRoomBroadContext getRoomContext() { return AUIRoomBroadContext.shared(); }

    @NonNull String getChannelName();

    default @NonNull String getLockOwnerId() {
        AUIBroadArbiter arbiter = AUIRoomBroadContext.shared().getArbiter(getChannelName());
        if(arbiter == null){
            return "";
        }
        return arbiter.lockOwnerId();
    }
}
