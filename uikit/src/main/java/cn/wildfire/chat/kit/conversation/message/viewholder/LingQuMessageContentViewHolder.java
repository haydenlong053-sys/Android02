package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.message.LingQuMessageContent;
import cn.wildfirechat.remote.ChatManager;

@MessageContentType(LingQuMessageContent.class)
@EnableContextMenu
public class LingQuMessageContentViewHolder extends NotificationMessageContentViewHolder {

    private TextView tvContent;
    private Gson gson = new Gson();

    public LingQuMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        tvContent = itemView.findViewById(R.id.tvContent);
    }


    @Override
    public void onBind(UiMessage message, int position) {
        super.onBind(message, position);
        LingQuMessageContent lqMessage = (LingQuMessageContent) message.message.content;
        if (lqMessage.sendUserId.equals(ChatManager.Instance().getUserId())) {
            Log.d("formatNotificationClass2", "lingquUserName:" + lqMessage.lingquUserName);
            if (message.message.sender.equals(ChatManager.Instance().getUserId())) {
                tvContent.setText("你领取了自己的红包");
            } else {
                tvContent.setText(lqMessage.lingquUserName + "领取了你的红包");
            }
        } else {
            Log.d("formatNotificationClass2", "sendUserName:" + lqMessage.sendUserName);
            if (lqMessage.lingquUserId.equals(ChatManager.Instance().getUserId())) {
                tvContent.setText("您领取了" + lqMessage.sendUserName + "红包");
            } else
                tvContent.setText(lqMessage.lingquUserName + "领取了" + lqMessage.sendUserName + "的红包");
        }
    }

}
