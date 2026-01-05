/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextMenu;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.annotation.MessageContextMenuItem;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.forward.ForwardActivity;
import com.cboda.chatui.conversation.message.model.UiMessage;
import com.cboda.chatui.third.utils.UIUtils;
import cn.wildfirechat.message.StickerMessageContent;

@MessageContentType(StickerMessageContent.class)
@EnableContextMenu
public class ExampleRichNotificationMessageContentViewHolder extends NotificationMessageContentViewHolder {
    private String path;
    ImageView imageView;

    public ExampleRichNotificationMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.stickerImageView).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        imageView =itemView.findViewById(R.id.stickerImageView);
    }

    @Override
    public void onBind(UiMessage message, int position) {
        StickerMessageContent stickerMessage = (StickerMessageContent) message.message.content;
        imageView.getLayoutParams().width = UIUtils.dip2Px(stickerMessage.width > 150 ? 150 : stickerMessage.width);
        imageView.getLayoutParams().height = UIUtils.dip2Px(stickerMessage.height > 150 ? 150 : stickerMessage.height);

        if (!TextUtils.isEmpty(stickerMessage.localPath)) {
            if (stickerMessage.localPath.equals(path)) {
                return;
            }
            Glide.with(fragment).load(stickerMessage.localPath)
                .into(imageView);
            path = stickerMessage.localPath;
        } else {
            CircularProgressDrawable progressDrawable = new CircularProgressDrawable(fragment.getContext());
            progressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
            progressDrawable.start();
            Glide.with(fragment)
                .load(stickerMessage.remoteUrl)
                .placeholder(progressDrawable)
                .into(imageView);
        }
    }

    public void onClick(View view) {
        Toast.makeText(fragment.getContext(), "TODO", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean contextMenuItemFilter(UiMessage uiMessage, String itemTitle) {
        return false;
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_FORWARD, priority = 11)
    public void forwardMessage(View itemView, UiMessage message) {
        Intent intent = new Intent(fragment.getContext(), ForwardActivity.class);
        intent.putExtra("message", message.message);
        fragment.startActivity(intent);
    }
}
