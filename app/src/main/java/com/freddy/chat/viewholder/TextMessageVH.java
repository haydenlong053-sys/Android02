package com.freddy.chat.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.FileUtils;
import com.cboda.baselibrary.utils.GlideUtilsKt;
import com.cboda.chatemoji.LQREmotionBroadKit;
import com.cboda.chatemoji.MoonBroadUtils;
import com.cboda.chatui.utils.FileBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.mention.AtMentionSpanUtil;
import com.freddy.chat.utils.QuoteMessageContentUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

public class TextMessageVH extends BaseMessageVH {
    public final LinearLayout textBubbleRoot;
    public final TextView tvMessage;
    public final View quoteContainer;
    public final TextView tvQuoteTitle;
    public final TextView tvQuoteDigest;
    public final View quoteThumbFrame;
    public final ImageView ivQuoteThumb;
    public final ImageView ivQuotePlay;

    public TextMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_text, false);
        textBubbleRoot = contentView.findViewById(R.id.text_bubble_root);
        tvMessage = contentView.findViewById(R.id.tv_message);
        quoteContainer = contentView.findViewById(R.id.quote_container);
        tvQuoteTitle = contentView.findViewById(R.id.tv_quote_title);
        tvQuoteDigest = contentView.findViewById(R.id.tv_quote_digest);
        quoteThumbFrame = contentView.findViewById(R.id.quote_thumb_frame);
        ivQuoteThumb = contentView.findViewById(R.id.iv_quote_thumb);
        ivQuotePlay = contentView.findViewById(R.id.iv_quote_play);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        applyBroadBubbleBackground(textBubbleRoot, sentByMe);
        if (sentByMe) {
            tvMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            tvMessage.setLinkTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        } else {
            int textColor = ContextCompat.getColor(itemView.getContext(), com.cboda.chatui.R.color.color_text0);
            tvMessage.setTextColor(textColor);
            tvMessage.setLinkTextColor(textColor);
        }
        bindQuote(message, context, sentByMe);
        bindMessageBody(message, context);
        refreshContextMenuLongClickOnContent();
    }

    @SuppressLint("SetTextI18n")
    private void bindQuote(@NonNull AppMessage message,
                           @NonNull MessageViewHolderContext context,
                           boolean sentByMe) {
//        eLogD("bindQuoteClass", "msgContent:" + message.getContent());
        eLogD("bindQuoteClass", "msgCustomInfo:" + message.getCustomInfo());
        QuoteMessageContentUtil.QuoteInfo quote = QuoteMessageContentUtil.parseFromMessage(message);
        if (quote == null) {
            hideQuote();
            return;
        }
        String name = QuoteMessageContentUtil.resolveDisplayNameForPreview(
                quote, context.currentUserId, context.buildUserDisplayNameMap());
        String digest = quote.digest == null ? "" : quote.digest.trim();
        boolean showThumb = quote.hasThumbPreview();
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(digest) && !showThumb) {
            hideQuote();
            return;
        }
        quoteContainer.setVisibility(View.VISIBLE);
        // 发送气泡内白底卡片，接收气泡内浅灰卡片，样式对齐设计稿
        quoteContainer.setBackgroundResource(
                sentByMe ? R.drawable.bg_message_quote_card : R.drawable.bg_message_quote_card_input);
        if (TextUtils.isEmpty(name)) {
            tvQuoteTitle.setText("");
        } else {
            tvQuoteTitle.setText(name);
        }
        bindQuoteThumb(quote, showThumb);
        // 图片/视频：对齐微信，摘要隐藏，只展示缩略图；文件保留文件名
        if (quote.isImageQuote() || quote.isVideoQuote()) {
            tvQuoteDigest.setText("");
            tvQuoteDigest.setVisibility(View.GONE);
        } else {
            tvQuoteDigest.setText(digest);
            tvQuoteDigest.setVisibility(TextUtils.isEmpty(digest) ? View.GONE : View.VISIBLE);
        }
        quoteContainer.setOnClickListener(v -> {
            if (context.quoteClickListener != null) {
                context.quoteClickListener.onQuoteClick(quote);
            }
        });
    }

    private void bindQuoteThumb(@NonNull QuoteMessageContentUtil.QuoteInfo quote, boolean showThumb) {
        if (!showThumb || quoteThumbFrame == null || ivQuoteThumb == null) {
            hideQuoteThumb();
            return;
        }
        quoteThumbFrame.setVisibility(View.VISIBLE);
//        eLogD("bindQuoteThumb","quote:"+toJson2(quote));
        if (quote.isFileQuote()) {
            String fileName = !TextUtils.isEmpty(quote.fileName) ? quote.fileName : quote.digest;
            ivQuoteThumb.setImageResource(FileBroadUtils.getFileTypeImageResId(fileName));
            if (ivQuotePlay != null) {
                ivQuotePlay.setVisibility(View.GONE);
            }
            return;
        }
        String thumb = quote.thumbUrl;
        if (TextUtils.isEmpty(thumb) || !FileUtils.isFileExists(thumb)) {
            thumb = quote.mediaUrl;
//            eLogD("bindQuoteThumb","isFileExists");
        }
        GlideUtilsKt.loadImg(ivQuoteThumb, thumb, com.cboda.baselibrary.R.mipmap.icon_image_placeholder_my);
        if (ivQuotePlay != null) {
            ivQuotePlay.setVisibility(quote.isVideoQuote() ? View.VISIBLE : View.GONE);
        }
    }

    private void hideQuoteThumb() {
        if (quoteThumbFrame != null) {
            quoteThumbFrame.setVisibility(View.GONE);
        }
        if (ivQuotePlay != null) {
            ivQuotePlay.setVisibility(View.GONE);
        }
        if (ivQuoteThumb != null) {
            ivQuoteThumb.setImageDrawable(null);
        }
    }

    private void hideQuote() {
        quoteContainer.setVisibility(View.GONE);
        quoteContainer.setOnClickListener(null);
        tvQuoteTitle.setText("");
        tvQuoteDigest.setText("");
        hideQuoteThumb();
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
