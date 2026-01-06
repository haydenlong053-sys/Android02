package com.cboda.agora.service.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import com.cboda.agora.service.model.AUIRoomBroadInfo;


public interface AUIRoomListBroadCallback {
    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIRoomBroadInfo> roomList);
}

