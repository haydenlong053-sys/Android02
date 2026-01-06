package com.cboda.agora.service.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import com.cboda.agora.service.model.AUIChoristerBroadModel;


public interface AUIChoristerListBroadCallback {
    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIChoristerBroadModel> songList);
}
