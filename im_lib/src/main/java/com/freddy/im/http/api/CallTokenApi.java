package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code call/token}：获取 LiveKit 通话 Token。 */
public final class CallTokenApi implements IRequestApi {

    private String identity;
    private String roomName;
    private Boolean video;
    private String name;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/call/token";
    }

    public CallTokenApi setIdentity(String identity) {
        this.identity = identity;
        return this;
    }

    public CallTokenApi setRoomName(String roomName) {
        this.roomName = roomName;
        return this;
    }

    public CallTokenApi setVideo(boolean video) {
        this.video = video;
        return this;
    }

    public CallTokenApi setName(String name) {
        this.name = name;
        return this;
    }
}
