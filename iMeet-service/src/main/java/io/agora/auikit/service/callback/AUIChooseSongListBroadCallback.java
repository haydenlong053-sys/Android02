package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIChooseMusicBroadModel;


public interface AUIChooseSongListBroadCallback {

    void onResult(@Nullable AUIBroadException error, @Nullable List<AUIChooseMusicBroadModel> songList);

}
