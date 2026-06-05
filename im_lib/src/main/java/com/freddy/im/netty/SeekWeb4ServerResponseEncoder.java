package com.freddy.im.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class SeekWeb4ServerResponseEncoder extends MessageToByteEncoder<SeekWeb4ServerOutboundFrame> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SeekWeb4ServerOutboundFrame frame, ByteBuf out) throws Exception {
        byte[] body = frame.getBody();
        int totalLen = SeekWeb4Protocol.RES_HEADER_SIZE + body.length;
        if (totalLen > 65535) {
            throw new IllegalArgumentException("SeekWeb4: 包过大");
        }
        out.writeInt(SeekWeb4Protocol.PKG_BEGIN_VAL);
        out.writeShort(totalLen);
        out.writeByte(SeekWeb4Protocol.VERSION);
        out.writeByte(frame.getCmd());
        out.writeByte(frame.getFlags());
        out.writeLong(0L);
        out.writeInt((int) (System.currentTimeMillis() & 0xFFFFFFFFL));
        out.writeByte(frame.getCode());
        out.writeBytes(body);
    }
}
