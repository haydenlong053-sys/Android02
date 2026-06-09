package com.freddy.chat.viewholder;

import androidx.annotation.NonNull;

import com.freddy.im.bean.AppMessage;

public interface OnMessageRetryListener {
    void onRetryFailedMessage(@NonNull AppMessage message);
}
