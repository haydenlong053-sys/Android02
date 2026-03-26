//package com.cboda.baselibrary.utils;
//
//import android.util.Base64;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//
///**
// * @author:李迪迦
// * @date: 2024/12/12
// * @Type:
// */
//public class AESBroadUtil {
//
//    // AES密钥，16字节、24字节或32字节
//    private static final String AES_KEY = HttpsBroadConfig.INSTANCE.getAppAesPwd();
//
//    // 加密
//    public static String encrypt(String input) throws Exception {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//        byte[] encrypted = cipher.doFinal(input.getBytes());
//        return Base64.encodeToString(encrypted, Base64.DEFAULT);
//    }
//
//    // 解密
//    public static String decrypt(String encrypted) throws Exception {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
//        byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
//        return new String(original);
//    }
//}
