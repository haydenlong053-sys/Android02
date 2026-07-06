package com.freddy.im.http.model.im;

/**
 * POST {@code call/token} 响应 {@code content}。
 */
@SuppressWarnings("unused")
public class CallTokenContent {

    private String token;
    private String url;
    private String roomName;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
