/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ConversationContextMenuBroadItem;
import com.cboda.chatui.annotation.ConversationInfoBroadType;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.third.utils.UIBroadUtils;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.remote.ChatManager;

@ConversationInfoBroadType(type = Conversation.ConversationType.Channel, line = 0)
@EnableContextBroadMenu
public class ChannelConversationViewBroadHolder extends ConversationViewBroadHolder {

    public ChannelConversationViewBroadHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        ChannelInfo channelInfo = ChatManager.Instance().getChannelInfo(conversationInfo.conversation.target, false);
        String name;
        String portrait;
        if (channelInfo != null) {
            name = channelInfo.name;
            portrait = channelInfo.portrait;
        } else {
            name = "Channel<" + conversationInfo.conversation.target + ">";
            portrait = null;
        }
        nameTextView.setText(name);
        Glide
            .with(fragment)
            .load(portrait)
            .placeholder(R.mipmap.ic_channel)
            .transforms(new CenterCrop(), new RoundedCorners(UIBroadUtils.dip2Px(4)))
            .into(portraitImageView);
    }

    @ConversationContextMenuBroadItem(tag = ConversationContextMenuItemBroadTags.TAG_UNSUBSCRIBE,
        confirm = true,
        priority = 0)
    public void unSubscribeChannel(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.unSubscribeChannel(conversationInfo);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        if (ConversationContextMenuItemBroadTags.TAG_UNSUBSCRIBE.equals(tag)) {
            return "取消收听";
        }
        return super.contextMenuTitle(context, tag);
    }

    @Override
    public String contextConfirmPrompt(Context context, String tag) {
        if (ConversationContextMenuItemBroadTags.TAG_UNSUBSCRIBE.equals(tag)) {
            return "确认取消订阅频道?";
        }
        return super.contextConfirmPrompt(context, tag);
    }
}
