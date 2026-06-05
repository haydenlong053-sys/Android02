package com.freddy.chat.media;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cboda.select.picture.engine.ImageEngine;
import com.cboda.select.picture.utils.ActivityCompatHelper;

/**
 * PictureSelector 图片加载引擎（Glide），参考官方 Demo。
 *
 * @see <a href="https://github.com/LuckSiege/PictureSelector/blob/version_component/README_CN.md">PictureSelector README</a>
 */
public final class PictureSelectorGlideEngine implements ImageEngine {

    private static final class Holder {
        static final PictureSelectorGlideEngine INSTANCE = new PictureSelectorGlideEngine();
    }

    private PictureSelectorGlideEngine() {
    }

    @NonNull
    public static PictureSelectorGlideEngine createGlideEngine() {
        return Holder.INSTANCE;
    }

    @Override
    public void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context).load(url).into(imageView);
    }

    @Override
    public void loadImage(@NonNull Context context, @NonNull ImageView imageView, @NonNull String url,
                          int maxWidth, int maxHeight) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context).load(url).override(maxWidth, maxHeight).into(imageView);
    }

    @Override
    public void loadAlbumCover(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .load(url)
                .override(180, 180)
                .sizeMultiplier(0.5f)
                .transform(new CenterCrop(), new RoundedCorners(8))
                .into(imageView);
    }

    @Override
    public void loadGridImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .override(200, 200)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void pauseRequests(Context context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context).pauseRequests();
    }

    @Override
    public void resumeRequests(Context context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context).resumeRequests();
    }
}
