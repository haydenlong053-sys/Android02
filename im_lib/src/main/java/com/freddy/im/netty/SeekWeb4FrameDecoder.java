package com.freddy.im.netty;

import static com.freddy.im.netty.SeekWeb4Protocol.MAX_PACKET_LENGTH;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class SeekWeb4FrameDecoder extends ByteToMessageDecoder {

    private final int headerSize;
    private final int expectedMagic;

    public SeekWeb4FrameDecoder(boolean responseFormat, int expectedMagic) {
        this.headerSize = responseFormat ? SeekWeb4Protocol.RES_HEADER_SIZE : SeekWeb4Protocol.REQ_HEADER_SIZE;
        this.expectedMagic = expectedMagic;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //找到起始位置
        while (in.readableBytes() >= headerSize) {
            int begin = in.readInt();
            if (expectedMagic == begin) {
                in.readerIndex(in.readerIndex() - 4);
                break;
            }
            in.readerIndex(in.readerIndex() - 3);
        }
        //检查长度
        if (in.readableBytes() < headerSize) {
//            log.error("readableBytes {} < {}", in.readableBytes(), SecpMessage.C2S_HEADER_LENGTH);
            return;
        }

        int idx = in.readerIndex();
        int pkgLen = in.getShort(idx + 4) & 0xffff;
        if (pkgLen > MAX_PACKET_LENGTH) {
//            log.error("discarding data. pkgLen: {}", pkgLen);
            in.skipBytes(pkgLen);
            return;
        }
        if (pkgLen > in.readableBytes()) {
            //不完整
            return;
        }
        in.readInt();
        in.readShort();
        short version = (short) (in.readByte() & 0xff);
        short cmd = (short) (in.readByte() & 0xff);
        short flags = (short) (in.readByte() & 0xff);
        long lcId = in.readLong();
        int seqId = in.readInt();
        short code = -1;
        if (headerSize == SeekWeb4Protocol.RES_HEADER_SIZE) {
            code =in.readByte();
        }
        int bodyLen = pkgLen - headerSize;
        if (in.readableBytes() < bodyLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] body = bodyLen > 0 ? new byte[bodyLen] : SeekWeb4Packet.EMPTY_BODY;
        if (bodyLen > 0) {
            in.readBytes(body);
        }

        out.add(new SeekWeb4Packet(expectedMagic, pkgLen, version, cmd, flags, lcId, seqId, code, body));
    }
}
