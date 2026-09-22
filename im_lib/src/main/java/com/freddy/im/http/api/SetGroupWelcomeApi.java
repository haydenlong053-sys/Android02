package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/setGroupWelcome} */
public final class SetGroupWelcomeApi implements IRequestApi {

    private String groupId;
    private boolean enabled;
    private String content;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/setGroupWelcome";
    }

    public SetGroupWelcomeApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public SetGroupWelcomeApi setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public SetGroupWelcomeApi setContent(String content) {
        this.content = content;
        return this;
    }
}
