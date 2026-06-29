package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/** POST {@code api/im/group/setMembersMute} */
public final class SetMembersMuteApi implements IRequestApi {

    private String groupId;
    private List<String> userIds;
    private Boolean mute;
    private Long duration;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/setMembersMute";
    }

    public SetMembersMuteApi setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
        return this;
    }

    public SetMembersMuteApi setUserIds(@NonNull List<String> userIds) {
        this.userIds = userIds;
        return this;
    }

    public SetMembersMuteApi setMute(boolean mute) {
        this.mute = mute;
        return this;
    }

    /** 仅 {@code mute=true} 时有效；秒，{@code <=0} 或不传表示永久。 */
    public SetMembersMuteApi setDuration(@Nullable Long duration) {
        this.duration = duration;
        return this;
    }
}
