package com.freddy.im.message;

import androidx.annotation.NonNull;

import com.freddy.im.bean.AppMessage;

/**
 * 主动发送消息落库状态变更（如发送成功回包更新 {@code msgId}）。
 */
public interface OnOutgoingMessageListener {

    void onOutgoingMessageUpdated(@NonNull AppMessage message);
}
