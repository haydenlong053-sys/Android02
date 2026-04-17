package com.cboda.baselibrary.utils;

import static com.cboda.baselibrary.utils.ELogUtilKt.toLog;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.USER_CREDIT_SCORE_NEW_USER_DAYS;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.USER_SEND_LINK;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.anyToInt;

import android.util.Base64;

import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class JavaBroadUtil {

    // 预编译正则，避免重复创建提升性能（与 BroadE.kt 中 link 规则一致，须闭合最外层 (?: ）
    private static String LINK_PATTERN_BEFORE =
            "(?i)\\b(?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.](?:com|cn|net|org|io|tv|cc|me|xyz|top|fun|online|site|tech|store|app|dev|cloud|link|info|biz|gov|edu|mobi|name|pro|coop|aero|jobs|museum|travel|tel|asia|post|cat|xxx|[a-z]{2,})(?:/\\S*)?\\b|"
                    + "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b|"
                    + "\\b(?:weixin://|alipays://|taobao://|zhihu://|douyin://|bilibili://|tencent://|qq://|mailto:|tel:|sms:|file://|content://|ftp://)\\b)";
    private static Pattern LINK_PATTERN = Pattern.compile(LINK_PATTERN_BEFORE, Pattern.CASE_INSENSITIVE);

    /**
     * 检测文本是否包含链接(获取时间)
     *
     * @param text 待检测文本
     * @return true 表示包含链接，false 表示安全
     */
    public static String containsLinkReturnDay(String text) {
        String day = "";
        if (text == null || text.isEmpty()) return day;
        // 防御零宽字符、全角空格等绕过手段
        String cleaned = text
                .replaceAll("[\\u200B\\u200C\\u200D\\uFEFF\\u00A0\\u3000]", "")
                .trim();
        if (LINK_PATTERN.matcher(cleaned).find()) {
            String canSendLink = MMKVBroadUtils.Companion.getSqlStrData(USER_SEND_LINK, "false");
            if (!canSendLink.equals("true")) {
                int dayInt = anyToInt(MMKVBroadUtils.Companion.getSqlStrData(USER_CREDIT_SCORE_NEW_USER_DAYS, ""));
                if (dayInt <= 0) {
                    dayInt = 61;
                }
                day = String.valueOf(dayInt);
            }
            return day;
        } else {
            return day;
        }
//        return LINK_PATTERN.matcher(cleaned).find();
    }

    public static String UStr_2_Str(String Ustr0) {
        String Ustr = Ustr0;
        String[] per = new String[6];
        try {
            int S = 0, BroadE = 0;
            String C = "", Value = "";
            while (Ustr.contains("\\u")) {
                S = Ustr.indexOf("\\u") + "\\u".length();
                BroadE = Ustr.indexOf("\\u", S);
                if (BroadE == -1) BroadE = Ustr.length();

                if (BroadE > S) {
                    C = Ustr.substring(S, BroadE);
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


//    private static final String AES_KEY = HttpsBroadConfig.INSTANCE.getAppAesPwd();
//
//    public static String aesDecrypt(String encrypted) throws Exception {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
//        byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
//        return new String(original);
//    }
}
