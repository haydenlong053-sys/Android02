package com.freddy.chat.viewholder;

import android.text.Editable;
import android.text.Spannable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.cboda.chatemoji.LQREmotionBroadKit;
import com.cboda.chatemoji.MoonBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

public class TextMessageVH extends BaseMessageVH {
    public final TextView tvMessage;

    public TextMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_text, false);
        tvMessage = contentView.findViewById(R.id.tv_message);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        applyBroadBubbleBackground(tvMessage, sentByMe);
        if (sentByMe) {
            tvMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            tvMessage.setLinkTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        } else {
            int textColor = ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_text0);
            tvMessage.setTextColor(textColor);
            tvMessage.setLinkTextColor(textColor);
        }
        bindMessageBody(message, context);
    }

    protected final void bindMessageBody(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        String body = WeChatUiHelper.formatMessageBody(
                message, context.currentUserId, context.buildUserDisplayNameMap());
        tvMessage.setText(body);
        try {
            CharSequence text = tvMessage.getText();
            if (text instanceof Spannable) {
                MoonBroadUtils.replaceEmoticons(
                        LQREmotionBroadKit.getContext(),
                        (Editable) text,
                        0,
                        text.length());
            }
        } catch (Exception ignored) {
        }
    }
}
