/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.receipt;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.R;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import com.cboda.chatui.widget.ProgressBroadFragment;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class GroupMessageReceiptListBroadFragment extends ProgressBroadFragment implements GroupMessageReceiptBroadAdapter.OnMemberClickListener {
    private GroupInfo groupInfo;
    private GroupMessageReceiptBroadAdapter groupMemberListAdapter;

    private boolean unread;
    private Message message;

    RecyclerView recyclerView;

    public static GroupMessageReceiptListBroadFragment newInstance(GroupInfo groupInfo, Message message, boolean unread) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        args.putParcelable("message", message);
        args.putBoolean("unread", unread);
        GroupMessageReceiptListBroadFragment fragment = new GroupMessageReceiptListBroadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
        this.message = getArguments().getParcelable("message");
        this.unread = getArguments().getBoolean("unread");

        if (groupInfo == null || message == null) {
            getActivity().finish();
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.conversation_receipt_broad_fragment;
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        if (groupInfo == null) {
            return;
        }
        bindViews(view);
        groupMemberListAdapter = new GroupMessageReceiptBroadAdapter(groupInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(groupMemberListAdapter);
        groupMemberListAdapter.setOnMemberClickListener(this);
        UserViewBroadModel userViewModel = ViewModelProviders.of(this).get(UserViewBroadModel.class);
        userViewModel.userInfoLiveData().observe(this, userInfos -> loadAndShowGroupMembers());
        loadAndShowGroupMembers();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    private void loadAndShowGroupMembers() {
        GroupViewBroadModel groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewBroadModel.class);
        groupViewModel.getGroupMemberUserInfosLiveData(groupInfo.target, false, this.message.serverTime).observe(this, userInfos -> {
            showContent();
            groupMemberListAdapter.setMembers(filterGroupMember(userInfos, unread));
            groupMemberListAdapter.notifyDataSetChanged();
        });
    }

    private List<UserInfo> filterGroupMember(List<UserInfo> userInfos, boolean unread) {
        Map<String, Long> readEntries = ChatManager.Instance().getConversationRead(this.message.conversation);
        List<UserInfo> result = new ArrayList<>();
        for (UserInfo info : userInfos) {
            if (TextUtils.equals(this.message.sender, info.uid)) {
                if (!unread) {
                    result.add(info);
                }
                continue;
            }
            Long readDt = readEntries.get(info.uid);
            if (unread) {
                if (readDt == null || readDt < message.serverTime) {
                    result.add(info);
                }
            } else {
                if (readDt != null && readDt >= message.serverTime) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    @Override
    public void onUserMemberClick(UserInfo userInfo) {
//        GroupMember groupMember = ChatManager.Instance().getGroupMember(groupInfo.target, ChatManager.Instance().getUserId());
//        if (groupInfo != null && groupInfo.privateChat == 1 && groupMember.type == GroupMember.GroupMemberType.Normal) {
//            return;
//        }
//        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
//        intent.putExtra("userInfo", userInfo);
//        intent.putExtra("groupId", groupInfo.target);
//        startActivity(intent);
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getActivity(),"weiduxiaoxitianjiahaoyou",userInfo.uid,"",null);
    }
}
