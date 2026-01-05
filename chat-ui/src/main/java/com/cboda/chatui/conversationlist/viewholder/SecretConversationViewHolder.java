/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.cboda.chatui.ChatManagerHolder;
import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ConversationInfoType;
import com.cboda.chatui.annotation.EnableContextMenu;
import com.cboda.chatui.third.utils.UIUtils;
import com.cboda.chatui.user.UserViewModel;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.SecretChatInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

@ConversationInfoType(type = Conversation.ConversationType.SecretChat, line = 0)
@EnableContextMenu
public class SecretConversationViewHolder extends ConversationViewHolder {

    public SecretConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        secretChatIndicator.setVisibility(View.VISIBLE);
        SecretChatInfo secretChatInfo = ChatManager.Instance().getSecretChatInfo(conversationInfo.conversation.target);
        if (secretChatInfo == null){
            return;
        }
        String userId = secretChatInfo.getUserId();
        if (TextUtils.isEmpty(userId)){
            return;
        }
        UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(userId, false);
        UserViewModel userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
        String name = userViewModel.getUserDisplayName(userInfo);
        String portrait;
        portrait = userInfo.portrait;
        Glide
            .with(fragment)
            .load(portrait)
            .placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(fragment.getContext(), 4)))
            .into(portraitImageView);
        nameTextView.setText(name);
    }

}
