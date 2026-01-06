package com.cboda.agora.service.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AUIUserThumbnailBroadInfo implements Serializable {

    public @NonNull String userId = "";      //用户Id
    public @NonNull String userName = "";    //用户名
    public @NonNull String userAvatar = "";  //用户头像

    @Override
    public String toString() {
        return "AUIUserThumbnailBroadInfo{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                '}';
    }
}
