/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.GlobalNoticeBroadBean;
import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseActivity;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.bridge.YeHuoCommonBridge;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.message.model.UiMessage;
import cn.wildfirechat.message.NoticeMessageContent;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

@MessageContentType(value = {
        NoticeMessageContent.class,
})
public class NoticeContentViewHolder extends NormalMessageContentViewHolder {
    ImageView hostPortraitImageView;
    TextView titleTextView;
    TextView descTextView;

    private NoticeMessageContent inviteMessageContent;

    public NoticeContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.contentLayout).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               GlobalNoticeBroadBean event = new GlobalNoticeBroadBean();
               event.setId(inviteMessageContent.getId());
               event.setContentUrl(inviteMessageContent.getContentUrl());
               event.setNoticeTitle(inviteMessageContent.getNoticeTitle());
               event.setCreateTime(inviteMessageContent.getCreateTime());
               event.setPreviewContent(inviteMessageContent.getPreviewContent());
               event.setJumpAppAction(1);
               EventBus.getDefault().post(event);
           }
       });
    }

    private void bindViews(View itemView) {
        hostPortraitImageView =itemView.findViewById(R.id.hostPortraitImageView);
        titleTextView =itemView.findViewById(R.id.titleTextView);
        descTextView =itemView.findViewById(R.id.descTextView);
    }

    @Override
    protected void onBind(UiMessage message) {
        inviteMessageContent = (NoticeMessageContent) message.message.content;
        titleTextView.setText(inviteMessageContent.getNoticeTitle());
        descTextView.setText(inviteMessageContent.getCreateTime());
//        UserInfo userInfo = ChatManager.Instance().getUserInfo(inviteMessageContent.getIdno(), false);
//        Glide
//            .with(fragment)
//            .load(userInfo.portrait)
//            .transforms(new CenterCrop(), new RoundedCorners(10))
//            .placeholder(R.mipmap.avatar_def)
//            .into(hostPortraitImageView);
    }


}
