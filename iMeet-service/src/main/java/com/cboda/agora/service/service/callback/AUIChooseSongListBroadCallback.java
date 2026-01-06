package com.cboda.agora.service.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import com.cboda.agora.service.model.AUIChooseMusicBroadModel;


public interface AUIChooseSongListBroadCallback {

    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIChooseMusicBroadModel> songList);

}
