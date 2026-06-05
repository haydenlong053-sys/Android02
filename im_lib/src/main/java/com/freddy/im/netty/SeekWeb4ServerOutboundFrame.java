package com.freddy.im.netty;

/**
 * 服务端下行帧（22 字节头含 code + body）。
 */
public final class SeekWeb4ServerOutboundFrame {

    private final byte cmd;
    private final byte flags;
    private final byte code;
    private final byte[] body;

    public SeekWeb4ServerOutboundFrame(byte cmd, byte flags, byte code, byte[] body) {
        this.cmd = cmd;
        this.flags = flags;
        this.code = code;
        this.body = body != null ? body : SeekWeb4Packet.EMPTY_BODY;
    }

    public byte getCmd() {
        return cmd;
    }

    public byte getFlags() {
        return flags;
    }

    public byte getCode() {
        return code;
    }

    public byte[] getBody() {
        return body;
    }
}
