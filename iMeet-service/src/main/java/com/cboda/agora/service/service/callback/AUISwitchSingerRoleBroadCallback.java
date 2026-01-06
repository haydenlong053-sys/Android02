package com.cboda.agora.service.service.callback;

public interface AUISwitchSingerRoleBroadCallback {
    /**
     * 切换演唱角色成功
     */
    void onSwitchRoleSuccess();

    /**
     * 切换演唱角色失败
     * @param reason 切换演唱角色失败的原因
     */
    void onSwitchRoleFail(int reason);
}
