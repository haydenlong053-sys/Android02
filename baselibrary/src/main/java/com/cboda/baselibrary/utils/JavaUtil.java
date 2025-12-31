package com.cboda.baselibrary.utils;

import static com.cboda.baselibrary.utils.ELogUtilKt.toLog;

import android.util.Base64;

import java.net.URLDecoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class JavaUtil {
    public static String UStr_2_Str(String Ustr0) {
        String Ustr = Ustr0;
        String[] per = new String[6];
        try {
            int S = 0, E = 0;
            String C = "", Value = "";
            while (Ustr.contains("\\u")) {
                S = Ustr.indexOf("\\u") + "\\u".length();
                E = Ustr.indexOf("\\u", S);
                if (E == -1) E = Ustr.length();

                if (E > S) {
                    C = Ustr.substring(S, E);
                    if (C.length() > 4) C = C.substring(0, 4);
                    Value = (char) Integer.parseInt(C, 16) + "";

                    Ustr = Ustr.replace("\\u" + C, Value);
                }
            }
            Ustr = URLDecoder.decode(Ustr, "UTF-8");
        } catch (Exception e) {
            toLog(e, "JavaWayUtil");
        }
        return Ustr;
    }



    private static final String AES_KEY = "1q30vqwerf8yiac3";

    public static String aesDecrypt(String encrypted) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
        return new String(original);
    }
}
