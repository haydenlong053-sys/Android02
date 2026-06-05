package com.freddy.im.http.model.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@code POST filehub/batchUpload} 视频场景：files[0]=封面，files[1]=视频。
 */
public final class VideoBatchUploadResult {

    @Nullable
    private final String coverUrl;
    @NonNull
    private final String videoUrl;

    public VideoBatchUploadResult(@Nullable String coverUrl, @NonNull String videoUrl) {
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
    }

    @Nullable
    public String getCoverUrl() {
        return coverUrl;
    }

    @NonNull
    public String getVideoUrl() {
        return videoUrl;
    }

    @Override
    public String toString() {
        return "VideoBatchUploadResult{" +
                "coverUrl='" + coverUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}
