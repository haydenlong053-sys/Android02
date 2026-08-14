package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/setAllMute} */
public final class SetAllMuteApi implements IRequestApi {

    private String groupId;
    /** 0 未开启，1 开启 */
    private Integer allMute;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/setAllMute";
    }

    public SetAllMuteApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public SetAllMuteApi setAllMute(boolean mute) {
        this.allMute = mute ? 1 : 0;
        return this;
    }
}
