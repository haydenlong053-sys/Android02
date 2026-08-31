package com.freddy.im.crypto;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.util.ImUtils;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-128-CBC，IV 为 16 字节全 0，PKCS5/7 填充（与 SeekWeb4 文档一致）。
 */
public final class Aes128CbcZeroIv {
    public static String generateRandomKey="";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private Aes128CbcZeroIv() {
    }

    private static IvParameterSpec zeroIv() {
        return new IvParameterSpec(new byte[16]);
    }

    public static byte[] encrypt(byte[] plain, byte[] key16) throws GeneralSecurityException {
        if (key16 == null || key16.length != 16) {
            throw new IllegalArgumentException("AES key must be 16 bytes");
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16, "AES"), zeroIv());
        return cipher.doFinal(plain);
    }

    public static byte[] decrypt(byte[] cipherBytes, byte[] key16) throws GeneralSecurityException {
        if (key16 == null || key16.length != 16) {
            throw new IllegalArgumentException("AES key must be 16 bytes");
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key16, "AES"), zeroIv());
        return cipher.doFinal(cipherBytes);
    }

    public static byte[] randomKey16() {
//        byte[] k = new byte[16];
//        java.security.SecureRandom sr = new java.security.SecureRandom();
//        sr.nextBytes(k);
//        return k;
        if (generateRandomKey.isEmpty()){
            generateRandomKey = ImUtils.generateRandomKey(16);
        }
        ELogUtilKt.eLogI(ELogUtilKt.APP_IM_TAG,"AES密钥"+generateRandomKey);
        return ImUtils.stringToBytes(generateRandomKey);

    }
}
