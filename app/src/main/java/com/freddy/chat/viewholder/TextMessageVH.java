package com.freddy.chat.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

public class TextMessageVH extends BaseMessageVH {
    public final ImageView ivContentType;
    public final TextView tvMessage;

    public TextMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_text);
        ivContentType = contentView.findViewById(R.id.iv_content_type);
        tvMessage = contentView.findViewById(R.id.tv_message);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        tvMessage.setVisibility(View.VISIBLE);
        bindMessageBody(message, context);
        bindDefaultContentTypeIcon(message.getMsgContentType());
    }

    protected final void bindMessageBody(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        tvMessage.setText(WeChatUiHelper.formatMessageBody(
                message, context.currentUserId, context.buildUserDisplayNameMap()));
    }

    protected final void showContentTypeIcon(int iconRes) {
        ivContentType.setVisibility(View.VISIBLE);
        ivContentType.setImageResource(iconRes);
    }

    protected final void hideContentTypeIcon() {
        ivContentType.setVisibility(View.GONE);
    }

    protected void bindDefaultContentTypeIcon(int msgContentType) {
        if (WeChatUiHelper.isVoiceMessage(msgContentType)) {
            showContentTypeIcon(R.drawable.ic_msg_voice);
        } else if (WeChatUiHelper.isImageMessage(msgContentType)) {
            showContentTypeIcon(R.drawable.ic_msg_image);
        } else if (WeChatUiHelper.isVideoMessage(msgContentType)) {
            showContentTypeIcon(R.drawable.ic_msg_video);
        } else if (WeChatUiHelper.isFileMessage(msgContentType)) {
            showContentTypeIcon(R.drawable.ic_msg_file);
        } else {
            hideContentTypeIcon();
        }
    }
}
