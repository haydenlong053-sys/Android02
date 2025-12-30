/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.base.commonlibrary.bean.GlobalFriendBean;
import com.base.commonlibrary.bean.GlobalInfo;
import com.base.commonlibrary.bean.GlobalSmallGroupBean;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.ConversationInfoType;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.utils.GroupAvatarLoader;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.GetGroupMembersCallback;

@ConversationInfoType(type = Conversation.ConversationType.Group, line = 0)
@EnableContextMenu
public class GroupConversationViewHolder extends ConversationViewHolder {

    TextView organizationGroupIndicator;

    public GroupConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        organizationGroupIndicator = itemView.findViewById(R.id.organizationGroupIndicator);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        GroupInfo groupInfo = ChatManagerHolder.gChatManager.getGroupInfo(conversationInfo.conversation.target, false);
        String name;
        String portrait;
        if (groupInfo != null) {
            name = !TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name;
            portrait = groupInfo.portrait;
            if (groupInfo.type == GroupInfo.GroupType.Organization) {
                organizationGroupIndicator.setVisibility(View.VISIBLE);
            } else {
                organizationGroupIndicator.setVisibility(View.GONE);
            }
        } else {
            name = "群聊";
            portrait = null;
        }
//        if (portrait.startsWith("http")){
        nameTextView.setText(name);

        // 加载群头像
        loadGroupPortrait(groupInfo, conversationInfo.conversation.target, portrait);
        GlobalSmallGroupBean smallGroupBean = GlobalInfo.INSTANCE.queryGroupInfo(conversationInfo.conversation.target);
        if (null != smallGroupBean && null != smallGroupBean.getGroupDays()) {
            gradeTv.setVisibility(View.VISIBLE);
            gradeTv.setText(smallGroupBean.getGroupDays() + "天");
        } else {
            gradeTv.setVisibility(View.VISIBLE);
            gradeTv.setText("0天");
        }
        if (null != smallGroupBean && null != smallGroupBean.getRenameCount()) {
            nameTimeTv.setVisibility(View.VISIBLE);
            nameTimeTv.setText("改名" + smallGroupBean.getRenameCount() + "次");
        } else {
            nameTimeTv.setVisibility(View.VISIBLE);
            nameTimeTv.setText("改名0次");
        }
    }

    private void loadGroupPortrait(GroupInfo groupInfo, String groupId, String portrait) {
        // 如果群头像URL有效，直接加载
        if (!TextUtils.isEmpty(portrait) && (portrait.startsWith("http") || portrait.startsWith("file://"))) {
            Glide
                    .with(fragment)
                    .load(portrait)
                    .placeholder(R.mipmap.ic_group_chat)
                    .into(portraitImageView);
//        }else {
//            try {
//                JSONObject object =new JSONObject(portrait);
//                if (object.has("members")) {
//                    JSONArray members = object.getJSONArray("members");
//                    List<String> avatars=new ArrayList<>();
//                    for (int i = 0; i < members.length(); i++) {
//                        JSONObject member = members.getJSONObject(i);
//                        if (member.has("avatarUrl")){
//                            avatars.add(member.getString("avatarUrl"));
//
//                        }
//                    }
//                    GroupAvatarLoader.INSTANCE.loadToImageView(fragment.getActivity(), avatars,portraitImageView, groupInfo.target);
//
//                }else {
//                    Glide
//                            .with(fragment)
//                            .load(portrait)
//                            .placeholder(R.mipmap.ic_group_chat)
//                            .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(fragment.getContext(), 4)))
//                            .into(portraitImageView);
//                }
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//        }


            return;
        }

        // 如果群头像为空或无效，获取群成员信息并生成组合头像
        if (groupInfo != null && !TextUtils.isEmpty(groupId)) {
            // 先显示占位图
            portraitImageView.setImageResource(R.mipmap.ic_group_chat);

            // 异步获取群成员信息
            ChatManagerHolder.gChatManager.getGroupMembers(groupId, false, new GetGroupMembersCallback() {
                @Override
                public void onSuccess(List<GroupMember> groupMembers) {
                    if (groupMembers == null || groupMembers.isEmpty()) {
                        return;
                    }

                    // 获取最多9个成员的用户ID
                    List<String> memberIds = new ArrayList<>();
                    int maxCount = Math.min(groupMembers.size(), 9);
                    for (int i = 0; i < maxCount; i++) {
                        memberIds.add(groupMembers.get(i).memberId);
                    }

                    // 获取用户信息（包括头像）
                    List<UserInfo> userInfos = ChatManagerHolder.gChatManager.getUserInfos(memberIds, groupId);
                    if (userInfos == null || userInfos.isEmpty()) {
                        return;
                    }

                    // 提取头像URL列表
                    List<String> avatarUrls = new ArrayList<>();
                    for (UserInfo userInfo : userInfos) {
                        if (userInfo != null && !TextUtils.isEmpty(userInfo.portrait)) {
                            avatarUrls.add(userInfo.portrait);
                        } else if (userInfo != null) {
                            GlobalFriendBean globalFriendBean = GlobalInfo.INSTANCE.queryMyFriendInfo(userInfo.uid);
                            if (globalFriendBean != null) {
                                avatarUrls.add(globalFriendBean.icon);
                            }
                        }
                    }

                    // 如果有头像URL，使用GroupAvatarLoader加载组合头像
                    if (!avatarUrls.isEmpty() && fragment.getActivity() != null) {
                        GroupAvatarLoader.INSTANCE.loadToImageView(
                                fragment.getActivity(),
                                avatarUrls,
                                portraitImageView,
                                groupId
                        );
                    }
                }

                @Override
                public void onFail(int errorCode) {
                    // 获取失败，保持占位图
                }
            });
        } else {
            // 没有群信息，显示默认占位图
            portraitImageView.setImageResource(R.mipmap.ic_group_chat);
        }

    }

}
