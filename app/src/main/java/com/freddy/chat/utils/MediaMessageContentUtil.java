package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.message.MessageContentFormatter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;

/**
 * 媒体消息 content JSON 解析；发送方展示优先 {@code localUrl}。
 */
public final class MediaMessageContentUtil {

    public static final String KEY_URL = "url";
    public static final String KEY_THUMB_URL = "thumbUrl";
    public static final String KEY_LOCAL_URL = "localUrl";
    public static final String KEY_COVER_URL = "coverUrl";
    public static final String KEY_LOCAL_COVER_URL = "localCoverUrl";

    private MediaMessageContentUtil() {
    }

    /**
     * 视频封面：发送方优先 {@code localCoverUrl}，否则 {@code coverUrl}；接收方用 {@code coverUrl}。
     */
    @NonNull
    public static String resolveCoverDisplayUrl(@Nullable String contentJson, boolean sentByMe) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        if (sentByMe) {
            String localCover = readString(obj, KEY_LOCAL_COVER_URL);
            if (isUsableLocalPath(localCover)) {
                return localCover;
            }
        }
        String remoteCover = readString(obj, KEY_COVER_URL);
        return remoteCover != null ? remoteCover : "";
    }

    /**
     * 视频播放地址：发送方优先 {@code localUrl}，否则 {@code url}。
     */
    @NonNull
    public static String resolveVideoPlayUrl(@Nullable String contentJson, boolean sentByMe) {
        return resolveDisplayUrl(contentJson, sentByMe);
    }

    /**
     * 解析用于 UI 加载的地址：发送方且本地文件仍存在时用 {@code localUrl}，否则用 {@code url}。
     */
    @NonNull
    public static String resolveDisplayUrl(@Nullable String contentJson, boolean sentByMe) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        if (sentByMe) {
            String local = readString(obj, KEY_LOCAL_URL);
            if (isUsableLocalPath(local)) {
                return local;
            }
        }
        String remote = readString(obj, KEY_URL);
        return remote != null ? remote : "";
    }

    public static int resolveVoiceDurationSec(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null || !obj.has("duration") || !obj.get("duration").isJsonPrimitive()) {
            return 0;
        }
        try {
            return Math.max(0, obj.get("duration").getAsInt());
        } catch (Exception ignored) {
            return 0;
        }
    }

    @NonNull
    public static String resolveFileName(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        String name = readString(obj, "msg");
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        String url = readString(obj, KEY_URL);
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int slash = url.lastIndexOf('/');
        return slash >= 0 ? url.substring(slash + 1) : url;
    }

    public static long resolveFileSizeBytes(@Nullable String contentJson, boolean sentByMe) {
        JsonObject obj = parseObject(contentJson);
        if (obj != null && obj.has("size") && obj.get("size").isJsonPrimitive()) {
            try {
                return Math.max(0L, obj.get("size").getAsLong());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        String localPath = resolveDisplayUrl(contentJson, sentByMe);
        if (isUsableLocalPath(localPath)) {
            return new File(localPath).length();
        }
        return 0L;
    }

    @NonNull
    public static String resolveDisplayThumbUrl(@Nullable String contentJson, boolean sentByMe) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        if (sentByMe) {
            String local = readString(obj, KEY_LOCAL_URL);
            if (isUsableLocalPath(local)) {
                return local;
            }
        }
        String remoteThumbUrl = readString(obj, KEY_THUMB_URL);
        if (!TextUtils.isEmpty(remoteThumbUrl)){
            return remoteThumbUrl;
        }
        String remote = readString(obj, KEY_URL);
        return remote != null ? remote : "";
    }


    @NonNull
    public static String resolveLocalUrl(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        return readString(obj, KEY_LOCAL_URL);
    }

    @NonNull
    public static String resolveLocalCoverUrl(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        return readString(obj, KEY_LOCAL_COVER_URL);
    }

    @NonNull
    public static String resolveRemoteUrl(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return "";
        }
        return readString(obj, KEY_URL);
    }

    public static boolean hasRemoteUrl(@Nullable String contentJson) {
        return isHttpUrl(resolveRemoteUrl(contentJson));
    }

    /** 视频远端封面 {@code coverUrl} 是否为可用 http(s) 地址。 */
    public static boolean hasRemoteCoverUrl(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            return false;
        }
        return isHttpUrl(readString(obj, KEY_COVER_URL));
    }

    /**
     * 发信令前校验：图/语音/文件须有远端 {@code url}；视频还须有远端 {@code coverUrl}。
     * 非媒体类型直接通过。
     */
    public static boolean isMediaReadyForTransmit(int msgContentType, @Nullable String contentJson) {
        boolean media = MessageContentFormatter.isImageMessage(msgContentType)
                || MessageContentFormatter.isVoiceMessage(msgContentType)
                || MessageContentFormatter.isFileMessage(msgContentType)
                || MessageContentFormatter.isVideoMessage(msgContentType);
        if (!media) {
            return true;
        }
        if (!hasRemoteUrl(contentJson)) {
            return false;
        }
        //没封面图，暂时也放行
//        if (MessageContentFormatter.isVideoMessage(msgContentType)) {
//            return hasRemoteCoverUrl(contentJson);
//        }
        return true;
    }

    public static boolean isUsableLocalFile(@Nullable String path) {
        return isUsableLocalPath(path);
    }

    private static boolean isHttpUrl(@Nullable String url) {
        return !TextUtils.isEmpty(url)
                && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static int resolveVideoWidth(@Nullable String contentJson) {
        return readIntField(contentJson, "dw", 180);
    }

    public static int resolveVideoHeight(@Nullable String contentJson) {
        return readIntField(contentJson, "dh", 120);
    }

    private static int readIntField(@Nullable String contentJson, @NonNull String key, int fallback) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return fallback;
        }
        try {
            return Math.max(0, obj.get(key).getAsInt());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean isUsableLocalPath(@Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String trimmed = path.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return false;
        }
        return new File(trimmed).isFile();
    }

    @Nullable
    private static JsonObject parseObject(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            return JsonParser.parseString(raw.trim()).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    private static String readString(@NonNull JsonObject obj, @NonNull String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return "";
        }
        String value = obj.get(key).getAsString();
        return TextUtils.isEmpty(value) ? "" : value.trim();
    }
}
