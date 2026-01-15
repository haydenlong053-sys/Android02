/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.OrganizationServiceViewBroadModel;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.pick.PickConversationTargetBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.organization.model.Employee;
import com.cboda.chatui.organization.model.Organization;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

public class CreateConversationBroadActivity extends PickConversationTargetBroadActivity {
    private GroupViewBroadModel groupViewModel;

    @Override
    protected void afterViews() {
        super.afterViews();
        groupViewModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onContactPicked(List<UIUserBroadInfo> newlyCheckedUserInfos, List<Organization> organizations) {
        List<String> initialCheckedIds = pickUserViewModel.getInitialCheckedIds();
        List<UserInfo> userInfos = null;
        if (initialCheckedIds != null && !initialCheckedIds.isEmpty()) {
            UserViewBroadModel userViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
            userInfos = userViewModel.getUserInfos(initialCheckedIds);
        }
        userInfos = userInfos == null ? new ArrayList<>() : userInfos;

        for (UIUserBroadInfo uiUserinfo : newlyCheckedUserInfos) {
            userInfos.add(uiUserinfo.getUserInfo());
        }

        if (organizations != null && !organizations.isEmpty()) {
            OrganizationServiceViewBroadModel organizationServiceViewModel = new ViewModelProvider(this).get(OrganizationServiceViewBroadModel.class);
            List<Integer> orgIds = new ArrayList<>();
            for (Organization org : organizations) {
                orgIds.add(org.id);
            }
            List<UserInfo> finalUserInfos = userInfos;
            organizationServiceViewModel.getOrganizationEmployees(orgIds, true).observe(this, employees -> {
                if (employees != null) {
                    for (Employee e : employees) {
                        finalUserInfos.add(e.toUserInfo());
                    }
                }
                startConversation(finalUserInfos);
            });
        } else {
            startConversation(userInfos);
        }
    }

    private void startConversation(List<UserInfo> userInfos) {
        if (userInfos == null || userInfos.isEmpty()) {
            return;
        }
        if (userInfos.size() == 1) {
            Toast.makeText(this, "选择多位联系人时，才会发起群聊", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ConversationBroadActivity.class);
            Conversation conversation = new Conversation(Conversation.ConversationType.Single, userInfos.get(0).uid);
            intent.putExtra("conversation", conversation);
            startActivity(intent);
            finish();
        } else {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("创建中...")
                .progress(true, 100)
                .build();
            dialog.show();

            Map<String, UserInfo> userMap = new HashMap<>();
            for (UserInfo info : userInfos) {
                userMap.put(info.uid, info);
            }

            groupViewModel.createGroup(this, new ArrayList<UserInfo>(userMap.values()), null, Arrays.asList(0)).observe(this, result -> {
                dialog.dismiss();
                if (result.isSuccess()) {
                    Toast.makeText(this, getString(R.string.create_group_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateConversationBroadActivity.this, ConversationBroadActivity.class);
                    Conversation conversation = new Conversation(Conversation.ConversationType.Group, result.getResult(), 0);
                    intent.putExtra("conversation", conversation);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, getString(R.string.create_group_fail), Toast.LENGTH_SHORT).show();
                }
                finish();
            });
        }
    }

    @Override
    public void onGroupPicked(List<GroupInfo> groupInfos) {
        Intent intent = new Intent(this, ConversationBroadActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.Group, groupInfos.get(0).target);
        intent.putExtra("conversation", conversation);
        startActivity(intent);
        finish();
    }
}
