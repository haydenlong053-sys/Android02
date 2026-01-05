/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcWebViewBroadActivity;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.LinkMessageContent;

@MessageContentBroadType(LinkMessageContent.class)
@EnableContextBroadMenu
public class LinkMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    ImageView thumbnailImageView;
    TextView titleTextView;
    TextView descTextView;

    private LinkMessageContent linkMessageContent;

    public LinkMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.linkMessageContentItemView).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        thumbnailImageView =itemView.findViewById(R.id.thumbnailImageView);
        titleTextView =itemView.findViewById(R.id.titleTextView);
        descTextView =itemView.findViewById(R.id.descTextView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        linkMessageContent = (LinkMessageContent) message.message.content;
        titleTextView.setText(linkMessageContent.getTitle());
        descTextView.setText(!TextUtils.isEmpty(linkMessageContent.getContentDigest()) ? linkMessageContent.getContentDigest() : linkMessageContent.getUrl());
        Glide.with(fragment)
            .load(linkMessageContent.getThumbnailUrl())
            .placeholder(R.mipmap.logo)
            .into(thumbnailImageView);
    }

    public void onClick(View view) {
        WfcWebViewBroadActivity.loadUrl(fragment.getContext(), linkMessageContent.getTitle(), linkMessageContent.getUrl());
    }
}
