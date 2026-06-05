package com.freddy.im.netty;

public final class SeekWeb4Packet {

    public static final byte[] EMPTY_BODY = new byte[0];

    private final int begin;
    private final int pkgLen;
    private final short version;
    private final short cmd;
    private final short flags;
    private final long lcId;
    private final int seqId;
    private final short code;
    private final byte[] body;

    public SeekWeb4Packet(int begin, int pkgLen, short version, short cmd, short flags,
                          long lcId, int seqId, short code, byte[] body) {
        this.begin = begin;
        this.pkgLen = pkgLen;
        this.version = version;
        this.cmd = cmd;
        this.flags = flags;
        this.lcId = lcId;
        this.seqId = seqId;
        this.code = code;
        this.body = body;
    }

    public int getBegin() {
        return begin;
    }

    public int getPkgLen() {
        return pkgLen;
    }

    public short getVersion() {
        return version;
    }

    public short getCmd() {
        return cmd;
    }

    public short getFlags() {
        return flags;
    }

    public long getLcId() {
        return lcId;
    }

    public int getSeqId() {
        return seqId;
    }

    public short getCode() {
        return code;
    }

    public byte[] getBody() {
        return body;
    }
}
