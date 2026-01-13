/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

@MessageContentBroadType(value = {
    CardMessageContent.class,

})
@EnableContextBroadMenu
public class UserCardMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    ImageView portraitImageView;
    TextView nameTextView;
    TextView userIdTextView;

    CardMessageContent userCardMessageContent;

    public UserCardMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.contentLayout).setOnClickListener(_v -> onUserCardClick());
    }

    private void bindViews(View itemView) {
        portraitImageView =itemView.findViewById(R.id.userCardPortraitImageView);
        nameTextView =itemView.findViewById(R.id.userCardNameTextView);
        userIdTextView =itemView.findViewById(R.id.userIdTextView);
    }

    @Override
    protected void onBind(UiBroadMessage message) {
        userCardMessageContent = (CardMessageContent) message.message.content;
        nameTextView.setText(userCardMessageContent.getDisplayName());
        userIdTextView.setText(userCardMessageContent.getName());
        userIdTextView.setVisibility(View.GONE);
        Glide
            .with(fragment)
            .load(userCardMessageContent.getPortrait())
            .transforms(new CenterCrop(), new RoundedCorners(10))
            .placeholder(R.mipmap.avatar_def)
            .into(portraitImageView);
    }

    void onUserCardClick() {
//        Intent intent = new Intent(fragment.getContext(), UserInfoActivity.class);
        UserInfo userInfo = ChatManager.Instance().getUserInfo(userCardMessageContent.getTarget(), false);
//        intent.putExtra("userInfo", userInfo);
//        fragment.startActivity(intent);
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getContext(),"tianjiahaoyou",userInfo.uid,"",null);
    }
}
