package com.cboda.agora.service.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import com.cboda.agora.service.model.AUIUserBroadInfo;


public interface AUIUserListBroadCallback {

    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIUserBroadInfo> userList);
}
