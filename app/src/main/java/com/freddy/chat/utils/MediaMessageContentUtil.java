package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
