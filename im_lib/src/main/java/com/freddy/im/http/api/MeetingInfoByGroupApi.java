package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/**
 * GET ,通过群id查会议信息。
 * query：groupId
 */
public final class MeetingInfoByGroupApi implements IRequestApi {

    private String groupId;


    @NonNull
    @Override
    public String getApi() {
//        return "api/im/meeting/liveByGroup?groupId=" + groupId;
        return "api/im/meeting/liveByGroup";
    }

    public MeetingInfoByGroupApi setGroupId(@Nullable String groupId) {
        this.groupId = groupId;
        return this;
    }


}
