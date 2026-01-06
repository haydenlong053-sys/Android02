package com.cboda.agora.service.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import com.cboda.agora.service.model.AUIMusicBroadModel;


public interface AUIMusicListBroadCallback {

    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIMusicBroadModel> songList);
}
