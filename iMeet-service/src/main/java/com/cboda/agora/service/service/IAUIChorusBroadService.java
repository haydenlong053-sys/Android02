package com.cboda.agora.service.service;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import com.cboda.agora.service.model.AUIChoristerBroadModel;
import com.cboda.agora.service.service.callback.AUIBroadCallback;
import com.cboda.agora.service.service.callback.AUIChoristerListBroadCallback;
import com.cboda.agora.service.service.callback.AUIBroadException;
import com.cboda.agora.service.service.callback.AUISwitchSingerRoleBroadCallback;

public interface IAUIChorusBroadService extends IAUICommonBroadService<IAUIChorusBroadService.AUIChorusRespObserver> {


    // 获取合唱者列表
    void getChoristersList(@Nullable AUIChoristerListBroadCallback callback);

    // 加入合唱
    void joinChorus(@Nullable String songCode, @Nullable String userId, @Nullable AUIBroadCallback callback);

    // 退出合唱
    void leaveChorus(@Nullable String songCode, @Nullable String userId, @Nullable AUIBroadCallback callback);

    // 切换角色
    void switchSingerRole(int newRole, @Nullable AUISwitchSingerRoleBroadCallback callback);

    interface AUIChorusRespObserver {
        /// 合唱者加入
        /// - Parameter chorus: <#chorus description#>
        void onChoristerDidEnter(AUIChoristerBroadModel chorister);

        /// 合唱者离开
        /// - Parameter chorister: <#chorister description#>
        void onChoristerDidLeave(AUIChoristerBroadModel chorister);

        /// 角色切换回调
        /// - Parameters:
        ///   - oldRole: <#oldRole description#>
        ///   - newRole: <#newRole description#>
        void onSingerRoleChanged(int oldRole, int newRole);

        void onChoristerDidChanged();

        default @Nullable AUIBroadException onWillJoinChorus(@NotNull String songCode, @NotNull String userId, @NotNull Map<String, String> metaData){
            return null;
        }
    }
}
