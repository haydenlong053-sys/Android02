package com.cboda.baselibrary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class VideoThumbnailUtils {

    /**
     * 使用 Glide 异步获取视频第一帧
     * @param context 上下文
     * @param videoUri 视频URI（可以是本地路径或网络URL）
     * @param callback 回调接口
     */
    public static void getVideoFirstFrame(Context context, String videoUri,
                                          ThumbnailCallback callback) {

        Glide.with(context)
                .asBitmap()
                .load(videoUri)
                .apply(new RequestOptions()
                        .frame(0)  // 获取第0毫秒的帧（第一帧）
                        .centerCrop())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable Transition<? super Bitmap> transition) {
                        if (callback != null) {
                            callback.onSuccess(bitmap);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (callback != null) {
                            callback.onError("Failed to load video thumbnail");
                        }
                    }
                });
    }

    /**
     * 同步方式获取视频第一帧
     * 注意：需要在子线程中调用
     */
    public static Bitmap getVideoFirstFrameSync(Context context, String videoUri) {
        try {
            return Glide.with(context)
                    .asBitmap()
                    .load(videoUri)
                    .apply(new RequestOptions().frame(0))
                    .submit()
                    .get();  // 阻塞直到获取完成
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface ThumbnailCallback {
        void onSuccess(Bitmap bitmap);
        void onError(String error);
    }
}
