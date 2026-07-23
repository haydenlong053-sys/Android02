package com.freddy.im.netty;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import java.security.GeneralSecurityException;

import com.freddy.im.crypto.Aes128CbcZeroIv;
import com.freddy.im.signal.ImSignalProtos;
import com.freddy.im.util.ImUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理服务端 CMD 0x81 / 0x82 / 0x00：按需 AES 解密后解析 imsignal。
 */
public class SeekWeb4SignalInboundHandler extends SimpleChannelInboundHandler<SeekWeb4Packet> {
    private String TAG = "SeekWeb4SignalInboundHandlerClass";
    private final NettyTcpClient imsClient;

    public SeekWeb4SignalInboundHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel ch = ctx.channel();
        if (ch != null) {
            ch.close();
            ctx.close();
        }
        imsClient.resetConnect(false);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        Channel ch = ctx.channel();
        if (ch != null) {
            ch.close();
            ctx.close();
        }
        imsClient.resetConnect(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SeekWeb4Packet p) throws Exception {
        short cmd = p.getCmd();
        eLogD(TAG, "cmd: " + p.getCmd(), "body长度：" + p.getBody().length, "lcId:" + p.getLcId(), "seqId:" + p.getSeqId(), "code=" + p.getCode());
        if (cmd != 0x00) {
            if (cmd != 0x03 || ImUtils.isPrintHeartLog) {
                System.out.println("收到服务端的信令响应cmd: " + p.getCmd() + "=====body长度：" + p.getBody().length + "----" + "lcId:" + p.getLcId() + "----" + "seqId:" + p.getSeqId() + "---code=" + p.getCode());
            }
        }

        if (cmd == 0x81) {
            SeekWeb4FrameEncoder.lcId = p.getLcId();
            imsClient.onNegotiateResponse(p.getCode());
            return;
        }

        byte[] body = p.getBody();
        if (imsClient.getSessionAesKey() != null && body.length > 0) {
            try {
//                System.out.println("去AES 解密信令: cmd:" + p.getCmd());
                body = Aes128CbcZeroIv.decrypt(body, imsClient.getSessionAesKey());
            } catch (GeneralSecurityException e) {
                System.err.println("AES 解密失败: " + e.getMessage());
                ctx.close();
                return;
            }
        }

        if (cmd == 0x82) {
            ImSignalProtos.SignalResponse response = ImSignalProtos.SignalResponse.parseFrom(body);
            imsClient.handleSignalResponseInternal(response, p.getSeqId());
            imsClient.getMsgDispatcher().dispatchSignalResponse(response);
            return;
        }

        if (cmd == 0x00 && p.getCode() == 0) {
            ImSignalProtos.SignalPush push = ImSignalProtos.SignalPush.parseFrom(body);
            imsClient.getMsgDispatcher().dispatchSignalPush(push);
        }
    }
}
