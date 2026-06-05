package com.freddy.chat.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.intelligent.app.MyBroadApp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 从本地视频提取封面与元数据。
 */
public final class VideoCoverUtil {

    public static final class Meta {
        public final int durationSec;
        public final int width;
        public final int height;
        @Nullable
        public final File coverFile;

        Meta(int durationSec, int width, int height, @Nullable File coverFile) {
            this.durationSec = durationSec;
            this.width = width;
            this.height = height;
            this.coverFile = coverFile;
        }
    }

    private VideoCoverUtil() {
    }

    @Nullable
    public static Meta extract(@NonNull String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            int durationSec = parseDurationSec(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION));
            Bitmap frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            int width = 0;
            int height = 0;
            File coverFile = null;
            if (frame != null) {
                width = frame.getWidth();
                height = frame.getHeight();
                coverFile = saveCoverJpeg(frame);
                if (!frame.isRecycled()) {
                    frame.recycle();
                }
            }
            if (width <= 0 || height <= 0) {
                width = 180;
                height = 120;
            }
            return new Meta(Math.max(1, durationSec), width, height, coverFile);
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private static int parseDurationSec(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return 1;
        }
        try {
            long ms = Long.parseLong(raw.trim());
            return Math.max(1, (int) ((ms + 999) / 1000));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    @Nullable
    private static File saveCoverJpeg(@NonNull Bitmap bitmap) {
        File dir = new File(MyBroadApp.cxt.getCacheDir(), "video_covers");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        File out = new File(dir, "cover_" + UUID.randomUUID() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)) {
                return null;
            }
            fos.flush();
            return out.isFile() && out.length() > 0 ? out : null;
        } catch (Exception ignored) {
            if (out.exists()) {
                out.delete();
            }
            return null;
        }
    }
}
