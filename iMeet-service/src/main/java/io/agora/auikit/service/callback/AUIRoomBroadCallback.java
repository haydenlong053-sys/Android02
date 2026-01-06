package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import io.agora.auikit.model.AUIRoomBroadInfo;


public interface AUIRoomBroadCallback {
    void onResult(@Nullable AUIBroadException error, @Nullable AUIRoomBroadInfo roomInfo);
}
