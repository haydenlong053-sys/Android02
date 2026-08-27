package com.freddy.chat.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageContentFormatter;

/**
 * 群红包领取通知条（contentType=104）。
 * 暂不使用
 */
@Deprecated()
public final class HongbaoClaimMessageVH extends RecyclerView.ViewHolder {

    private final TextView timeTextView;
    private final TextView tvContent;

    public HongbaoClaimMessageVH(@NonNull View itemView) {
        super(itemView);
        timeTextView = itemView.findViewById(R.id.time_text_view);
        tvContent = itemView.findViewById(R.id.tvContent);
    }

    public void bind(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        bindMessageTime(message);
        tvContent.setText(MessageContentFormatter.formatHongbaoClaimDisplay(
                message.getContent(), context.currentUserId));
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
