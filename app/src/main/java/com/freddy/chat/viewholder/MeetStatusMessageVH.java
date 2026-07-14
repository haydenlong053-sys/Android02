package com.freddy.chat.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;

/**
 * 自研会议开始/结束通知条（对齐野火
 * {@code MeetStartMessageContentViewBroadHolder} /
 * {@code MeetEndMessageContentViewBroadHolder}）。
 */
public final class MeetStatusMessageVH extends RecyclerView.ViewHolder {

    private final TextView timeTextView;
    private final TextView tvContent;

    public MeetStatusMessageVH(@NonNull View itemView) {
        super(itemView);
        timeTextView = itemView.findViewById(R.id.time_text_view);
        tvContent = itemView.findViewById(R.id.tvContent);
    }

    public void bind(@NonNull AppMessage message) {
        bindMessageTime(message);
        int type = message.getMsgContentType();
        if (MessageType.MessageContentType.isMeetEnd(type)) {
            tvContent.setText("会议已结束");
        } else {
            tvContent.setText("会议中");
        }
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
