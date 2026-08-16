package com.freddy.chat.im;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

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
        // 经 MessageProcessor 防抖，避免与其它入口短时间重复拉会话
        MessageProcessor.getInstance().notifyConversationList();
    }

    @Override
    public void onConnectFailed() {
        eLogD(TAG, "onConnectFailed");
        ImConnectStateStore.markConnectFailed();
    }
}
