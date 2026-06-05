package com.freddy.im.netty;

/**
 * SeekWeb4 传输层包头约定。
 */
public final class SeekWeb4Protocol {

    private SeekWeb4Protocol() {
    }

    public static final int REQ_HEADER_SIZE = 21;
    public static final int RES_HEADER_SIZE = 22;
    public static final int VERSION = 1;
    public static final int MAX_PACKET_LENGTH = 65535;

    /** 与服务端约定的魔数 begin（大端 4 字节） */
    public static final int PKG_BEGIN_VAL = 0xFAED7439;

    public static final byte DEFAULT_TRANSPORT_CMD = 0x02;
    public static final byte DEFAULT_TRANSPORT_FLAGS = 0;

    public static final int CLIENT_TYPE_ANDROID = 0x0101;
}
