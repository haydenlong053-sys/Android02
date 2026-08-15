package com.freddy.im.netty;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.text.TextUtils;

import com.freddy.im.ExecutorServiceFactory;
import com.freddy.im.HeartbeatHandler;
import com.freddy.im.IMSConfig;
import com.freddy.im.MsgDispatcher;
import com.freddy.im.MsgTimeoutTimerManager;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.crypto.Aes128CbcZeroIv;
import com.freddy.im.crypto.RsaPkcs1Util;
import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.listener.IMSConnectStatusCallback;
import com.freddy.im.listener.OnEventListener;
import com.freddy.im.signal.ImSignalProtos;
import com.freddy.im.util.ImUtils;
import com.google.protobuf.ByteString;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;

/**
 * SeekWeb4 传输层 + imsignal：CMD 0x01 RSA 协商 AES，CMD 0x02 AES 承载 SignalRequest。
 */
public class NettyTcpClient implements IMSClientInterface {

    private static volatile NettyTcpClient instance;

    private Bootstrap bootstrap;
    private Channel channel;

    private boolean isClosed = false;
    private Vector<String> serverUrlList;
    private OnEventListener mOnEventListener;
    private IMSConnectStatusCallback mIMSConnectStatusCallback;
    private MsgDispatcher msgDispatcher;
    private ExecutorServiceFactory loopGroup;

    private boolean isReconnecting = false;
    private int connectStatus = IMSConfig.CONNECT_STATE_FAILURE;
    private int reconnectInterval = IMSConfig.DEFAULT_RECONNECT_BASE_DELAY_TIME;
    private int connectTimeout = IMSConfig.DEFAULT_CONNECT_TIMEOUT;
    private int heartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    private int foregroundHeartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    private int backgroundHeartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_BACKGROUND;
    private int appStatus = IMSConfig.APP_STATUS_FOREGROUND;
    private int resendCount = IMSConfig.DEFAULT_RESEND_COUNT;
    private int resendInterval = IMSConfig.DEFAULT_RESEND_INTERVAL;

    private String currentHost = null;
    private int currentPort = -1;

    private MsgTimeoutTimerManager msgTimeoutTimerManager;

    /**
     * 客户端生成的 16 字节 AES 会话密钥（0x01 用 RSA 发给服务端）
     */
    private byte[] sessionAesKey;
    private volatile boolean userChannelBound;

    private String TAG = "NettyTcpClientClass";

    private NettyTcpClient() {
    }

    public static NettyTcpClient getInstance() {
        if (null == instance) {
            synchronized (NettyTcpClient.class) {
                if (null == instance) {
                    instance = new NettyTcpClient();
                }
            }
        }
        return instance;
    }

    @Override
    public void init(Vector<String> serverUrlList, OnEventListener listener, IMSConnectStatusCallback callback) {
        close();
        isClosed = false;
        this.serverUrlList = serverUrlList;
        this.mOnEventListener = listener;
        this.mIMSConnectStatusCallback = callback;
        msgDispatcher = new MsgDispatcher();
        msgDispatcher.setOnEventListener(listener);
        loopGroup = new ExecutorServiceFactory();
        loopGroup.initBossLoopGroup();
        msgTimeoutTimerManager = new MsgTimeoutTimerManager(this);
        resetConnect(true);
    }

    @Override
    public void resetConnect() {
        this.resetConnect(false);
    }

