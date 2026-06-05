package com.freddy.chat.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.cboda.intelligent.R;
import com.freddy.im.bean.AppMessage;

public final class FileMessageVH extends TextMessageVH {
    public FileMessageVH(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        bindCommon(message, position, context);
        tvMessage.setVisibility(View.VISIBLE);
        bindMessageBody(message, context);
        showContentTypeIcon(R.drawable.ic_msg_file);
    }
}
