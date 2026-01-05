/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.viewHolder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cboda.chatui.R;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

public class ConversationViewBroadHolder2 extends ResultItemViewBroadHolder<ConversationSearchResult> {
    ImageView portraitImageView;
    TextView nameTextView;
    TextView descTextView;
    View dividerLine;
    View mLlItemBg;

    private UserViewBroadModel userViewModel;
    private GroupViewBroadModel groupViewModel;

    public ConversationViewBroadHolder2(Fragment fragment, View itemView) {
        super(fragment, itemView);
        bindViews(itemView);

        userViewModel = ViewModelProviders.of(fragment).get(UserViewBroadModel.class);
        groupViewModel = ViewModelProviders.of(fragment).get(GroupViewBroadModel.class);
    }

    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        descTextView = itemView.findViewById(R.id.descTextView);
        dividerLine = itemView.findViewById(R.id.dividerLine);
        mLlItemBg = itemView.findViewById(R.id.mLlItemBg);
    }

    @Override
    public void onBind(String keyword, ConversationSearchResult conversationSearchResult) {
        goBind(keyword,conversationSearchResult);
    }


    @Override
    public void onBind2(String keyword, ConversationSearchResult conversationSearchResult, int currentPosition, int startPosition, int endPosition) {
        goBind(keyword,conversationSearchResult);
        if (currentPosition == endPosition) {
            dividerLine.setVisibility(View.GONE);
        } else {
            dividerLine.setVisibility(View.VISIBLE);
        }
        if (currentPosition == startPosition + 1 && currentPosition == endPosition) {
            //只有一条时
            mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_16dp);
        }else if (currentPosition ==startPosition+1){
            //第一条
            mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_only_top_16dp);
        }else if (currentPosition==endPosition){
            //最后一条
            mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_only_bottom_16dp);
        }else {
            mLlItemBg.setBackgroundResource(R.color.white);
        }

    }
    private void goBind(String keyword, ConversationSearchResult conversationSearchResult) {
        Conversation conversation = conversationSearchResult.conversation;
        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
            if (userInfo != null) {
                Glide.with(fragment).load(userInfo.portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_def)).into(portraitImageView);
                nameTextView.setText(userViewModel.getUserDisplayName(userInfo));
            }
        } else {
            GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
            if (groupInfo != null) {
                Glide.with(fragment).load(groupInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.ic_group_chat).centerCrop()).into(portraitImageView);
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
