package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/setNickname}（文档 §3.26） */
public final class SetGroupNicknameApi implements IRequestApi {

    private String groupId;
    private String nickname;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/setNickname";
    }

    public SetGroupNicknameApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public SetGroupNicknameApi setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        return this;
    }
}
