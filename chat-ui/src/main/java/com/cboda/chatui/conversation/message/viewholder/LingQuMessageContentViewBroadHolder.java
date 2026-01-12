package com.cboda.chatui.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;

import cn.wildfirechat.message.LingQuMessageContent;
import cn.wildfirechat.remote.ChatManager;

@MessageContentBroadType(LingQuMessageContent.class)
@EnableContextBroadMenu
public class LingQuMessageContentViewBroadHolder extends NotificationMessageContentViewBroadHolder {

    private TextView tvContent;
    private Gson gson = new Gson();

    public LingQuMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        tvContent = itemView.findViewById(R.id.tvContent);
    }


    @Override
    public void onBind(UiBroadMessage message, int position) {
        super.onBind(message, position);
        LingQuMessageContent lqMessage = (LingQuMessageContent) message.message.content;
        if (lqMessage.sendUserId.equals(ChatManager.Instance().getUserId())) {
            eLogD("formatNotificationClass2", "lingquUserName:" + lqMessage.lingquUserName);
            if (message.message.sender.equals(ChatManager.Instance().getUserId())) {
                tvContent.setText("你领取了自己的红包");
            } else {
                tvContent.setText(lqMessage.lingquUserName + "领取了你的红包");
            }
        } else {
            eLogD("formatNotificationClass2", "sendUserName:" + lqMessage.sendUserName);
            if (lqMessage.lingquUserId.equals(ChatManager.Instance().getUserId())) {
                tvContent.setText("您领取了" + lqMessage.sendUserName + "红包");
            } else
                tvContent.setText(lqMessage.lingquUserName + "领取了" + lqMessage.sendUserName + "的红包");
        }
    }

}
