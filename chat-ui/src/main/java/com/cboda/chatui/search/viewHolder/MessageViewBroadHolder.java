/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.viewHolder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class MessageViewBroadHolder extends RecyclerView.ViewHolder {
    protected ImageView portraitImageView;
    protected TextView nameTextView;
    protected TextView contentTextView;
    protected TextView timeTextView;

    private Fragment fragment;
    private UserViewBroadModel userViewModel;

    public MessageViewBroadHolder(Fragment fragment, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.userViewModel = ViewModelProviders.of(fragment).get(UserViewBroadModel.class);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        timeTextView = itemView.findViewById(R.id.timeTextView);
    }

    public void onBind(Message message) {
        UserInfo sender = userViewModel.getUserInfo(message.sender, false);
        if (sender != null) {
            String senderName;
            if (message.conversation.type == Conversation.ConversationType.Group) {
                senderName = ChatManager.Instance().getGroupMemberDisplayName(message.conversation.target, sender.uid);
            } else {
                senderName = ChatManager.Instance().getUserDisplayName(sender);
            }
            nameTextView.setText(senderName);
//            String portrait=sender.portrait;
//            eLogD("MessageViewHolderClass","用户头像："+portrait);
            Glide.with(portraitImageView).load(sender.portrait).placeholder(R.mipmap.avatar_broad_def).into(portraitImageView);
        }
        if (message.content instanceof NotificationMessageContent) {
            contentTextView.setText(((NotificationMessageContent) message.content).formatNotification(message));
        } else {
            contentTextView.setText(message.digest());
        }
        timeTextView.setText(TimeBroadBroadUtils.getMsgFormatTime(message.serverTime));
    }
}
