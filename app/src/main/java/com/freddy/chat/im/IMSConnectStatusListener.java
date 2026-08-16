package com.freddy.chat.im;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.im.listener.IMSConnectStatusCallback;

/**
 * 自研 IM 连接状态：更新 UI 状态，并在通道就绪后触发会话列表刷新。
 */
public class IMSConnectStatusListener implements IMSConnectStatusCallback {

    private static final String TAG = "IMSConnectStatus";

    @Override
    public void onConnecting() {
        eLogD(TAG, "onConnecting");
        ImConnectStateStore.markConnecting();
    }

    @Override
    public void onConnected() {
        // TCP 连通，尚未 BIND
        eLogD(TAG, "onConnected(TCP)");
        ImConnectStateStore.markTcpConnected();
    }

    @Override
    public void onImChannelReady() {
        eLogD(TAG, "onImChannelReady");
        ImConnectStateStore.markImChannelReady();
        // 重连/首连就绪后拉会话，补齐断线期间变更
        CEventCenter.dispatchEvent(Events.NOTIFY_CONVERSATION_LIST, 0, 0, "im_channel_ready");
    }

    @Override
    public void onConnectFailed() {
        eLogD(TAG, "onConnectFailed");
        ImConnectStateStore.markConnectFailed();
    }
}
