/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.forward.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;

public class ConversationViewHolder extends RecyclerView.ViewHolder {
    private Fragment fragment;
    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    TextView nameTextView;
    ImageView portraitImageView;
    String portrait;

    public ConversationViewHolder(Fragment fragment, View itemView) {
        super(itemView);
        this.fragment = fragment;
        bindViews(itemView);

        userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
        groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);
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
                nameTextView.setText(getMyUserDisplayName(conversation,userInfo));
                Glide.with(fragment).load(portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_def)).into(portraitImageView);
            }
        } else {
            GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
            if (groupInfo != null) {
                Glide.with(fragment).load(groupInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.ic_group_chat).centerCrop()).into(portraitImageView);
                nameTextView.setText(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name);
            }
        }
    }

    private String getMyUserDisplayName(Conversation conversationInfo, UserInfo userInfo) {
        if (conversationInfo.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
            return MMKVBroadUtils.Companion.getKeFuName();
        } else if (userInfo != null && !TextUtils.isEmpty(userInfo.friendAlias)) {
            return userInfo.friendAlias;
        } else if (userInfo != null && !(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
            return userInfo.displayName;
        } else {
            GlobalFriendBroadBean friendSmailListBean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversationInfo.target);
            if (friendSmailListBean != null) {
                portrait = friendSmailListBean.icon;
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
