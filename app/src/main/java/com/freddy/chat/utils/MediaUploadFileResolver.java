package com.freddy.chat.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 分区存储适配：将 {@code content://}、{@code file://} 或普通路径解析为可读的本地文件。
 */
public final class MediaUploadFileResolver {

    public enum MediaKind {
        IMAGE,
        VIDEO,
        FILE
    }

    private MediaUploadFileResolver() {
    }

    @NonNull
    public static MediaKind detectMediaKind(@NonNull Context context,
                                            @Nullable Uri uri,
                                            @Nullable File file) {
        String mime = null;
        if (uri != null) {
            try {
                mime = context.getContentResolver().getType(uri);
            } catch (Exception ignored) {
            }
        }
        if (TextUtils.isEmpty(mime) && file != null) {
            mime = guessMimeFromFileName(file.getName());
        }
        if (!TextUtils.isEmpty(mime)) {
            String lower = mime.toLowerCase();
            if (lower.startsWith("image/")) {
                return MediaKind.IMAGE;
            }
            if (lower.startsWith("video/")) {
                return MediaKind.VIDEO;
            }
        }
        String name = file != null ? file.getName() : null;
        if (isImageFileName(name)) {
            return MediaKind.IMAGE;
        }
        if (isVideoFileName(name)) {
            return MediaKind.VIDEO;
        }
        return MediaKind.FILE;
    }

    public static boolean isImage(@NonNull Context context,
                                  @Nullable Uri uri,
                                  @Nullable File file) {
        return detectMediaKind(context, uri, file) == MediaKind.IMAGE;
    }

    public static boolean isVideo(@NonNull Context context,
                                @Nullable Uri uri,
                                @Nullable File file) {
        return detectMediaKind(context, uri, file) == MediaKind.VIDEO;
    }

    /**
     * @return 可直接用于 {@code File} 上传/解码的本地路径；{@code content://} 会复制到应用 cache
     */
    @Nullable
    public static File resolve(@NonNull Context context, @Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.startsWith("content://")) {
            return copyUriToCacheFile(context, Uri.parse(trimmed));
        }
        if (trimmed.startsWith("file://")) {
            String filePath = Uri.parse(trimmed).getPath();
            if (!TextUtils.isEmpty(filePath)) {
                File file = new File(filePath);
                if (file.isFile()) {
                    return file;
                }
            }
            return null;
        }
        File file = new File(trimmed);
        return file.isFile() ? file : null;
    }

    @Nullable
    public static File resolve(@NonNull Context context, @Nullable Uri uri) {
        if (uri == null) {
            return null;
        }
        return resolve(context, uri.toString());
    }

    @Nullable
    private static File copyUriToCacheFile(@NonNull Context context, @NonNull Uri uri) {
        try {
            String fileName = queryDisplayName(context, uri);
            if (TextUtils.isEmpty(fileName)) {
                fileName = "media_" + System.currentTimeMillis() + "_" + (uri.hashCode() & 0xFFFF)
                        + guessSuffix(context, uri);
            }
            File outFile = new File(context.getCacheDir(), fileName);
            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(outFile)) {
                if (in == null) {
                    return null;
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            return outFile.isFile() && outFile.length() > 0 ? outFile : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    private static String guessSuffix(@NonNull Context context, @NonNull Uri uri) {
        String mime = context.getContentResolver().getType(uri);
        if ("image/png".equalsIgnoreCase(mime)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(mime)) {
            return ".webp";
        }
        if ("image/gif".equalsIgnoreCase(mime)) {
            return ".gif";
        }
        if ("video/mp4".equalsIgnoreCase(mime)) {
            return ".mp4";
        }
        return ".jpg";
    }

    @Nullable
    private static String guessMimeFromFileName(@Nullable String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (lower.endsWith(".heic") || lower.endsWith(".heif")) {
            return "image/heic";
        }
        if (lower.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (lower.endsWith(".mov")) {
            return "video/quicktime";
        }
        if (lower.endsWith(".3gp")) {
            return "video/3gpp";
        }
        if (lower.endsWith(".m4v")) {
            return "video/x-m4v";
        }
        if (lower.endsWith(".webm")) {
            return "video/webm";
        }
        return null;
    }

    private static boolean isImageFileName(@Nullable String fileName) {
        String mime = guessMimeFromFileName(fileName);
        return mime != null && mime.startsWith("image/");
    }

    private static boolean isVideoFileName(@Nullable String fileName) {
        String mime = guessMimeFromFileName(fileName);
        return mime != null && mime.startsWith("video/");
    }

    @Nullable
    private static String queryDisplayName(@NonNull Context context, @NonNull Uri uri) {
        try (Cursor c = context.getContentResolver().query(
                uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int index = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = c.getString(index);
                    if (!TextUtils.isEmpty(name)) {
                        return name.trim();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
