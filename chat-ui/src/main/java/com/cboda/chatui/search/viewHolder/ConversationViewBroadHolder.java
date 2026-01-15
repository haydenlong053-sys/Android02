/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.viewHolder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cboda.chatui.R;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

public class ConversationViewBroadHolder extends ResultItemViewBroadHolder<ConversationSearchResult> {
    ImageView portraitImageView;
    TextView nameTextView;
    TextView descTextView;

    private UserViewBroadModel userViewModel;
    private GroupViewBroadModel groupViewModel;

    public ConversationViewBroadHolder(Fragment fragment, View itemView) {
        super(fragment, itemView);
        bindViews(itemView);

        userViewModel = new ViewModelProvider(fragment).get(UserViewBroadModel.class);
        groupViewModel = new ViewModelProvider(fragment).get(GroupViewBroadModel.class);
    }

    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        descTextView = itemView.findViewById(R.id.descTextView);
    }

    @Override
    public void onBind(String keyword, ConversationSearchResult conversationSearchResult) {
        Conversation conversation = conversationSearchResult.conversation;
        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
            if (userInfo != null) {
                Glide.with(fragment).load(userInfo.portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_broad_def)).into(portraitImageView);
                nameTextView.setText(userViewModel.getUserDisplayName(userInfo));
            }
        } else {
            GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
            if (groupInfo != null) {
                Glide.with(fragment).load(groupInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.ic_group_broad_chat).centerCrop()).into(portraitImageView);
                nameTextView.setText(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name);
            }
        }

        if (conversationSearchResult.marchedMessage != null) {
            descTextView.setText(conversationSearchResult.marchedMessage.digest());
        } else {
            descTextView.setText(conversationSearchResult.marchedCount + "条记录");
        }
    }
}
