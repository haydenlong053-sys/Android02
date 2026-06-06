package com.freddy.im.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ImUtils {
    public  static boolean  isPrintHeartLog=false;
    public  static int  seqIdInit=(int) (System.currentTimeMillis() & 0xFFFFFFFFL);
    public static String bytesToString(byte[] bytes, Charset charset) {
        return bytes == null ? null : new String(bytes, charset);
    }

    public static String bytesToString(byte[] bytes) {
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] stringToBytes(String str) {
        return str == null ? null : str.getBytes(StandardCharsets.UTF_8);
    }

    public static String generateRandomKey(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    /**
     * 分配下一个传输层请求头中的 seqId（单调递增）。
     * 由发送路径在组帧前调用；{@link com.freddy.im.netty.SeekWeb4FrameEncoder} 只写入帧中的值，不再在此处自动取号。
     */
    public static int nextSeqId() {
        return seqIdInit++;
    }

//    1791854966a9485c8483ac3cce077ff6!85a581c1b0e74647a23324326ca055da
    public static String getP2pSessionIdId(String aId,String bId){
        if (aId.isEmpty() || bId.isEmpty()){
            return "";
        }
        return  aId+"!"+bId;

    }

}
