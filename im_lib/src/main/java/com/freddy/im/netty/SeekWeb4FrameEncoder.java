package com.freddy.im.netty;

import static com.freddy.im.netty.SeekWeb4Protocol.MAX_PACKET_LENGTH;

import com.freddy.im.util.ImUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class SeekWeb4FrameEncoder extends MessageToByteEncoder<SeekWeb4OutboundFrame> {
    public static long lcId =0L;

    @Override
    protected void encode(ChannelHandlerContext ctx, SeekWeb4OutboundFrame frame, ByteBuf out) throws Exception {
        if (frame.getCmd()!=0x03 || ImUtils.isPrintHeartLog){
            System.out.println("发信令cmd："+frame.getCmd()+"----lcId="+lcId+"---seqId="+frame.getSeqId()+"---body长度:"+frame.getBody().length);
        }
        byte[] body = frame.getBody();
        int totalLen = SeekWeb4Protocol.REQ_HEADER_SIZE + body.length;
        if (totalLen > MAX_PACKET_LENGTH) {
            throw new IllegalArgumentException("SeekWeb4: 包过大");
        }
        out.writeInt(SeekWeb4Protocol.PKG_BEGIN_VAL);
        out.writeShort(totalLen);
        out.writeByte(SeekWeb4Protocol.VERSION);
        out.writeByte(frame.getCmd());
        out.writeByte(frame.getFlags());
        out.writeLong(lcId);
        out.writeInt(frame.getSeqId());
        out.writeBytes(body);
    }
}
