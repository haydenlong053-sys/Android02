package com.freddy.chat.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.freddy.chat.MessageImagePreviewActivity;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ImageMessageVH extends BaseMessageVH {
    public final ImageView ivMessageImage;

    public ImageMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_image);
        ivMessageImage = contentView.findViewById(R.id.iv_message_image);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        Log.d("zhanglong", "图片消息getContent--->" + message.getContent());
        bindCommon(message, position, context);
        applyImageBubbleSize(message);
        boolean sentByMe = message.isSender(context.currentUserId);
        String imgUrl = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
        String thumbnailsImg = MediaMessageContentUtil.resolveDisplayThumbUrl(message.getContent(), sentByMe);

        Glide.with(ivMessageImage.getContext())
                .load(imgUrl)
                .placeholder(R.drawable.bg_bubble_received)   // 原图加载前的占位图
                .thumbnail(
                        Glide.with(ivMessageImage.getContext())
                                .load(thumbnailsImg)
                                .placeholder(R.drawable.bg_bubble_received)  // 缩略图的占位图
                                .error(R.drawable.bg_bubble_received)
                )
                .error(R.drawable.bg_bubble_received)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivMessageImage);

//        Glide.with(ivMessageImage.getContext())
//                .load(imgUrl+SizeUtil.SMALL_SIZE)
//                .centerCrop()
//                .placeholder(R.drawable.bg_bubble_received)
//                .into(ivMessageImage);
        ivMessageImage.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MessageImagePreviewActivity.class);
            intent.putExtra(MessageImagePreviewActivity.EXTRA_IMAGE_PATH, imgUrl);
            intent.putExtra(MessageImagePreviewActivity.EXTRA_THUMB_IMAGE_PATH, thumbnailsImg);
            v.getContext().startActivity(intent);
        });
    }

    private void applyImageBubbleSize(@NonNull AppMessage message) {
        int min = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_min);
        int max = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_max);
        int fallback = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_size);
        int dw = extractImageDimension(message, "dw");
        int dh = extractImageDimension(message, "dh");
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
        ViewGroup.LayoutParams lp = ivMessageImage.getLayoutParams();
        lp.width = targetW;
        lp.height = targetH;
        ivMessageImage.setLayoutParams(lp);
    }

    @Nullable
    private static JsonObject parseImageContent(@Nullable AppMessage message) {
        if (message == null || TextUtils.isEmpty(message.getContent())) {
            return null;
        }
        try {
            return JsonParser.parseString(message.getContent()).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static String readStringField(@NonNull JsonObject obj, @NonNull String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        String value = obj.get(key).getAsString();
        return TextUtils.isEmpty(value) ? null : value.trim();
    }


    private static int extractImageDimension(@Nullable AppMessage message, @NonNull String key) {
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
}
