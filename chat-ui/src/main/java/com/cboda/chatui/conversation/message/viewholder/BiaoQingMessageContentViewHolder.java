package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextMenu;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.message.model.UiMessage;
import cn.wildfirechat.message.BiaoQingMessageContent;

@MessageContentType(BiaoQingMessageContent.class)
@EnableContextMenu
public class BiaoQingMessageContentViewHolder extends NormalMessageContentViewHolder{
    ImageView imageView;

    public BiaoQingMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        imageView =itemView.findViewById(R.id.stickerImageView);
    }

    @Override
    protected void onBind(UiMessage message) {
        BiaoQingMessageContent stickerMessage = (BiaoQingMessageContent) message.message.content;
        Glide.with(fragment)
                .load(stickerMessage.emoji)
                .into(imageView);
    }
}
