package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.ArrayList;
import java.util.List;

/**
 * POST {@code call/start}：发起通话。
 */
public final class CallStartApi implements IRequestApi {

    private List<String> toUserIds;    // 必填:被邀请者
    private String mediaType;          // audio / video,默认 audio
    private String fromUsername;       // 发起人展示名(选填)
    private String fromUserIcon;       // 发起人头像(选填)

    private String groupId;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/call/start";
    }

    public CallStartApi setToUserIds(List<String> toUserIds) {
        this.toUserIds = toUserIds;
        return this;
    }

    public CallStartApi setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public CallStartApi setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    public CallStartApi setFromUserIcon(String fromUserIcon) {
        this.fromUserIcon = fromUserIcon;
        return this;
    }

    public CallStartApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }
}
