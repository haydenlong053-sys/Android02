package com.freddy.chat.viewholder;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.cboda.chatemoji.LQREmotionBroadKit;
import com.cboda.chatemoji.MoonBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.mention.AtMentionSpanUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageEditHelper;

public class TextMessageVH extends BaseMessageVH {
    public final TextView tvMessage;
    public final TextView tvEdited;
    private final View textBubbleRoot;

    public TextMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_text, false);
        textBubbleRoot = contentView.findViewById(R.id.text_bubble_root);
        tvMessage = contentView.findViewById(R.id.tv_message);
        tvEdited = contentView.findViewById(R.id.tv_edited);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        View bubbleTarget = textBubbleRoot != null ? textBubbleRoot : tvMessage;
        applyBroadBubbleBackground(bubbleTarget, sentByMe);
        if (sentByMe) {
            tvMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            tvMessage.setLinkTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            if (tvEdited != null) {
                tvEdited.setTextColor(0x99FFFFFF);
            }
        } else {
            int textColor = ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_text0);
            tvMessage.setTextColor(textColor);
            tvMessage.setLinkTextColor(textColor);
            if (tvEdited != null) {
                tvEdited.setTextColor(0x99000000);
            }
        }
        bindMessageBody(message, context);
        if (tvEdited != null) {
            boolean edited = MessageEditHelper.isEdited(message);
            tvEdited.setText(com.cboda.chatui.R.string.message_edited);
            tvEdited.setVisibility(edited ? View.VISIBLE : View.GONE);
            int bottom = edited ? dp(4) : dp(12);
            tvMessage.setPadding(
                    tvMessage.getPaddingLeft(),
                    tvMessage.getPaddingTop(),
                    tvMessage.getPaddingRight(),
                    bottom);
        }
    }

    protected final void bindMessageBody(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        String body = WeChatUiHelper.formatMessageBody(
                message, context.currentUserId, context.buildUserDisplayNameMap());
        boolean sentByMe = message.isSender(context.currentUserId);
        int mentionColor = sentByMe
                ? ContextCompat.getColor(itemView.getContext(), android.R.color.white)
                : ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_text0);
        Spannable spannable = new SpannableString(body);
        AtMentionSpanUtil.applyMentionHighlight(spannable, message.getCustomInfo(), mentionColor);
        tvMessage.setText(spannable);
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

    private int dp(int value) {
        float density = itemView.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
