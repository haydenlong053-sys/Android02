package com.freddy.im.netty;

/**
 * 客户端上行帧（21 字节头 + body）。
 */
public final class SeekWeb4OutboundFrame {

    private final byte cmd;
    private final byte flags;
    private final int seqId;
    private final byte[] body;

    public SeekWeb4OutboundFrame(byte cmd, byte flags, byte[] body, int seqId) {
        this.cmd = cmd;
        this.flags = flags;
        this.seqId = seqId;
        this.body = body != null ? body : SeekWeb4Packet.EMPTY_BODY;
    }

    public byte getCmd() {
        return cmd;
    }

    public byte getFlags() {
        return flags;
    }

    public int getSeqId() {
        return seqId;
    }

    public byte[] getBody() {
        return body;
    }
}
