/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.text.TextUtils;
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
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.utils.DownloadBroadManager;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

@MessageContentBroadType(StickerMessageContent.class)
@EnableContextBroadMenu
public class StickerMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    private String path;
    ImageView imageView;

    public StickerMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        imageView =itemView.findViewById(R.id.stickerImageView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        StickerMessageContent stickerMessage = (StickerMessageContent) message.message.content;
        imageView.getLayoutParams().width = UIBroadUtils.dip2Px(stickerMessage.width > 150 ? 150 : stickerMessage.width);
        imageView.getLayoutParams().height = UIBroadUtils.dip2Px(stickerMessage.height > 150 ? 150 : stickerMessage.height);

        if (!TextUtils.isEmpty(stickerMessage.localPath)) {
            if (stickerMessage.localPath.equals(path)) {
                return;
            }
            Glide.with(fragment).load(stickerMessage.localPath)
                .into(imageView);
            path = stickerMessage.localPath;
        } else {
            String imagePath = stickerMessage.remoteUrl;
            if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
                imagePath = DownloadBroadManager.buildSecretChatMediaUrl(message.message);
            }
            CircularProgressDrawable progressDrawable = new CircularProgressDrawable(fragment.getContext());
            progressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
            progressDrawable.start();
            Glide.with(fragment)
                .load(imagePath)
                .placeholder(progressDrawable)
                .into(imageView);
        }
        if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
            if (message.message.direction == MessageDirection.Receive && message.message.status != MessageStatus.Played) {
                message.message.status = MessageStatus.Played;
                ChatManager.Instance().setMediaMessagePlayed(message.message.messageId);
            }
        }
    }

    // 其实也没啥用，有itemView了，可以直接setXXXListener了, 只是简化了，不用调用setXXXListener
    // 更复杂的，比如设置播放进度条的滑动等，在通过itemView设置相关listener吧
    public void onClick(View view) {
        // TODO
    }

}
