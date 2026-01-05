/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.MessageContextMenuBroadItem;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.ConversationMessageBroadAdapter;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.mm.MMPreviewBroadActivity;
import com.cboda.chatui.mm.MediaBroadEntry;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.core.MessageContentBroadType;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

public abstract class MediaMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {

    /**
     * 小视频，图片 占位图的配置
     */
    protected RequestOptions placeholderOptions = new RequestOptions();

    public MediaMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        placeholderOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        placeholderOptions.centerCrop();
        placeholderOptions.placeholder(R.drawable.image_chat_placeholder);
    }

    @Override
    protected void onBind(UiBroadMessage message) {
        if (message.isDownloading || message.message.status == MessageStatus.Sending) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    protected void previewMM() {
        List<UiBroadMessage> messages = ((ConversationMessageBroadAdapter) adapter).getMessages();
        List<MediaBroadEntry> entries = new ArrayList<>();
        UiBroadMessage msg;

        int current = 0;
        int index = 0;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);
//            if (msg.message.content.getMessageContentType() != MessageContentBroadType.ContentType_Image
//                && msg.message.content.getMessageContentType() != MessageContentBroadType.ContentType_Video) {
//                continue;
//            }
            if (msg.message.content.getMessageContentType() != MessageContentBroadType.ContentType_Image
                    ) {
                continue;
            }
            MediaBroadEntry entry = new MediaBroadEntry(msg.message);
            entries.add(entry);

            if (message.message.messageId == msg.message.messageId) {
                current = index;
            }
            index++;
        }
        if (entries.isEmpty()) {
            return;
        }
        MMPreviewBroadActivity.previewMedia(fragment.getContext(), entries, current, messages.get(0).message.conversation.type == Conversation.ConversationType.SecretChat);
    }

    /**
     * 图片 和小视频 加载的地方
     * 策略是先加载缩略图，在加载原图
     *
     * @param thumbnail
     * @param imagePath
     * @param imageView
     */
    protected void loadMedia(Bitmap thumbnail, String imagePath, ImageView imageView) {
        RequestBuilder<Drawable> thumbnailRequest = null;
        if (thumbnail != null) {
            thumbnailRequest = Glide
                .with(fragment)
                .load(thumbnail);
        } else {
            thumbnailRequest = Glide
                .with(fragment)
                .load(R.drawable.image_chat_placeholder);
        }
        RequestBuilder<Drawable> request = Glide.with(fragment)
            .load(imagePath)
            .thumbnail(thumbnailRequest)
            .apply(placeholderOptions);
        if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
            request = request.diskCacheStrategy(DiskCacheStrategy.NONE);
        }
        request.into(imageView);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_CANCEL_SEND, confirm = false, priority = 13)
    public void cancelSend(View itemView, UiBroadMessage message) {
        boolean canceled = ChatManager.Instance().cancelSendingMessage(message.message.messageId);
        if (!canceled) {
            Toast.makeText(fragment.getContext(), "取消失败", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public String contextMenuTitle(Context context, String tag) {
        if (MessageContextMenuItemBroadTags.TAG_CANCEL_SEND.equals(tag)) {
            return " 取消发送";
        }
        return super.contextMenuTitle(context, tag);
    }

    @Override
    public boolean contextMenuItemFilter(UiBroadMessage uiMessage, String tag) {
        if (MessageContextMenuItemBroadTags.TAG_CANCEL_SEND.equals(tag)) {
            return !(uiMessage.message.content instanceof MediaMessageContent && MessageStatus.Sending == uiMessage.message.status);
        } else {
            return super.contextMenuItemFilter(uiMessage, tag);
        }
    }
}
