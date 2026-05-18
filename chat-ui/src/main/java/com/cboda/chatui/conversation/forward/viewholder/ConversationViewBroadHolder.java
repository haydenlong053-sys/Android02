/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.forward.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.FriendBean;
import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.chatui.R;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;

import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ConversationViewBroadHolder extends RecyclerView.ViewHolder {
    private Fragment fragment;
    private UserViewBroadModel userViewModel;
    private GroupViewBroadModel groupViewModel;
    TextView nameTextView;
    ImageView portraitImageView;
    String portrait;

    public ConversationViewBroadHolder(Fragment fragment, View itemView) {
        super(itemView);
        this.fragment = fragment;
        bindViews(itemView);

        userViewModel = new ViewModelProvider(fragment).get(UserViewBroadModel.class);
        groupViewModel = new ViewModelProvider(fragment).get(GroupViewBroadModel.class);
    }

    private void bindViews(View itemView) {
        nameTextView = itemView.findViewById(R.id.nameTextView);
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
    }

    public void onBind(ConversationInfo conversationInfo) {
        Conversation conversation = conversationInfo.conversation;
        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
            eLogD("MessageViewHolderClass", "userInfo=null:" + userInfo == null);
            if (userInfo != null) {
                portrait = userInfo.portrait;
//                eLogD("MessageViewHolderClass", "用户头像：" + portrait);
                final String[] displayName = {"<" + userInfo.uid + ">"};
                if (conversation.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
                    displayName[0] = MMKVBroadUtils.Companion.getKeFuName();
                    nameTextView.setText(displayName[0]);
                    Glide.with(fragment).load(portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_broad_def)).into(portraitImageView);
                } else if (userInfo != null && !TextUtils.isEmpty(userInfo.friendAlias)) {
                    displayName[0] = userInfo.friendAlias;
                    nameTextView.setText(displayName[0]);
                    Glide.with(fragment).load(portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_broad_def)).into(portraitImageView);
                } else if (userInfo != null && !(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
                    displayName[0] = userInfo.displayName;
                    nameTextView.setText(displayName[0]);
                    Glide.with(fragment).load(portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_broad_def)).into(portraitImageView);
                } else {
                    RealmUtil.INSTANCE.findUserData(conversation.target, friendSmailListBean -> {
                        if (friendSmailListBean != null) {
                            portrait = friendSmailListBean.userImg;
                            if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                                displayName[0] = friendSmailListBean.remarkName;
                            } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                                displayName[0] = friendSmailListBean.nickName;
                            }
                        }
                        nameTextView.setText(displayName[0]);
                        Glide.with(fragment).load(portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_broad_def)).into(portraitImageView);
                        return null;
                    });
                }
            }
        } else {
            GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
            if (groupInfo != null) {
                Glide.with(fragment).load(groupInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.ic_group_broad_chat).centerCrop()).into(portraitImageView);
                nameTextView.setText(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name);
            }
        }
    }

}
