package com.freddy.chat.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;

/**
 * 撤回消息独立行（居中提示），与系统消息布局分离，便于后续单独改样式。
 */
public final class RecallMessageVH extends RecyclerView.ViewHolder {
    public final TextView timeTextView;
    public final TextView tvRecallMessage;

    public RecallMessageVH(@NonNull View itemView) {
        super(itemView);
        timeTextView = itemView.findViewById(R.id.time_text_view);
        tvRecallMessage = itemView.findViewById(R.id.tv_recall_message);
    }

    public void bind(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        bindMessageTime(message);
        tvRecallMessage.setText(WeChatUiHelper.formatRecallDisplay(
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
