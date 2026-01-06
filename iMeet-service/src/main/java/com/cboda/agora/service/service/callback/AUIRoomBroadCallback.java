package com.cboda.agora.service.service.callback;

import androidx.annotation.Nullable;

import com.cboda.agora.service.model.AUIRoomBroadInfo;


public interface AUIRoomBroadCallback {
    void onResult(@Nullable AUIBroadException error, @Nullable AUIRoomBroadInfo roomInfo);
}
