package com.cboda.intelligent.utils;



import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringBroadUtils {

    public static String removeDecimalPart(String money) {
        try {
            return money.replaceAll("\\..*", "");
        } catch (Exception e) {
            AppBroadUtils.Companion.debug("format","-->异常 "+e);
        }
        return money;
    }

    public static String format(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (Exception e) {
           AppBroadUtils.Companion.debug("format","-->异常 "+e);
        }
        return "";
    }


    public static String format(Locale l, String format, Object... args) {
        try {
            return String.format(l, format, args);
        } catch (Exception e) {
            AppBroadUtils.Companion.debug("format11","-->异常 "+e);
        }
        return "";
    }

    /**
     * 多个空格转换为一个空格
     */
    public static String replaceLineBlanks(String str) {
        String result = "";
        if (str != null) {
            Pattern p = Pattern.compile("(\r?\n(\\s*\r?\n)+)");
            Matcher m = p.matcher(str);
            result = m.replaceAll("\r\n");
        }
        return result;

    }
    
    /**
     * 判断是否包含大写字母
     * @param str
     * @return
     */
    public static boolean isMaxWrite(String str){
        boolean hasUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
                break;
            }
        }
        return hasUpperCase;
    }
}
