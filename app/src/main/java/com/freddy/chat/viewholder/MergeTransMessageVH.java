package com.freddy.chat.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.freddy.im.bean.AppMessage;

public final class MergeTransMessageVH extends TextMessageVH {
    public MergeTransMessageVH(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        super.bind(message, position, context);
    }
}
