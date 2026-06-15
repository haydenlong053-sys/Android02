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
 * 发送超时重发：按固定间隔重发同一条 {@link ImSignalProtos.SignalRequest}，
 * 重发时复用首次发送的传输层 seqId，以便与下行 0x82 回包中的 seqId 对齐。
 */
public class MsgTimeoutTimer {

    private final IMSClientInterface imsClient;
    private final ImSignalProtos.SignalRequest request;
    private final int transportSeqId;
    private final Timer timer = new Timer();
    private int resendCount;

    public MsgTimeoutTimer(IMSClientInterface imsClient, ImSignalProtos.SignalRequest request, int transportSeqId) {
        this.imsClient = imsClient;
        this.request = request;
        this.transportSeqId = transportSeqId;
        int configured = imsClient.getResendCount();
        this.resendCount = configured <= 0 ? 3 : configured;
        long interval = imsClient.getResendInterval() <= 0 ? 8000 : imsClient.getResendInterval();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (resendCount <= 0) {
                    timer.cancel();
                    onResendExhausted();
                    return;
                }
                resendCount--;
                imsClient.sendSignalRequest(MsgTimeoutTimer.this.request, false, MsgTimeoutTimer.this.transportSeqId);
            }
        }, interval, interval);
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

    /** 超时重发次数用尽：移除队列并标记本地发送失败。 */
    private void onResendExhausted() {
        imsClient.getMsgTimeoutTimerManager().remove(transportSeqId);
        String clientMsgId = parseClientMsgId(request);
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        ImMessageManager.getInstance().onSendMsgFailed(clientMsgId);
    }

    @Nullable
    private static String parseClientMsgId(@Nullable ImSignalProtos.SignalRequest request) {
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
