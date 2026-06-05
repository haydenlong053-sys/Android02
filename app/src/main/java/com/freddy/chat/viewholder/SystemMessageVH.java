package com.freddy.chat.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

public final class SystemMessageVH extends RecyclerView.ViewHolder {
    public final TextView tvSystemMessage;

    public SystemMessageVH(@NonNull View itemView) {
        super(itemView);
        tvSystemMessage = itemView.findViewById(R.id.tv_system_message);
    }

    public void bind(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        tvSystemMessage.setText(WeChatUiHelper.formatMessageBody(
                message, context.currentUserId, context.buildUserDisplayNameMap()));
    }
}
