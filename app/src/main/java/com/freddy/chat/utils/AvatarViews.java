package com.freddy.chat.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.intelligent.R;

/**
 * 从 {@code include view_avatar} 根节点解析 ImageView / 占位 TextView。
 */
public final class AvatarViews {

    private AvatarViews() {
    }

    @NonNull
    public static ImageView imageView(@NonNull View includeRoot) {
        return includeRoot.findViewById(R.id.iv_avatar);
    }

    @NonNull
    public static TextView fallback(@NonNull View includeRoot) {
        return includeRoot.findViewById(R.id.tv_avatar_fallback);
    }

    public static void bind(@NonNull View includeRoot,
                            @Nullable String imageUrl,
                            @Nullable String labelPrimary,
                            @Nullable String labelFallback) {
        AvatarLoader.bind(imageView(includeRoot), fallback(includeRoot),
                imageUrl, labelPrimary, labelFallback);
    }
}
