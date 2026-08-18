package com.freddy.im.interf;

import androidx.annotation.NonNull;

import com.freddy.im.MsgDispatcher;
import com.freddy.im.MsgTimeoutTimerManager;
import com.freddy.im.listener.IMSConnectStatusCallback;
import com.freddy.im.listener.OnEventListener;
import com.freddy.im.signal.ImSignalProtos;
import com.freddy.im.util.ImUtils;

import java.util.Vector;

/**
 * IMS 抽象接口。
 */
public interface IMSClientInterface {

    void init(Vector<String> serverUrlList, OnEventListener listener, IMSConnectStatusCallback callback);

    void resetConnect();

    void resetConnect(boolean isFirst);

    /**
     * 系统网络恢复时调用：若未在重连且通道未就绪，则触发一次重连。
     */
    void notifyNetworkAvailable();

    void close();

    boolean isClosed();

    /** BIND_USER 是否已成功（IM 业务通道可用）。 */
    boolean isUserChannelBound();

    /** 是否正在重连循环中。 */
    boolean isReconnecting();

    /**
     * 发送已构造的 {@link ImSignalProtos.SignalRequest}（内部按 CMD 0x02 + AES 封装），
     * 传输层 seqId 由 {@link ImUtils#nextSeqId()} 自动分配。
     */
    default void sendSignalRequest(ImSignalProtos.SignalRequest request, boolean isJoinTimeoutManager) {
        sendSignalRequest(request, isJoinTimeoutManager, ImUtils.nextSeqId());
    }

    /**
     * 发送 SignalRequest，并指定传输层包头 seqId（超时队列与下行 0x82 回包按同一 seqId 对齐）。
     */
    void sendSignalRequest(ImSignalProtos.SignalRequest request, boolean isJoinTimeoutManager, int transportSeqId);

    int getReconnectInterval();

    int getConnectTimeout();

    int getForegroundHeartbeatInterval();

    int getBackgroundHeartbeatInterval();

    void setAppStatus(int appStatus);

    /**
     * 上报消息已读（{@link com.freddy.im.signal.ImSignalProtos.MessageReadSig}，cmd {@code MSG_READ}）。
     */
    void reportMessageReadSig(@NonNull String messageId);

    int getResendCount();

    int getResendInterval();

    MsgDispatcher getMsgDispatcher();

    MsgTimeoutTimerManager getMsgTimeoutTimerManager();
}
