package com.freddy.chat.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

public final class SystemMessageVH extends RecyclerView.ViewHolder {
    public final TextView timeTextView;
    public final TextView tvSystemMessage;

    public SystemMessageVH(@NonNull View itemView) {
        super(itemView);
        timeTextView = itemView.findViewById(R.id.time_text_view);
        tvSystemMessage = itemView.findViewById(R.id.tv_system_message);
    }

    public void bind(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        bindMessageTime(message);
        tvSystemMessage.setText(WeChatUiHelper.formatMessageBody(
                message, context.currentUserId, context.buildUserDisplayNameMap()));
    }

    private void bindMessageTime(@NonNull AppMessage message) {
        long millis = WeChatUiHelper.normalizeToMillis(message.getTimestamp());
        if (millis <= 0L) {
            timeTextView.setVisibility(View.GONE);
            return;
        }
        timeTextView.setVisibility(View.VISIBLE);
        timeTextView.setText(TimeBroadBroadUtils.getMsgFormatTime(millis));
    }
}
