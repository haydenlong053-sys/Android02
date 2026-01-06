package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIUserBroadInfo;


public interface AUIUserListBroadCallback {

    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIUserBroadInfo> userList);
}
