package com.freddy.chat.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.GlideUtilsKt;
import com.cboda.baselibrary.view.AppRoundBroadImage;
import com.cboda.intelligent.R;
import com.freddy.chat.media.MessageMediaPreviewHelper;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kotlin.Unit;

public final class ImageMessageVH extends BaseMessageVH {
    public final AppRoundBroadImage ivMessageImage;

    public ImageMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_image, false);
        ivMessageImage = (AppRoundBroadImage) contentView;
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        ELogUtilKt.eLogD("zhanglong", "图片消息getContent--->" + message.getContent());
        bindCommon(message, position, context);
        boolean meetingImage = MessageType.MessageContentType.isMeetingImage(message.getMsgContentType());
        if (meetingImage) {
            // 对齐野火会议弹幕图：只展示图片，隐藏头像/信用分/昵称/时间等
            applyMeetingImageChromeOnly();
        }
        applyImageBubbleSize(message, meetingImage);
        boolean sentByMe = message.isSender(context.currentUserId);
        String imgUrl = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
        String thumbnailsImg = MediaMessageContentUtil.resolveDisplayThumbUrl(message.getContent(), sentByMe);

        if (meetingImage) {
            // 对齐野火：宽 = 屏宽 - 40dp，高按位图比例自适应（mHeight=0）
            int targetW = itemView.getResources().getDisplayMetrics().widthPixels
                    - itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_40);
            GlideUtilsKt.loadDiyImg(ivMessageImage, imgUrl, targetW, 0, () -> Unit.INSTANCE);
        } else {
            if (!thumbnailsImg.isEmpty()){
                GlideUtilsKt.loadImg(ivMessageImage,thumbnailsImg);
            }else {
                GlideUtilsKt.loadImg(ivMessageImage,imgUrl);
            }

//            Glide.with(ivMessageImage.getContext())
//                    .load(imgUrl)
//                    .placeholder(R.drawable.bg_bubble_received)
//                    .thumbnail(Glide.with(ivMessageImage.getContext()).load(thumbnailsImg)
//                            .placeholder(R.drawable.bg_bubble_received))
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(ivMessageImage);
        }

        ivMessageImage.setOnClickListener(v -> MessageMediaPreviewHelper.previewImages(
                v.getContext(), context.items, message, context.currentUserId));
    }

    /** 会议弹幕图：去掉发送者头像、信用分、昵称、时间、回执等，仅保留图片。 */
    private void applyMeetingImageChromeOnly() {
        avatarLeft.setVisibility(View.GONE);
        avatarRight.setVisibility(View.GONE);
        avatarLeft.setOnClickListener(null);
        avatarRight.setOnClickListener(null);
        avatarLeft.setOnLongClickListener(null);
        nameLayout.setVisibility(View.GONE);
        tvNickname.setVisibility(View.GONE);
        joinTimeTv.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        errorLinearLayout.setVisibility(View.GONE);
        singleReceiptImageView.setVisibility(View.GONE);
        groupReceiptFrameLayout.setVisibility(View.GONE);

        int horizontal = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_20);
        int bottom = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_2);
        rowContainer.setPadding(horizontal, 0, horizontal, bottom);

        ViewGroup.MarginLayoutParams hostLp =
                (ViewGroup.MarginLayoutParams) flMessageContentHost.getLayoutParams();
        hostLp.setMarginStart(0);
        hostLp.setMarginEnd(0);
        flMessageContentHost.setLayoutParams(hostLp);

        messageColumn.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        bubbleRow.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
    }

    /**
     * 普通图：约 140dp 气泡；会议弹幕图(201)：先占位宽 = 屏宽 - 40dp（最终尺寸由 loadDiyImg 按比例校正）。
     */
    private void applyImageBubbleSize(@NonNull AppMessage message, boolean meetingImage) {
        ViewGroup.LayoutParams lp = ivMessageImage.getLayoutParams();
        if (meetingImage) {
            int targetW = itemView.getResources().getDisplayMetrics().widthPixels
                    - itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_40);
            int dw = extractImageDimension(message, "dw");
            int dh = extractImageDimension(message, "dh");
            lp.width = Math.max(1, targetW);
            if (dw > 0 && dh > 0) {
                lp.height = Math.max(1, Math.round(targetW * (dh * 1f / dw)));
            } else {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            ivMessageImage.setLayoutParams(lp);
            ivMessageImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivMessageImage.setAdjustViewBounds(true);
            return;
        }

        int size = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_140);
        int min = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_min);
        int max = itemView.getResources().getDimensionPixelSize(R.dimen.wechat_image_bubble_max);
        int dw = extractImageDimension(message, "dw");
        int dh = extractImageDimension(message, "dh");
        int targetW = size;
        int targetH = size;
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
        lp.width = targetW;
        lp.height = targetH;
        ivMessageImage.setLayoutParams(lp);
        ivMessageImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivMessageImage.setAdjustViewBounds(false);
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
