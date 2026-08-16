package com.cboda.chatui.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;

import cn.wildfirechat.message.MeetStartMessageContent;

@MessageContentBroadType(MeetStartMessageContent.class)
//@EnableContextBroadMenu
public class MeetStartMessageContentViewBroadHolder extends NotificationMessageContentViewBroadHolder {

    private TextView tvContent;

    public MeetStartMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        tvContent = itemView.findViewById(R.id.tvContent);
    }


    @Override
    public void onBind(UiBroadMessage message, int position) {
        super.onBind(message, position);
//        MeetStartMessageContent lqMessage = (MeetStartMessageContent) message.message.content;
        tvContent.setText("会议中" );
    }

}
