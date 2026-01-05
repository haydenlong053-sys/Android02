/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cboda.chatui.common.OperateBroadResult;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.OnSettingUpdateListener;

public class SettingViewBroadModel extends ViewModel implements OnSettingUpdateListener {
    private MutableLiveData<Object> settingUpdatedLiveData;

    public SettingViewBroadModel() {
        ChatManager.Instance().addSettingUpdateListener(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ChatManager.Instance().removeSettingUpdateListener(this);
    }

    public MutableLiveData<Object> settingUpdatedLiveData() {
        if (settingUpdatedLiveData == null) {
            settingUpdatedLiveData = new MutableLiveData<>();
        }
        return settingUpdatedLiveData;
    }

    public String getUserSetting(int scope, String key) {
        return ChatManager.Instance().getUserSetting(scope, key);
    }

    public MutableLiveData<OperateBroadResult<Integer>> setUserSetting(int scope, String key, String value) {
        MutableLiveData<OperateBroadResult<Integer>> result = new MutableLiveData<>();
        ChatManager.Instance().setUserSetting(scope, key, value, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));

            }
        });
        return result;
    }

    @Override
    public void onSettingUpdate() {
        if (settingUpdatedLiveData != null) {
            settingUpdatedLiveData.setValue(new Object());
        }
    }
}
