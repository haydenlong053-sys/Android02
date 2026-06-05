package com.freddy.im.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 内置演示用 RSA PEM（与 {@link com.freddy.im.NettyServerDemo} 私钥成对），仅用于本地联通验证。
 */
public final class ImCryptoKeys {

    private ImCryptoKeys() {
    }

    private static String readResource(String name) {
        InputStream in = ImCryptoKeys.class.getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("Missing resource: " + name);
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static String demoPublicKeyPem() {
        return readResource("demo_rsa_public.pem");
    }

    public static String demoPrivatePkcs8Pem() {
        return readResource("demo_rsa_private_pkcs8.pem");
    }
}
