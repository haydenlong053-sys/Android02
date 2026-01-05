/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.bumptech.glide.Glide;

import com.cboda.chatui.ChatManagerHolder;
import com.cboda.chatui.annotation.ConversationInfoType;
import com.cboda.chatui.annotation.EnableContextMenu;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;

@ConversationInfoType(type = Conversation.ConversationType.Single, line = 0)
@EnableContextMenu
public class SingleConversationViewHolder extends ConversationViewHolder {
    private String portrait;

    public SingleConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(conversationInfo.conversation.target, false);
        this.portrait = userInfo.portrait;
        GlobalFriendBroadBean friendSmailListBean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversationInfo.conversation.target);
        String name = getMyUserDisplayName(userInfo, friendSmailListBean);
        Glide.with(fragment)
                .load(portrait)
                .placeholder(com.cboda.baselibrary.R.mipmap.ic_logo_login)
                .into(portraitImageView);
        nameTextView.setText(name);
        if (null != friendSmailListBean && null != friendSmailListBean.grade) {
            gradeTv.setVisibility(View.VISIBLE);
            gradeTv.setText(friendSmailListBean.grade + "分");
        } else {
            gradeTv.setVisibility(View.GONE);
        }
        if (null != friendSmailListBean && null != friendSmailListBean.registerDay) {
            nameTimeTv.setVisibility(View.VISIBLE);
            nameTimeTv.setText(friendSmailListBean.registerDay + "天");
        } else {
            nameTimeTv.setVisibility(View.VISIBLE);
            nameTimeTv.setText("0天");
        }
    }


    private String getMyUserDisplayName(UserInfo userInfo, GlobalFriendBroadBean friendSmailListBean) {
        if (friendSmailListBean != null) {
            portrait = friendSmailListBean.icon;
        }
        if (userInfo != null && !TextUtils.isEmpty(userInfo.friendAlias)) {
            return userInfo.friendAlias;
        } else if (userInfo != null && !(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
            return userInfo.displayName;
        } else {
            if (friendSmailListBean != null) {
                if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                    return friendSmailListBean.remarkName;
                } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                    return friendSmailListBean.nickName;
                }
            }
            if (userInfo == null) {
                return "";
            }
            return "<" + userInfo.uid + ">";
        }
    }

}
