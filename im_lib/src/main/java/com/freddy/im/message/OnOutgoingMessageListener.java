package com.freddy.im.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.ImGroupBizErrorCodes;

/**
 * 主动发送消息落库状态变更（发送成功 / 发送失败）。
 */
public interface OnOutgoingMessageListener {

    void onOutgoingMessageUpdated(@NonNull AppMessage message);

    /** 发消息信令业务失败（如禁言 2017/2018）；宿主层负责 Toast 等提示。 */
    default void onSendBizError(int errorCode, @Nullable String errorMsg) {
    }
}
