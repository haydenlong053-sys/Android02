package com.lxkj.aiim.lj.utils;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtil {

    public static boolean isEmpty(final String s) {
        return null == s || s.trim().length() == 0;
    }

    public static boolean isEqual(final String s1, final String s2) {
        return (s1 == null && s2 == null) || (null != s1 && s1.equals(s2));
    }

    public static String getFileNameFromUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        return url.replaceAll("[.:/,%?&= ]", "+").replaceAll("[+]+", "+");
    }

    public static String cutLength(final String s, final int len) {
        return (s != null && s.length() >= len) ? s.substring(0, 9) : s;
    }

    public static String replaceBrace(final String s) {
        return (null == s) ? null : s.replace("{", "").replace("}", "");
    }

    public static String replaceBlank(final String s) {
        return (null == s) ? null : s.replaceAll("[\n]", " ");
    }

    public static byte[] getBytes(final String src) {
        if (null == src || src.length() == 0) {
            return null;
        }
        try {
            return src.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    // 将输入流转换成字符串
    public static String parseInStream(final InputStream is) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray());
    }

    /**
     * Convert byte[] to hex string.
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder sb = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            sb.append(String.format("%02X", Byte.valueOf(src[i])));
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        for (int i = 0; i < ret.length; i++) {
            Integer aInt = Integer.parseInt(src.substring(i * 2, i * 2 + 2), 16);
            ret[i] = aInt.byteValue();
        }
        return ret;
    }

    public static String strFloatOne(float f) {
        return String.format(Locale.getDefault(), "%.1f", f);
    }

    public static String removeFilter(String str, String filter) {
        return null == str ? null : str.replaceAll(filter, "");

    }
    public static String getDateStr(Date date, String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }
}
