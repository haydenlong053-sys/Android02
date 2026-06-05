package com.freddy.im.message;

import androidx.annotation.NonNull;

import com.freddy.im.bean.AppMessage;

import java.util.List;

/**
 * HTTP 拉取并落库后的消息列表回调。
 */
public interface OnNewMessageListListener {

    void onNewMessageList(@NonNull List<AppMessage> messages);
}
