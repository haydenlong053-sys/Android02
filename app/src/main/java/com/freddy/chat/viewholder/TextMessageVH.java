package com.freddy.chat.viewholder;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.cboda.chatemoji.LQREmotionBroadKit;
import com.cboda.chatemoji.MoonBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.mention.AtMentionSpanUtil;
import com.freddy.chat.utils.QuoteMessageContentUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

public class TextMessageVH extends BaseMessageVH {
    public final LinearLayout textBubbleRoot;
    public final TextView tvMessage;
    public final TextView tvQuoteRef;

    public TextMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_text, false);
        textBubbleRoot = contentView.findViewById(R.id.text_bubble_root);
        tvMessage = contentView.findViewById(R.id.tv_message);
        tvQuoteRef = contentView.findViewById(R.id.tv_quote_ref);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        applyBroadBubbleBackground(textBubbleRoot, sentByMe);
        if (sentByMe) {
            tvMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            tvMessage.setLinkTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            tvQuoteRef.setTextColor(0xB3FFFFFF);
        } else {
            int textColor = ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_text0);
            tvMessage.setTextColor(textColor);
            tvMessage.setLinkTextColor(textColor);
            tvQuoteRef.setTextColor(0x99000000);
        }
        bindQuote(message, context);
        bindMessageBody(message, context);
        refreshContextMenuLongClickOnContent();
    }

    private void bindQuote(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        QuoteMessageContentUtil.QuoteInfo quote = QuoteMessageContentUtil.parse(message.getContent());
        if (quote == null) {
            tvQuoteRef.setVisibility(View.GONE);
            tvQuoteRef.setOnClickListener(null);
            if (tvMessage.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();
                lp.topMargin = 0;
                tvMessage.setLayoutParams(lp);
            }
            return;
        }
        String preview = QuoteMessageContentUtil.formatPreviewLine(
                quote, context.currentUserId, context.buildUserDisplayNameMap());
        if (TextUtils.isEmpty(preview)) {
            tvQuoteRef.setVisibility(View.GONE);
            tvQuoteRef.setOnClickListener(null);
            if (tvMessage.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();
                lp.topMargin = 0;
                tvMessage.setLayoutParams(lp);
            }
            return;
        }
        tvQuoteRef.setVisibility(View.VISIBLE);
        tvQuoteRef.setText(preview);
        if (tvMessage.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();
            lp.topMargin = (int) (4 * itemView.getResources().getDisplayMetrics().density);
            tvMessage.setLayoutParams(lp);
        }
        tvQuoteRef.setOnClickListener(v -> {
            if (context.quoteClickListener != null) {
                context.quoteClickListener.onQuoteClick(quote);
            }
        });
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
}
