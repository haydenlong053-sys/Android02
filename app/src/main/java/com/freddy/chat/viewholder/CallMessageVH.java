package com.freddy.chat.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.content.res.ColorStateList;

import com.cboda.intelligent.R;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageContentFormatter;

/**
 * 通话记录气泡：图标 + 文案，样式对齐会话 Voip 消息（蓝底白字 / 白底蓝图标）。
 */
public final class CallMessageVH extends BaseMessageVH {
    private final LinearLayout callRoot;
    private final ImageView callIconView;
    private final TextView callTextView;

    public CallMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_call, false);
        callRoot = contentView.findViewById(R.id.call_root);
        callIconView = contentView.findViewById(R.id.iv_call_icon);
        callTextView = contentView.findViewById(R.id.tv_call_text);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        applyBroadBubbleBackground(callRoot, sentByMe);

        String content = message.getContent();
        boolean video = MessageContentFormatter.isCallVideoMedia(content);
        callIconView.setImageResource(video
                ? com.cboda.chatui.R.mipmap.ic_msg_cell_video_broad_call
                : com.cboda.chatui.R.mipmap.ic_msg_cell_voice_broad_call);

        int textColor;
        int iconColor;
        if (sentByMe) {
            textColor = ContextCompat.getColor(itemView.getContext(), android.R.color.white);
            iconColor = textColor;
        } else {
            textColor = ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_text0);
            iconColor = ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_main1);
        }
        callTextView.setTextColor(textColor);
        ImageViewCompat.setImageTintList(callIconView, ColorStateList.valueOf(iconColor));
        callTextView.setText(MessageContentFormatter.formatCallDisplayText(
                content, sentByMe, context.currentUserId, context.isGroupChat));

        reorderCallChildren(sentByMe);
    }

    /** 发出：文案在左、图标在右；接收：图标在左、文案在右。 */
    private void reorderCallChildren(boolean sentByMe) {
        callRoot.removeAllViews();
        ViewGroup.MarginLayoutParams iconLp = (ViewGroup.MarginLayoutParams) callIconView.getLayoutParams();
        int gap = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_8);
        if (sentByMe) {
            iconLp.setMarginStart(gap);
            iconLp.setMarginEnd(0);
            callIconView.setLayoutParams(iconLp);
            callRoot.addView(callTextView);
            callRoot.addView(callIconView);
        } else {
            iconLp.setMarginStart(0);
            iconLp.setMarginEnd(gap);
            callIconView.setLayoutParams(iconLp);
            callRoot.addView(callIconView);
            callRoot.addView(callTextView);
        }
    }
}
