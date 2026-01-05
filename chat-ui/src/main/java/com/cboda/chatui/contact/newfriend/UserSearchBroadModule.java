/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.newfriend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.UserViewBroadHolder;
import com.cboda.chatui.search.SearchableBroadModule;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SearchUserCallback;

public class UserSearchBroadModule extends SearchableBroadModule<UserInfo, UserViewBroadHolder> {
    @Override
    public UserViewBroadHolder onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.search_item_contact, parent, false);
        return new UserViewBroadHolder(fragment, null, itemView);
    }

    @Override
    public void onBind(Fragment fragment, UserViewBroadHolder holder, UserInfo userInfo) {
        holder.onBind(new UIUserBroadInfo(userInfo));
    }

    @Override
    public void onClick(Fragment fragment, UserViewBroadHolder holder, View view, UserInfo userInfo) {
//        Intent intent = new Intent(fragment.getActivity(), UserInfoActivity.class);
//        intent.putExtra("userInfo", userInfo);
//        fragment.startActivity(intent);
    }

    @Override
    public int getViewType(UserInfo userInfo) {
        return R.layout.contact_item_contact;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean expandable() {
        return false;
    }

    @Override
    public String category() {
        return null;
    }

    @Override
    public List<UserInfo> search(String keyword) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<UserInfo> userInfos = new ArrayList<>();
        ChatManager.Instance().searchUser(keyword, ChatManager.SearchUserType.General, 0, new SearchUserCallback() {
            @Override
            public void onSuccess(List<UserInfo> infos) {
                userInfos.addAll(infos);
                countDownLatch.countDown();
            }

            @Override
            public void onFail(int errorCode) {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userInfos;
    }
}
