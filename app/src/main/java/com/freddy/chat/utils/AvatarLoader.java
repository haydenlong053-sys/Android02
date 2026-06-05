package com.freddy.chat.utils;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.cboda.intelligent.R;

/**
 * 头像加载：网络图 + 圆形裁剪，失败时显示首字占位。
 */
public final class AvatarLoader {

    private static final RequestOptions CIRCLE = RequestOptions.bitmapTransform(new CircleCrop());

    private AvatarLoader() {
    }

    public static void bind(@NonNull ImageView imageView,
                            @NonNull TextView fallbackView,
                            @Nullable String imageUrl,
                            @Nullable String labelPrimary,
                            @Nullable String labelFallback) {
        String label = WeChatUiHelper.avatarLabel(labelPrimary, labelFallback);
        fallbackView.setText(label);

        String url = normalizeUrl(imageUrl);
        if (TextUtils.isEmpty(url)) {
            imageView.setVisibility(View.GONE);
            fallbackView.setVisibility(View.VISIBLE);
            Glide.with(imageView).clear(imageView);
            return;
        }

        imageView.setVisibility(View.INVISIBLE);
        fallbackView.setVisibility(View.VISIBLE);
        Glide.with(imageView)
                .load(url)
                .apply(CIRCLE)
                .placeholder(R.drawable.bg_avatar_clip)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                @NonNull Target<Drawable> target,
                                                boolean isFirstResource) {
                        imageView.setVisibility(View.GONE);
                        fallbackView.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   @NonNull DataSource dataSource,
                                                   boolean isFirstResource) {
                        fallbackView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);
    }

    @Nullable
    private static String normalizeUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("//")) {
            return "https:" + trimmed;
        }
        return trimmed;
    }
}
