package com.freddy.im.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

import android.util.Base64;

/**
 * RSA 2048 PKCS#1 填充公钥加密 / 私钥解密（用于 CMD 0x01 协商）。
 */
public final class RsaPkcs1Util {

    /** 密钥算法名，仅用于 {@link KeyFactory}。 */
    private static final String KEY_ALGORITHM = "RSA";

    /** Cipher 变换（含填充与模式），仅用于 {@link Cipher}。 */
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private RsaPkcs1Util() {
    }

    public static PublicKey parsePublicKeyFromX509Pem(String pem) throws GeneralSecurityException {
        String stripped = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.decode(stripped, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(spec);
    }

    public static PrivateKey parsePrivateKeyPkcs8Pem(String pem) throws GeneralSecurityException {
        String stripped = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.decode(stripped, Base64.DEFAULT);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(spec);
    }

    public static byte[] encrypt(byte[] plain, PublicKey publicKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plain);
    }

    public static byte[] decrypt(byte[] cipherBytes, PrivateKey privateKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(cipherBytes);
    }
}
