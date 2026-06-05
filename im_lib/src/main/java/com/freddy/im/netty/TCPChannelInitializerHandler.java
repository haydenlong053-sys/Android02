package com.freddy.im.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * SeekWeb4 帧解码 → 信令处理 → 帧编码。
 */
public class TCPChannelInitializerHandler extends ChannelInitializer<Channel> {

    private final NettyTcpClient imsClient;

    public TCPChannelInitializerHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(SeekWeb4FrameDecoder.class.getSimpleName(),
                new SeekWeb4FrameDecoder(true, SeekWeb4Protocol.PKG_BEGIN_VAL));
        pipeline.addLast(SeekWeb4SignalInboundHandler.class.getSimpleName(), new SeekWeb4SignalInboundHandler(imsClient));
        pipeline.addLast(SeekWeb4FrameEncoder.class.getSimpleName(), new SeekWeb4FrameEncoder());
    }
}
