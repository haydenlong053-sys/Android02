package com.freddy.im;

import androidx.annotation.Nullable;

import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.signal.ImSignalProtos;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 以传输层 SeekWeb4 请求头中的 seqId 为唯一键，管理发送超时重发。
 */
public class MsgTimeoutTimerManager {

    private final Map<Integer, MsgTimeoutTimer> mMsgTimeoutMap = new ConcurrentHashMap<>();
    private final IMSClientInterface imsClient;

    public MsgTimeoutTimerManager(IMSClientInterface imsClient) {
        this.imsClient = imsClient;
    }

    public void add(int transportSeqId, ImSignalProtos.SignalRequest request) {
        if (request == null || !request.hasHeader()) {
            return;
        }
        int cmd = request.getHeader().getCmd();
        if (cmd != ImSignalProtos.Cmd.SEND_MSG_VALUE) {
            return;
        }
        if (!request.hasBody()) {
            return;
        }
        System.out.println("将普通消息放入超时队列，transportSeqId：" + transportSeqId);
        mMsgTimeoutMap.putIfAbsent(transportSeqId, new MsgTimeoutTimer(imsClient, request, transportSeqId));
    }

    public void remove(int transportSeqId) {
        MsgTimeoutTimer timer = mMsgTimeoutMap.remove(transportSeqId);
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 按传输层 {@code transportSeqId} 取出对应的发送请求体；不存在或解析失败时返回 null。
     */
    @Nullable
    public ImSignalProtos.SendMsgReq getSendMsgReq(int transportSeqId) {
        MsgTimeoutTimer timer = mMsgTimeoutMap.get(transportSeqId);
        if (timer == null) {
            return null;
        }
        ImSignalProtos.SignalRequest request = timer.getRequest();
        if (request == null || !request.hasBody()) {
            return null;
        }
        try {
            return ImSignalProtos.SendMsgReq.parseFrom(request.getBody().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public synchronized void onResetConnected() {
        System.out.println("sendNow--->超时重发---mMsgTimeoutMap.size="+mMsgTimeoutMap.size());
        for (Iterator<Map.Entry<Integer, MsgTimeoutTimer>> it = mMsgTimeoutMap.entrySet().iterator(); it.hasNext(); ) {
            it.next().getValue().sendNow();
        }
    }
}
