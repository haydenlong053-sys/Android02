package com.freddy.im;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.signal.ImSignalProtos;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 发送超时：入队后等待 {@link IMSConfig#DEFAULT_SEND_TIMEOUT_MS}，
 * 未收到成功回执则停止发送并标记失败，由用户点击重新发送。
 */
public class MsgTimeoutTimer {

    private final IMSClientInterface imsClient;
    private final ImSignalProtos.SignalRequest request;
    private final int transportSeqId;
    private final Timer timer = new Timer();

    public MsgTimeoutTimer(IMSClientInterface imsClient, ImSignalProtos.SignalRequest request, int transportSeqId) {
        this.imsClient = imsClient;
        this.request = request;
        this.transportSeqId = transportSeqId;
        long timeoutMs = IMSConfig.DEFAULT_SEND_TIMEOUT_MS;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onSendTimeout();
            }
        }, timeoutMs);
    }

    public void cancel() {
        timer.cancel();
    }

    public ImSignalProtos.SignalRequest getRequest() {
        return request;
    }

    public void sendNow() {
        imsClient.sendSignalRequest(request, false, transportSeqId);
    }

    /** 发送超时：移除队列并标记本地发送失败，不再自动重发。 */
    private void onSendTimeout() {
        imsClient.getMsgTimeoutTimerManager().remove(transportSeqId);
        String clientMsgId = parseClientMsgId(request);
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        ImMessageManager.getInstance().onSendMsgFailed(clientMsgId);
    }

    @Nullable
    public static String parseClientMsgId(@Nullable ImSignalProtos.SignalRequest request) {
        if (request == null || !request.hasBody()) {
            return null;
        }
        try {
            ImSignalProtos.SendMsgReq sendMsgReq = ImSignalProtos.SendMsgReq.parseFrom(
                    request.getBody().toByteArray());
            return sendMsgReq.getClientMsgId();
        } catch (InvalidProtocolBufferException ignored) {
            return null;
        }
    }
}
