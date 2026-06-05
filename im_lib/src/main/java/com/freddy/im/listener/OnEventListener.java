package com.freddy.im.listener;

import com.freddy.im.signal.ImSignalProtos;

/**
 * 与应用层交互：SeekWeb4 + imsignal 信令。
 */
public interface OnEventListener {

    void onSignalResponse(ImSignalProtos.SignalResponse response);

    void onSignalPush(ImSignalProtos.SignalPush push);

    /**
     * 传输层 CMD 0x81 协商结果，code 0 表示成功。
     */
    void onKeyNegotiateResponse(short transportCode);

    boolean isNetworkAvailable();

    int getReconnectInterval();

    int getConnectTimeout();

    int getForegroundHeartbeatInterval();

    int getBackgroundHeartbeatInterval();

    int getResendCount();

    int getResendInterval();

    /**
     * 绑定长连接使用的用户 ID。
     */
    String getUserId();

    /**
     * 绑定长连接使用的 token（写入 ClientRequestHeader.token）。
     */
    String getBindToken();

    /**
     * RSA 公钥 PEM；若返回空串则使用库内置演示公钥（仅本地 Demo）。
     */
    String getRsaPublicKeyPem();
}
