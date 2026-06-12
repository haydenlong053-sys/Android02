package com.freddy.chat.collect;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.message.MessageContentFormatter;

/**
 * 收藏项 {@code info} 展示解析：优先按自研消息 JSON，兼容野火纯 URL / 纯文本。
 */
public final class CollectContentParser {

    public static final String TYPE_TEXT = "1";
    public static final String TYPE_IMAGE = "2";
    public static final String TYPE_VIDEO = "3";
    public static final String TYPE_VOICE = "4";
    public static final String TYPE_LOCATION = "5";

    private CollectContentParser() {
    }

    @NonNull
    public static String resolveDisplayText(@Nullable String collectType, @Nullable String info) {
        if (TextUtils.isEmpty(info) || "null".equalsIgnoreCase(info.trim())) {
            return "";
        }
        if (TYPE_TEXT.equals(collectType) || TYPE_LOCATION.equals(collectType)) {
            String plain = MessageContentFormatter.extractPlainText(info);
            return !TextUtils.isEmpty(plain) ? plain : info.trim();
        }
        return info.trim();
    }

    /** 图片 / 视频封面加载地址。 */
    @NonNull
    public static String resolveImageDisplayUrl(@Nullable String collectType, @Nullable String info) {
        if (TextUtils.isEmpty(info)) {
            return "";
        }
        if (TYPE_IMAGE.equals(collectType)) {
            String url = MediaMessageContentUtil.resolveDisplayThumbUrl(info, false);
            if (!TextUtils.isEmpty(url)) {
                return url;
            }
            if (isHttpUrl(info)) {
                return info.trim();
            }
            return MediaMessageContentUtil.resolveRemoteUrl(info);
        }
        if (TYPE_VIDEO.equals(collectType)) {
            String cover = MediaMessageContentUtil.resolveCoverDisplayUrl(info, false);
            if (!TextUtils.isEmpty(cover)) {
                return cover;
            }
            String remote = MediaMessageContentUtil.resolveRemoteUrl(info);
            if (!TextUtils.isEmpty(remote)) {
                return remote;
            }
            return isHttpUrl(info) ? info.trim() : "";
        }
        return "";
    }

    public static int resolveVoiceDurationSec(@Nullable String collectType,
                                              @Nullable String info,
                                              @Nullable String miao) {
        if (!TYPE_VOICE.equals(collectType)) {
            return 0;
        }
        int fromJson = MediaMessageContentUtil.resolveVoiceDurationSec(info);
        if (fromJson > 0) {
            return fromJson;
        }
        if (!TextUtils.isEmpty(miao)) {
            try {
                return Math.max(0, Integer.parseInt(miao.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public static boolean isSupportedSendType(@Nullable String collectType) {
        return TYPE_TEXT.equals(collectType)
                || TYPE_IMAGE.equals(collectType)
                || TYPE_VIDEO.equals(collectType)
                || TYPE_VOICE.equals(collectType);
    }

    private static boolean isHttpUrl(@NonNull String raw) {
        String trimmed = raw.trim();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }
}
