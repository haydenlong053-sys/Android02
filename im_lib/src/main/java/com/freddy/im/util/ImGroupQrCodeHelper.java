package com.freddy.im.util;

import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * 自研群二维码解析：识别 {@code im/group/getGroupByCodeId/{codeId}} 链接并取出 codeId。
 */
public final class ImGroupQrCodeHelper {

    private static final String PATH_MARKER = "im/group/getGroupByCodeId/";

    private ImGroupQrCodeHelper() {
    }

    public static boolean isGetGroupByCodeIdUrl(@Nullable String raw) {
        return !TextUtils.isEmpty(parseCodeId(raw));
    }

    /**
     * @return codeId，非自研群码则返回 null
     */
    @Nullable
    public static String parseCodeId(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        int idx = raw.indexOf(PATH_MARKER);
        if (idx < 0) {
            return null;
        }
        String rest = raw.substring(idx + PATH_MARKER.length());
        int query = rest.indexOf('?');
        if (query >= 0) {
            rest = rest.substring(0, query);
        }
        int slash = rest.indexOf('/');
        if (slash >= 0) {
            rest = rest.substring(0, slash);
        }
        rest = rest.trim();
        return TextUtils.isEmpty(rest) ? null : rest;
    }
}