    @Override
    public void resetConnect(boolean isFirst) {
        if (!isFirst) {
            eLogD(TAG, "resetConnect 开始重连, isReconnecting=" + isReconnecting + ", isClosed=" + isClosed);
            try {
                Thread.sleep(IMSConfig.DEFAULT_RECONNECT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isClosed && !isReconnecting) {
            synchronized (this) {
                if (!isClosed && !isReconnecting) {
                    isReconnecting = true;
                    eLogD(TAG, "resetConnect 进入重连任务, isFirst=" + isFirst);
                    onConnectStatusCallback(IMSConfig.CONNECT_STATE_CONNECTING);
                    closeChannel();
                    loopGroup.execBossTask(new ResetConnectRunnable(isFirst));
                }
            }
        } else if (!isFirst) {
            eLogD(TAG, "resetConnect 跳过：isClosed=" + isClosed + ", isReconnecting=" + isReconnecting);
        }
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }

        isClosed = true;

        try {
            closeChannel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (bootstrap != null) {
                bootstrap.group().shutdownGracefully();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (loopGroup != null) {
                loopGroup.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (serverUrlList != null) {
                    serverUrlList.clear();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            isReconnecting = false;
            channel = null;
            bootstrap = null;
            sessionAesKey = null;
            eLogD(TAG, "close");
            userChannelBound = false;
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void sendSignalRequest(ImSignalProtos.SignalRequest request, boolean isJoinTimeoutManager, int transportSeqId) {
        if (request == null || !request.hasHeader()) {
            return;
        }
        if (request.getHeader().getCmd() == 0x01) {
            eLogD(TAG, "2->发送 BIND_USER_VALUE 信令去绑定用户，发送 sendSignalRequest--cmd=" + request.getHeader().getCmd());
        } else if (request.getHeader().getCmd() == 0x03) {
            eLogD(TAG, "准备上报前后台状态sendSignalRequest--cmd=" + request.getHeader().getCmd());
        } else {
            eLogD(TAG, "普通发送 sendSignalRequest--cmd=" + request.getHeader().getCmd() + ", transportSeqId=" + transportSeqId);
        }

        if (sessionAesKey == null) {
            eLogD(TAG, "尚未协商 AES，无法发送 SignalRequest");
            return;
        }
        try {
            byte[] plain = request.toByteArray();
            byte[] enc = Aes128CbcZeroIv.encrypt(plain, sessionAesKey);
            if (isJoinTimeoutManager) {
                msgTimeoutTimerManager.add(transportSeqId, request);
            }
            writeTransport((byte) 0x02, (byte) 0, enc, transportSeqId);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public void writeTransport(byte cmd, byte flags, byte[] body, int seqId) {
        if (channel == null || !channel.isActive()) {
            eLogD(TAG, "channel 不可用，发送失败");
            return;
        }
        channel.writeAndFlush(new SeekWeb4OutboundFrame(cmd, flags, body, seqId));
    }

    public synchronized void startKeyNegotiation() {
        try {
//            if (sessionAesKey != null) {
//                return;
//            }
            sessionAesKey = Aes128CbcZeroIv.randomKey16();
            eLogD(TAG, "客户端生成的AES密钥长度=" + sessionAesKey.length);
            String pem = "";
            if (mOnEventListener != null) {
                String p = mOnEventListener.getRsaPublicKeyPem();
                if (p != null) {
                    pem = p.trim();
                }
            }
//            if (pem.isEmpty()) {
//                pem = ImCryptoKeys.demoPublicKeyPem();
//            }
            PublicKey pub = RsaPkcs1Util.parsePublicKeyFromX509Pem(pem);
            byte[] enc = RsaPkcs1Util.encrypt(sessionAesKey, pub);
            writeTransport((byte) 0x01, (byte) 0, enc, ImUtils.nextSeqId());
            eLogD(TAG, "1->客户端主动发送 CMD=0x01 RSA 协商，密文长度=" + enc.length);
        } catch (Exception e) {
            sessionAesKey = null;
            e.printStackTrace();
        }
    }

    public synchronized void onNegotiateResponse(short code) {
        //todo 第二步，收到服务端返回的协商响应0x81，准备去绑定用户
        if (mOnEventListener != null) {
            mOnEventListener.onKeyNegotiateResponse(code);
        }
        if (code != 0) {
            sessionAesKey = null;
            resetConnect(false);
            return;
        }
        sendBindUserInternal();
    }

    private void sendBindUserInternal() {
        //todo 第三步，发送 BIND_USER_VALUE 信令去绑定用户
        if (mOnEventListener == null || sessionAesKey == null) {
            return;
        }
        String uid = mOnEventListener.getUserId();
        if (uid == null || uid.isEmpty()) {
            eLogD(TAG, "userId 为空，跳过 BIND_USER");
            return;
        }
        ImSignalProtos.BindUserSig bind = ImSignalProtos.BindUserSig.newBuilder().setOperation(1).build();
        ImSignalProtos.ClientRequestHeader.Builder hb = ImSignalProtos.ClientRequestHeader.newBuilder()
                .setVersion(1)
                .setUserId(uid)
                .setClientType(ImSignalProtos.ClientType.ANDROID_VALUE)
                .setCmd(ImSignalProtos.Cmd.BIND_USER_VALUE);
        String token = mOnEventListener.getBindToken();
        if (token != null && !token.isEmpty()) {
            hb.setToken(token);
        }
        ImSignalProtos.SignalRequest req = ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(hb.build())
                .setBody(ByteString.copyFrom(bind.toByteArray()))
                .build();
        sendSignalRequest(req, false, ImUtils.nextSeqId());
    }

    /**
     * 上报用户前后台（{@link ImSignalProtos.UserProcessSig}，cmd {@link ImSignalProtos.Cmd#REPORT_USER_PROCESS}）。
     * 仅在 AES 协商完成且 BIND_USER 成功（IM 通道已打通）后发送。
     *
     * @param appStatus {@link IMSConfig#APP_STATUS_BACKGROUND} / {@link IMSConfig#APP_STATUS_FOREGROUND}，与 {@link ImSignalProtos.UserProcessSig} {@code processState} 一致
     */
    public void reportUserProcessSig(int appStatus) {
        if (!isImChannelReady()) {
            return;
        }
        sendUserProcessInternal(appStatus);
    }

    /**
     * IM 长连接已就绪：通道活跃、已协商 AES、用户已绑定。
     */
    private boolean isImChannelReady() {
        return !isClosed
                && userChannelBound
                && sessionAesKey != null
                && channel != null
                && channel.isActive();
    }

    private void sendUserProcessInternal(int appStatus) {
        if (mOnEventListener == null || sessionAesKey == null) {
            return;
        }
        if (appStatus != IMSConfig.APP_STATUS_BACKGROUND && appStatus != IMSConfig.APP_STATUS_FOREGROUND) {
            eLogD(TAG, "appStatus 非法，跳过 REPORT_USER_PROCESS: " + appStatus);
            return;
        }
        String uid = mOnEventListener.getUserId();
        if (uid == null || uid.isEmpty()) {
            eLogD(TAG, "userId 为空，跳过 REPORT_USER_PROCESS");
            return;
        }
        ImSignalProtos.UserProcessSig processSig = ImSignalProtos.UserProcessSig.newBuilder()
                .setProcessState(appStatus)
                .build();
        ImSignalProtos.ClientRequestHeader.Builder hb = ImSignalProtos.ClientRequestHeader.newBuilder()
                .setVersion(1)
                .setUserId(uid)
                .setClientType(ImSignalProtos.ClientType.ANDROID_VALUE)
                .setCmd(ImSignalProtos.Cmd.REPORT_USER_PROCESS_VALUE);
        String token = mOnEventListener.getBindToken();
        if (token != null && !token.isEmpty()) {
            hb.setToken(token);
        }
        ImSignalProtos.SignalRequest req = ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(hb.build())
                .setBody(ByteString.copyFrom(processSig.toByteArray()))
                .build();
        sendSignalRequest(req, false, ImUtils.nextSeqId());
        eLogD(TAG, "前后台状态appStatus=" + appStatus);
    }

    /**
     * 上报消息已读（{@link ImSignalProtos.MessageReadSig}，cmd {@link ImSignalProtos.Cmd#MSG_READ}）。
     * 仅在 AES 协商完成且 BIND_USER 成功（IM 通道已打通）后发送。
     *
     * @param messageId 已读消息的 messageId
     */
    @Override
    public void reportMessageReadSig(@NonNull String messageId) {
        if (!isImChannelReady()) {
            return;
        }
        sendMessageReadInternal(messageId);
    }

    private void sendMessageReadInternal(@NonNull String messageId) {
        if (mOnEventListener == null || sessionAesKey == null) {
            return;
        }
        if (messageId.isEmpty()) {
            eLogD(TAG, "messageId 为空，跳过 MSG_READ");
            return;
        }
        String uid = mOnEventListener.getUserId();
        if (uid == null || uid.isEmpty()) {
            eLogD(TAG, "userId 为空，跳过 MSG_READ");
            return;
        }
        ImSignalProtos.MessageReadSig readSig = ImSignalProtos.MessageReadSig.newBuilder()
                .setMessageId(messageId)
                .build();
        ImSignalProtos.ClientRequestHeader.Builder hb = ImSignalProtos.ClientRequestHeader.newBuilder()
                .setVersion(1)
                .setUserId(uid)
                .setClientType(ImSignalProtos.ClientType.ANDROID_VALUE)
                .setCmd(ImSignalProtos.Cmd.MSG_READ_VALUE);
        String token = mOnEventListener.getBindToken();
        if (token != null && !token.isEmpty()) {
            hb.setToken(token);
        }
        ImSignalProtos.SignalRequest req = ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(hb.build())
                .setBody(ByteString.copyFrom(readSig.toByteArray()))
                .build();
        sendSignalRequest(req, false, ImUtils.nextSeqId());
    }

    public void sendHeartbeatSignal() {
        if (!userChannelBound || sessionAesKey == null || mOnEventListener == null) {
            eLogD(TAG, "发送心跳跳过：bound=" + userChannelBound
                    + ", hasAes=" + (sessionAesKey != null)
                    + ", hasListener=" + (mOnEventListener != null));
            return;
        }
        //todo 第五步，发送维护心跳--->0x03
        eLogD(TAG, "发送维护心跳--->0x03");
        String uid = mOnEventListener.getUserId();
        if (uid == null || uid.isEmpty()) {
            eLogD(TAG, "发送心跳跳过：userId 为空");
            return;
        }
        writeTransport((byte) 0x03, (byte) 0, SeekWeb4Packet.EMPTY_BODY, 0);
    }

    public void handleSignalResponseInternal(ImSignalProtos.SignalResponse res, int transportSeqId) {
        if (!res.hasHeader()) {
            return;
        }
        int cmd = res.getHeader().getCmd();
        if (cmd == ImSignalProtos.Cmd.BIND_USER_RES_VALUE && res.getHeader().getCode() == 0) {
            //todo 第四步，用户绑定成功，im通道打通，开始维护心跳
            eLogD(TAG, "3->协商完成，绑定用户成功，IM通道打通！！！,准备开始维护心跳");
            userChannelBound = true;
            msgTimeoutTimerManager.onResetConnected();
            addHeartbeatHandler();
            sendUserProcessInternal(appStatus);
        }
        if (cmd == ImSignalProtos.Cmd.REPORT_USER_PROCESS_RES_VALUE) {
            eLogD(TAG, "上报前后台状态成功");
        }
        if (cmd == ImSignalProtos.Cmd.SEND_MSG_RES_VALUE) {
            ImSignalProtos.SendMsgReq sendMsgReq = msgTimeoutTimerManager.getSendMsgReq(transportSeqId);
            msgTimeoutTimerManager.remove(transportSeqId);
            if (res.getHeader().getCode() == 0 && res.hasBody()) {
                try {
                    ImSignalProtos.SendMsgRes smr = ImSignalProtos.SendMsgRes.parseFrom(res.getBody().toByteArray());
                    if (sendMsgReq != null
                            && !TextUtils.isEmpty(sendMsgReq.getClientMsgId())
                            && !TextUtils.isEmpty(smr.getMessageId())) {
                        ImMessageManager.getInstance().onSendMsgSuccess(
                                sendMsgReq.getClientMsgId(), smr.getMessageId());
                    }
                    eLogD(TAG, "收到响应消息SEND_MSG_RES---getMessageId=" + smr.getMessageId());
                } catch (InvalidProtocolBufferException ignored) {
                }
            } else {
                if (sendMsgReq != null && !TextUtils.isEmpty(sendMsgReq.getClientMsgId())) {
                    int code = res.getHeader().getCode();
                    String errorMsg = res.getHeader().hasErrorMsg() ? res.getHeader().getErrorMsg() : null;
                    ImMessageManager.getInstance().onSendMsgFailed(
                            sendMsgReq.getClientMsgId(), code, errorMsg);
                }
                eLogD(TAG, "收到响应消息SEND_MSG_RES的服务器报错回执---code=" + res.getHeader().getCode() + " " + res.getHeader().getErrorMsg());
            }
        }
    }

    public byte[] getSessionAesKey() {
        return sessionAesKey;
    }

    @Override
    public int getReconnectInterval() {
        if (mOnEventListener != null && mOnEventListener.getReconnectInterval() > 0) {
            return reconnectInterval = mOnEventListener.getReconnectInterval();
        }
        return reconnectInterval;
    }

    @Override
    public int getConnectTimeout() {
        if (mOnEventListener != null && mOnEventListener.getConnectTimeout() > 0) {
            return connectTimeout = mOnEventListener.getConnectTimeout();
        }
        return connectTimeout;
    }

    @Override
    public int getForegroundHeartbeatInterval() {
        if (mOnEventListener != null && mOnEventListener.getForegroundHeartbeatInterval() > 0) {
            return foregroundHeartbeatInterval = mOnEventListener.getForegroundHeartbeatInterval();
        }
        return foregroundHeartbeatInterval;
    }

    @Override
    public int getBackgroundHeartbeatInterval() {
        if (mOnEventListener != null && mOnEventListener.getBackgroundHeartbeatInterval() > 0) {
            return backgroundHeartbeatInterval = mOnEventListener.getBackgroundHeartbeatInterval();
        }
        return backgroundHeartbeatInterval;
    }

    @Override
    public void setAppStatus(int appStatus) {
        this.appStatus = appStatus;
        if (this.appStatus == IMSConfig.APP_STATUS_FOREGROUND) {
            heartbeatInterval = foregroundHeartbeatInterval;
        } else if (this.appStatus == IMSConfig.APP_STATUS_BACKGROUND) {
            heartbeatInterval = backgroundHeartbeatInterval;
        }
        addHeartbeatHandler();
        reportUserProcessSig(this.appStatus);
    }

    public int getHeartbeatInterval() {
        return this.heartbeatInterval;
    }

    @Override
    public int getResendCount() {
        if (mOnEventListener != null && mOnEventListener.getResendCount() != 0) {
            return resendCount = mOnEventListener.getResendCount();
        }
        return resendCount;
    }

    @Override
    public int getResendInterval() {
        if (mOnEventListener != null && mOnEventListener.getResendInterval() != 0) {
            return resendInterval = mOnEventListener.getResendInterval();
        }
        return resendInterval;
    }

    public ExecutorServiceFactory getLoopGroup() {
        return loopGroup;
    }

    @Override
    public MsgDispatcher getMsgDispatcher() {
        return msgDispatcher;
    }

    @Override
    public MsgTimeoutTimerManager getMsgTimeoutTimerManager() {
        return msgTimeoutTimerManager;
    }

    private void initBootstrap() {
        EventLoopGroup elg = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(elg).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout());
        bootstrap.handler(new TCPChannelInitializerHandler(this));
    }

    private void onConnectStatusCallback(int connectStatus) {
        this.connectStatus = connectStatus;
        switch (connectStatus) {
            case IMSConfig.CONNECT_STATE_CONNECTING: {
                eLogD(TAG, "ims连接中...");
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnecting();
                }
                break;
            }

            case IMSConfig.CONNECT_STATE_SUCCESSFUL: {
                eLogD(TAG, String.format("ims连接成功，host『%s』, port『%s』", currentHost, currentPort));
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnected();
                }
                //todo tcp连接成功后，第一步开始密钥协商cmd=0x01
                startKeyNegotiation();
                break;
            }

            case IMSConfig.CONNECT_STATE_FAILURE:
            default: {
                eLogD(TAG, "ims连接失败");
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnectFailed();
                }
                break;
            }
        }
    }

    public void addHeartbeatHandler() {
        if (channel == null || !channel.isActive() || channel.pipeline() == null) {
            return;
        }

        try {
            if (channel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(IdleStateHandler.class.getSimpleName());
            }
            channel.pipeline().addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    heartbeatInterval * 3, heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            if (channel.pipeline().get(HeartbeatHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(HeartbeatHandler.class.getSimpleName());
            }
            String anchor = SeekWeb4SignalInboundHandler.class.getSimpleName();
            if (channel.pipeline().get(anchor) != null) {
                channel.pipeline().addBefore(anchor, HeartbeatHandler.class.getSimpleName(),
                        new HeartbeatHandler(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
            eLogD(TAG, "添加心跳 handler 失败：" + e.getMessage());
        }
    }

    private void removeHandler(String handlerName) {
        try {
            if (channel.pipeline().get(handlerName) != null) {
                channel.pipeline().remove(handlerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeChannel() {
        try {
            if (channel != null) {
                try {
                    removeHandler(HeartbeatHandler.class.getSimpleName());
                    removeHandler(SeekWeb4SignalInboundHandler.class.getSimpleName());
                    removeHandler(SeekWeb4FrameDecoder.class.getSimpleName());
                    removeHandler(SeekWeb4FrameEncoder.class.getSimpleName());
                    removeHandler(IdleStateHandler.class.getSimpleName());
                } finally {
                    try {
                        channel.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        channel.eventLoop().shutdownGracefully();
                    } catch (Exception ignored) {
                    }
                    channel = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        sessionAesKey = null;
//        eLogD(TAG, "closeChannel");
        userChannelBound = false;
    }

    private boolean isNetworkAvailable() {
        if (mOnEventListener != null) {
            return mOnEventListener.isNetworkAvailable();
        }
        return false;
    }

    private void toServer() {
//        ChannelFuture future = null;
        try {
//            if (bootstrap == null || TextUtils.isEmpty(currentHost) || currentPort <= 0) {
//                channel = null;
//                return;
//            }
//            // 使用 await + 超时，避免断网/DNS 卡住时 sync() 长时间无失败回调
//            future = bootstrap.connect(currentHost, currentPort);
//            int timeoutMs = Math.max(getConnectTimeout(), 1000);
//            if (!future.await(timeoutMs, TimeUnit.MILLISECONDS)) {
//                future.cancel(true);
//                eLogD(TAG, String.format("连接Server超时(ip[%s], port[%s], timeout=%dms)",
//                        currentHost, currentPort, timeoutMs));
//                channel = null;
//                return;
//            }
//            if (!future.isSuccess()) {
//                Throwable cause = future.cause();
//                eLogD(TAG, String.format("连接Server失败(ip[%s], port[%s]): %s",
//                        currentHost, currentPort,
//                        cause != null ? cause.getMessage() : "unknown"));
//                channel = null;
//                return;
//            }
//            channel = future.channel();
            channel = bootstrap.connect(currentHost, currentPort).sync().channel();
        } catch (Exception e) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            eLogD(TAG, String.format("连接Server(ip[%s], port[%s])失败", currentHost, currentPort));

            channel = null;
//            if (future != null && !future.isDone()) {
//                future.cancel(true);
//            }
        }
    }

    private class ResetConnectRunnable implements Runnable {

        private final boolean isFirst;

        ResetConnectRunnable(boolean isFirst) {
            this.isFirst = isFirst;
        }

        @Override
        public void run() {
            // 首次连接也会在无网/失败时回调；此前 isFirst 跳过失败导致一直停在「连接中」
            if (!isFirst) {
                onConnectStatusCallback(IMSConfig.CONNECT_STATE_FAILURE);
            }

            try {
                loopGroup.destroyWorkLoopGroup();

                while (!isClosed) {
                    if (!isNetworkAvailable()) {
                        // 断网时必须从 CONNECTING 落到 FAILURE，否则只会打印「连接中」
                        if (connectStatus != IMSConfig.CONNECT_STATE_FAILURE) {
                            onConnectStatusCallback(IMSConfig.CONNECT_STATE_FAILURE);
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    int status;
                    if ((status = reConnect()) == IMSConfig.CONNECT_STATE_SUCCESSFUL) {
                        onConnectStatusCallback(status);
                        break;
                    }

                    if (status == IMSConfig.CONNECT_STATE_FAILURE) {
                        onConnectStatusCallback(status);
                        try {
                            Thread.sleep(IMSConfig.DEFAULT_RECONNECT_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                isReconnecting = false;
            }
        }

        private int reConnect() {
            if (!isClosed) {
                try {
                    if (bootstrap != null) {
                        bootstrap.group().shutdownGracefully();
                    }
                } finally {
                    bootstrap = null;
                }

                initBootstrap();
                return connectServer();
            }
            return IMSConfig.CONNECT_STATE_FAILURE;
        }

        private int connectServer() {
            if (serverUrlList == null || serverUrlList.size() == 0) {
                return IMSConfig.CONNECT_STATE_FAILURE;
            }

            for (int i = 0; (!isClosed && i < serverUrlList.size()); i++) {
                String serverUrl = serverUrlList.get(i);
                if (StringUtil.isNullOrEmpty(serverUrl)) {
                    return IMSConfig.CONNECT_STATE_FAILURE;
                }

                String[] address = serverUrl.split(" ");
                for (int j = 1; j <= IMSConfig.DEFAULT_RECONNECT_COUNT; j++) {
                    if (isClosed || !isNetworkAvailable()) {
                        return IMSConfig.CONNECT_STATE_FAILURE;
                    }

                    if (connectStatus != IMSConfig.CONNECT_STATE_CONNECTING) {
                        onConnectStatusCallback(IMSConfig.CONNECT_STATE_CONNECTING);
                    }
                    eLogD(TAG, String.format("正在进行『%s』的第『%d』次连接，当前重连延时时长为『%dms』", serverUrl, j, j * getReconnectInterval()));

                    try {
                        currentHost = address[0];
                        currentPort = Integer.parseInt(address[1]);
                        toServer();

                        if (channel != null) {
                            return IMSConfig.CONNECT_STATE_SUCCESSFUL;
                        } else {
                            eLogD(TAG, String.format("第『%d』次连接未成功，准备重试", j));
                            Thread.sleep(j * getReconnectInterval());
                        }
                    } catch (InterruptedException e) {
                        close();
                        break;
                    }
                }
            }

            return IMSConfig.CONNECT_STATE_FAILURE;
        }
    }
}
