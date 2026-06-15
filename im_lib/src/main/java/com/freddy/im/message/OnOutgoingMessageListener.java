package com.freddy.im.message;

import androidx.annotation.NonNull;

import com.freddy.im.bean.AppMessage;

/**
 * 主动发送消息落库状态变更（发送成功 / 发送失败）。
 */
public interface OnOutgoingMessageListener {

    void onOutgoingMessageUpdated(@NonNull AppMessage message);
}
