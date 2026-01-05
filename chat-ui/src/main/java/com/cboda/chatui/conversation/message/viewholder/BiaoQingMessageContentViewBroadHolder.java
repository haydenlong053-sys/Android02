package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.BiaoQingMessageContent;

@MessageContentBroadType(BiaoQingMessageContent.class)
@EnableContextBroadMenu
public class BiaoQingMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder{
    ImageView imageView;

    public BiaoQingMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        imageView =itemView.findViewById(R.id.stickerImageView);
    }

    @Override
    protected void onBind(UiBroadMessage message) {
        BiaoQingMessageContent stickerMessage = (BiaoQingMessageContent) message.message.content;
        Glide.with(fragment)
                .load(stickerMessage.emoji)
                .into(imageView);
    }
}
