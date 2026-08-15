package com.freddy.im.listener;

/**
 * IMS 连接状态回调。
 * <p>
 * 语义约定：
 * <ul>
 *   <li>{@link #onConnecting()}：正在建立 / 重连 TCP</li>
 *   <li>{@link #onConnected()}：TCP 已连通（尚未完成密钥协商与 BIND）</li>
 *   <li>{@link #onImChannelReady()}：BIND_USER 成功，IM 通道真正可用</li>
 *   <li>{@link #onConnectFailed()}：连接失败（仍可能在后台重试）</li>
 * </ul>
 */
public interface IMSConnectStatusCallback {

    /** ims 连接中 */
    void onConnecting();

    /** TCP 连接成功（协商/绑定尚未完成） */
    void onConnected();

    /**
     * IM 通道就绪（AES 协商 + BIND_USER 成功）。
     * 默认转调 {@link #onConnected()}，旧实现可不改。
     */
    default void onImChannelReady() {
        onConnected();
    }

    /** ims 连接失败 */
    void onConnectFailed();
}
