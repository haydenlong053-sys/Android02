package com.freddy.im;

/**
 * 外发消息发送超时：以落库 {@code timestamp} 起算，进程被杀后仍可据此判定失败。
 */
public final class OutgoingSendTimeout {

    private OutgoingSendTimeout() {
    }

    /** 距超时还剩多少毫秒；已超时返回 {@code <= 0}。 */
    public static long remainingMs(long timestamp) {
        long tsMs = timestamp > 0L && timestamp < 1_000_000_000_000L
                ? timestamp * 1000L
                : timestamp;
        if (tsMs <= 0L) {
            return 0L;
        }
        return IMSConfig.DEFAULT_SEND_TIMEOUT_MS - (System.currentTimeMillis() - tsMs);
    }

    public static boolean isTimedOut(long timestamp) {
        return remainingMs(timestamp) <= 0L;
    }
}
