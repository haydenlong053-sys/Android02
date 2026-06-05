package com.freddy.chat.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.freddy.chat.MessageVideoPreviewActivity;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Locale;

public final class VideoMessageVH extends BaseMessageVH {

    private final ImageView ivVideoCover;
    private final TextView tvVideoDuration;

    public VideoMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_video);
        ivVideoCover = contentView.findViewById(R.id.iv_video_cover);
        tvVideoDuration = contentView.findViewById(R.id.tv_video_duration);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        applyVideoBubbleSize(message);
        String coverUrl = MediaMessageContentUtil.resolveCoverDisplayUrl(message.getContent(), sentByMe);
        Log.d("zhanglong", "getContent--->" + message.getContent());

        Glide.with(ivVideoCover.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.bg_bubble_received)
                .error(R.drawable.bg_bubble_received)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivVideoCover);

        String duration = formatDuration(extractDurationSec(message));
        tvVideoDuration.setText(duration);

        String playUrl = MediaMessageContentUtil.resolveVideoPlayUrl(message.getContent(), sentByMe);
        View.OnClickListener openPreview = v -> {
            if (TextUtils.isEmpty(playUrl)) {
                return;
            }
            Intent intent = new Intent(v.getContext(), MessageVideoPreviewActivity.class);
            intent.putExtra(MessageVideoPreviewActivity.EXTRA_VIDEO_PATH, playUrl);
            v.getContext().startActivity(intent);
        };
        contentView.setOnClickListener(openPreview);
        ivVideoCover.setOnClickListener(openPreview);
    }

    private void applyVideoBubbleSize(@NonNull AppMessage message) {
        int min = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_min);
        int max = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_max);
        int fallback = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_size);
        int dw = extractDimension(message, "dw");
        int dh = extractDimension(message, "dh");
        int targetW = fallback;
        int targetH = fallback;
        if (dw > 0 && dh > 0) {
            float ratio = dw * 1f / dh;
            if (ratio >= 1f) {
                targetW = max;
                targetH = Math.max(min, Math.round(max / ratio));
            } else {
                targetH = max;
                targetW = Math.max(min, Math.round(max * ratio));
            }
        }
        ViewGroup.LayoutParams lp = ivVideoCover.getLayoutParams();
        lp.width = targetW;
        lp.height = targetH;
        ivVideoCover.setLayoutParams(lp);
    }

    private static int extractDimension(@Nullable AppMessage message, @NonNull String key) {
        if (message == null || TextUtils.isEmpty(message.getContent())) {
            return 0;
        }
        try {
            JsonObject obj = JsonParser.parseString(message.getContent()).getAsJsonObject();
            if (obj.has(key) && obj.get(key).isJsonPrimitive()) {
                return Math.max(0, obj.get(key).getAsInt());
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static int extractDurationSec(@Nullable AppMessage message) {
        if (message == null || TextUtils.isEmpty(message.getContent())) {
            return 0;
        }
        try {
            JsonObject obj = JsonParser.parseString(message.getContent()).getAsJsonObject();
            if (obj.has("duration") && obj.get("duration").isJsonPrimitive()) {
                return Math.max(0, obj.get("duration").getAsInt());
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    @NonNull
    private static String formatDuration(int durationSec) {
        int sec = Math.max(0, durationSec);
        int min = sec / 60;
        int remain = sec % 60;
        if (min > 99) {
            min = 99;
        }
        return String.format(Locale.getDefault(), "%02d:%02d", min, remain);
    }
}
