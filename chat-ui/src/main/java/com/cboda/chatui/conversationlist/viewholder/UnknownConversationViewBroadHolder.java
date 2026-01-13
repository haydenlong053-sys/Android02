/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ConversationInfoBroadType;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.third.utils.UIBroadUtils;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;

@ConversationInfoBroadType(type = Conversation.ConversationType.Single, line = 0)
@EnableContextBroadMenu
public class UnknownConversationViewBroadHolder extends ConversationViewBroadHolder {
    public UnknownConversationViewBroadHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        Glide
                .with(fragment)
                .load(R.mipmap.avatar_broad_def)
                .transforms(new CenterCrop(), new RoundedCorners(UIBroadUtils.dip2Px(4)))
                .into(portraitImageView);
        nameTextView.setText("未知会话类型(" + conversationInfo.conversation.type.getValue() + ")或线路(" + conversationInfo.conversation.line + ")");
    }

}
