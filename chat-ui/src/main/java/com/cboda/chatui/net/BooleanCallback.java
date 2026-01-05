package com.cboda.chatui.net;

public interface BooleanCallback {
    void onSuccess(boolean result);

    void onFail(int code, String msg);
}
