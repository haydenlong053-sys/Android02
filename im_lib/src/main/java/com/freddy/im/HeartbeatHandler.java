package com.freddy.im;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import com.freddy.im.netty.NettyTcpClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 写空闲时发送加密 PING 信令；读空闲触发重连。
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "HeartbeatHandler";

    private final NettyTcpClient imsClient;

    public HeartbeatHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                eLogD(TAG, "写空闲，发送心跳 PING(0x03)");
                imsClient.getLoopGroup().execWorkTask(() -> imsClient.sendHeartbeatSignal());
            } else if (state == IdleState.READER_IDLE) {
                eLogD(TAG, "读空闲超时，触发心跳重连");
                imsClient.resetConnect(false);
            }
        }
    }
}
